package com.rental.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rental.server.common.BusinessException;
import com.rental.server.common.Result;
import com.rental.server.entity.User;
import com.rental.server.mapper.UserMapper;
import com.rental.server.utils.JwtUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 登录接口：微信 code → openid → 查/建用户 → 返回 JWT
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${rental.wechat.appid}")
    private String appid;

    @Value("${rental.wechat.secret}")
    private String secret;

    @Value("${rental.wechat.mock:true}")
    private Boolean mock;

    public AuthController(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        String openid;

        if (mock) {
            // 开发期 mock：code 直接作为 openid 使用
            openid = "mock_" + request.getCode();
        } else {
            // 真实模式：调微信接口用 code 换 openid
            openid = getOpenidFromWechat(request.getCode());
        }

        // 查询或创建用户
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("openid", openid));
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setRole("tenant"); // 默认租客
            userMapper.insert(user);
        }

        // 生成 token
        String token = jwtUtil.generateToken(user.getId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(user);
        return Result.success(response);
    }

    /**
     * 调用微信接口，用 code 换取 openid
     * https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html
     */
    private String getOpenidFromWechat(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                throw new BusinessException("微信接口返回为空");
            }

            // 微信返回错误码
            if (response.containsKey("errcode")) {
                Integer errcode = (Integer) response.get("errcode");
                String errmsg = (String) response.get("errmsg");
                throw new BusinessException("微信登录失败: " + errcode + " " + errmsg);
            }

            String openid = (String) response.get("openid");
            if (openid == null || openid.isEmpty()) {
                throw new BusinessException("微信接口未返回 openid");
            }
            return openid;
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException("调用微信接口失败: " + e.getMessage());
        }
    }

    @Data
    public static class LoginRequest {
        private String code; // 微信登录 code
    }

    @Data
    public static class LoginResponse {
        private String token;
        private User user;
    }
}
