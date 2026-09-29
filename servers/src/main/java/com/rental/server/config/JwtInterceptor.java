package com.rental.server.config;

import com.rental.server.common.BusinessException;
import com.rental.server.utils.JwtUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 拦截器：校验请求头中的 token，解析出 userId 并存入 request attribute
 * 后续 Controller 可通过 request.getAttribute("userId") 获取当前登录用户 ID
 */
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录或 token 格式错误");
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.parseToken(token);
        if (userId == null) {
            throw new BusinessException(401, "token 无效或已过期");
        }

        // 把 userId 存入 request，后续 Controller 可以直接取
        request.setAttribute("userId", userId);
        return true;
    }
}
