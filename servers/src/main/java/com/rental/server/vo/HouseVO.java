package com.rental.server.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 房源返回对象：在 House 实体基础上补充前端需要的字段
 */
@Data
public class HouseVO {

    private Long id;
    private Long userId;
    private String title;
    private String address;
    private BigDecimal lat;
    private BigDecimal lng;
    private BigDecimal area;
    private String layout;
    private Integer price;
    /** 图片路径数组（由 images 字段的 JSON 字符串解析而来） */
    private List<String> images;
    /** 封面图，取 images 第一张，方便列表卡片直接使用 */
    private String cover;
    private Integer status;
    private LocalDateTime createTime;

    /** 距当前位置的距离（公里），仅附近查询时填充 */
    private Double distance;

    /** 房东姓名 */
    private String landlordName;
    /** 房东联系方式 */
    private String landlordContact;
    /** 房东性别：1=男，2=女 */
    private Integer landlordGender;
}