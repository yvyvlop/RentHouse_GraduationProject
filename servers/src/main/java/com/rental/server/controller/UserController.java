package com.rental.server.controller;

import com.rental.server.common.BusinessException;
import com.rental.server.common.Result;
import com.rental.server.entity.User;
import com.rental.server.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息接口：需要登录，JWT 拦截器会校验 token 并注入 userId
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 获取当前登录用户信息
     * 请求头：Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 更新当前用户信息（姓名、性别、联系方式、身份）
     * 请求头：Authorization: Bearer <token>
     */
    @PutMapping("/me")
    public Result<Void> updateCurrentUser(
            HttpServletRequest request,
            @RequestBody UpdateUserRequest body) {
        Long userId = (Long) request.getAttribute("userId");

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 只更新非空字段
        if (body.getName() != null) {
            user.setName(body.getName());
        }
        if (body.getGender() != null) {
            user.setGender(body.getGender());
        }
        if (body.getContact() != null) {
            user.setContact(body.getContact());
        }
        if (body.getRole() != null) {
            if (!"tenant".equals(body.getRole()) && !"landlord".equals(body.getRole())) {
                throw new BusinessException(400, "身份只能是 tenant 或 landlord");
            }
            user.setRole(body.getRole());
        }

        userMapper.updateById(user);
        return Result.success();
    }

    @Data
    public static class UpdateUserRequest {
        private String name;
        private Integer gender;
        private String contact;
        private String role;
    }
}
