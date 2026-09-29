-- ============================================
-- 租房管理系统 数据库初始化脚本
-- 环境：MySQL 8.0，字符集 utf8mb4
-- ============================================

CREATE DATABASE IF NOT EXISTS userdb
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE userdb;

-- --------------------------------------------
-- 用户表（租客 / 房东共用，role 区分身份）
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid`      VARCHAR(64)  NOT NULL COMMENT '微信唯一标识，登录时由后端换取',
  `role`        VARCHAR(16)  NOT NULL DEFAULT 'tenant' COMMENT '身份：tenant=租客，landlord=房东',
  `name`        VARCHAR(32)  DEFAULT NULL COMMENT '姓名',
  `gender`      TINYINT      DEFAULT NULL COMMENT '性别：1=男，2=女',
  `contact`     VARCHAR(32)  DEFAULT NULL COMMENT '联系方式（电话）',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- --------------------------------------------
-- 房源表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `house` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT        NOT NULL COMMENT '发布人，关联 user.id',
  `title`       VARCHAR(64)   DEFAULT NULL COMMENT '房源标题',
  `address`     VARCHAR(255)  NOT NULL COMMENT '详细地址',
  `lat`         DECIMAL(10,6) NOT NULL COMMENT '纬度，距离计算用',
  `lng`         DECIMAL(10,6) NOT NULL COMMENT '经度，距离计算用',
  `area`        DECIMAL(8,2)  DEFAULT NULL COMMENT '房间大小（平方米）',
  `layout`      VARCHAR(32)   DEFAULT NULL COMMENT '格局，如"2室1厅"',
  `price`       INT           DEFAULT NULL COMMENT '租金（元/月）',
  `images`      TEXT          DEFAULT NULL COMMENT '图片相对路径 JSON 数组',
  `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1=上架，0=下架',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_lat_lng` (`lat`, `lng`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源表';