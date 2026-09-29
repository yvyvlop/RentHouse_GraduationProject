package com.rental.server.controller;

import com.rental.server.common.BusinessException;
import com.rental.server.common.Result;
import com.rental.server.mapper.HouseMapper;
import com.rental.server.mapper.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查接口：验证服务启动 + 数据库连通 + 实体映射
 * 浏览器/PowerShell 访问 http://localhost:8080/api/ping 查看结果
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final UserMapper userMapper;
    private final HouseMapper houseMapper;

    public HealthController(UserMapper userMapper, HouseMapper houseMapper) {
        this.userMapper = userMapper;
        this.houseMapper = houseMapper;
    }

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        // 走 MyBatis-Plus BaseMapper 查询，能返回数字说明 MySQL 连接和实体映射都正常
        Long userCount = userMapper.selectCount(null);
        Long houseCount = houseMapper.selectCount(null);
        return Result.success(Map.of(
                "userCount", userCount,
                "houseCount", houseCount));
    }

    /**
     * 测试业务异常：验证全局异常处理器是否生效
     * 访问 http://localhost:8080/api/test-error 应返回 {"code":500,"msg":"测试业务异常"}
     */
    @GetMapping("/test-error")
    public Result<Void> testError() {
        throw new BusinessException("测试业务异常");
    }
}
