package com.rental.server.config;

import com.rental.server.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：静态资源映射 + JWT 拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;

    @Value("${rental.image-dir}")
    private String imageDir;

    @Value("${rental.image-url-prefix}")
    private String imageUrlPrefix;

    public WebConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 静态资源映射：把本地图片目录暴露为 /images/** URL
     * 例：/data/user/images/2026/09/abc.jpg → http://host:8080/images/2026/09/abc.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(imageUrlPrefix + "/**")
                .addResourceLocations("file:" + imageDir + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor(jwtUtil))
                .addPathPatterns("/api/**") // 拦截所有 /api 下的接口
                .excludePathPatterns(
                        "/api/login", // 登录接口不需要 token
                        "/api/ping", // 健康检查
                        "/api/house/nearby", // 附近房源（游客可看）
                        "/api/house/search", // 搜索（游客可看）
                        "/api/house/detail/**" // 房源详情（游客可看）
                );
    }
}