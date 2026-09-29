package com.rental.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应 user 表
 * 租客和房东共用一张表，用 role 字段区分身份
 */
@Data
@TableName("user")
public class User {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 微信唯一标识，登录时由后端用 code 向微信服务器换取 */
    private String openid;

    /** 身份：tenant=租客，landlord=房东 */
    private String role;

    /** 姓名（房东发布房源时展示） */
    private String name;

    /** 性别：1=男，2=女 */
    private Integer gender;

    /** 联系方式（电话），房源详情页展示 */
    private String contact;

    /** 注册时间，数据库自动填充 */
    private LocalDateTime createTime;
}
