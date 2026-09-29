package com.rental.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 租房管理系统后端入口
 */
@SpringBootApplication
@MapperScan("com.rental.server.mapper")
public class RentalServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalServerApplication.class, args);
    }
}
