package com.rental.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 房源实体，对应 house 表
 */
@Data
@TableName("house")
public class House {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布人 ID，关联 user.id */
    private Long userId;

    /** 房源标题 */
    private String title;

    /** 详细地址（小程序 chooseLocation 选点带回） */
    private String address;

    /** 纬度，距离计算用 */
    private BigDecimal lat;

    /** 经度，距离计算用 */
    private BigDecimal lng;

    /** 房间大小（平方米） */
    private BigDecimal area;

    /** 格局，如"2室1厅" */
    private String layout;

    /** 租金（元/月） */
    private Integer price;

    /** 图片相对路径 JSON 数组字符串，如 ["/images/2026/09/xx.jpg"] */
    private String images;

    /**
     * 图片路径数组，仅用于接收前端请求参数，不映射数据库字段
     * 入库前由 Service 转成 JSON 字符串存入 images
     */
    @TableField(exist = false)
    private List<String> imageList;

    /** 状态：1=上架，0=下架（软删除） */
    private Integer status;

    /**
     * 距查询点的距离（公里），仅附近查询时由 SQL 计算填充，不映射数据库字段
     */
    @TableField(exist = false)
    private Double distance;

    /** 发布时间，数据库自动填充 */
    private LocalDateTime createTime;
}
