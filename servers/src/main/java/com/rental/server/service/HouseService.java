package com.rental.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.server.common.BusinessException;
import com.rental.server.entity.House;
import com.rental.server.entity.User;
import com.rental.server.mapper.HouseMapper;
import com.rental.server.mapper.UserMapper;
import com.rental.server.vo.HouseVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 房源业务逻辑
 */
@Service
public class HouseService {

    private final HouseMapper houseMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public HouseService(HouseMapper houseMapper, UserMapper userMapper, ObjectMapper objectMapper) {
        this.houseMapper = houseMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 附近房源：按距离升序返回
     * 先用矩形边界粗筛（走 idx_lat_lng 索引），再由 SQL 用 Haversine 精确计算距离并过滤
     *
     * @param lat    用户纬度
     * @param lng    用户经度
     * @param radius 搜索半径（公里）
     * @param limit  最多返回条数
     */
    public List<HouseVO> nearby(double lat, double lng, double radius, int limit) {
        // 纬度方向：1 度约等于 111 公里，与所在纬度无关
        double latDelta = radius / 111.0;

        // 经度方向：1 度的实际距离随纬度升高而缩短，需要除以 cos(纬度)
        double cosLat = Math.cos(Math.toRadians(lat));
        // 极地附近 cos 接近 0 会导致除零，此时直接放开整个经度范围
        double lngDelta = cosLat < 1e-6 ? 180.0 : radius / (111.0 * cosLat);

        List<House> list = houseMapper.selectNearby(
                lat, lng,
                lat - latDelta, lat + latDelta,
                lng - lngDelta, lng + lngDelta,
                radius, limit);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 关键词搜索：标题或地址模糊匹配，传了经纬度则按距离升序
     */
    public List<HouseVO> search(String keyword, Double lat, Double lng, int limit) {
        List<House> list = houseMapper.search(keyword, lat, lng, limit);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 发布房源
     */
    public Long publish(Long userId, House house) {
        house.setUserId(userId);
        house.setStatus(1); // 默认上架
        house.setImages(toJson(house.getImageList())); // 图片数组 → JSON 字符串
        houseMapper.insert(house);
        return house.getId();
    }

    /**
     * 我的房源列表（只查当前用户发布的，含已下架）
     */
    public List<HouseVO> listMine(Long userId) {
        List<House> list = houseMapper.selectList(
                new QueryWrapper<House>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time"));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 房源详情（含房东信息）
     */
    public HouseVO getDetail(Long id) {
        House house = houseMapper.selectById(id);
        if (house == null) {
            throw new BusinessException(404, "房源不存在");
        }
        HouseVO vo = toVO(house);

        // 补房东信息（详情页展示联系方式用）
        User landlord = userMapper.selectById(house.getUserId());
        if (landlord != null) {
            vo.setLandlordName(landlord.getName());
            vo.setLandlordContact(landlord.getContact());
            vo.setLandlordGender(landlord.getGender());
        }
        return vo;
    }

    /**
     * 编辑房源（校验归属：只能改自己的）
     */
    public void update(Long userId, Long id, House house) {
        House existing = houseMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "房源不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改他人的房源");
        }

        house.setId(id);
        house.setUserId(userId); // 防止被篡改归属
        house.setImages(toJson(house.getImageList()));
        houseMapper.updateById(house);
    }

    /**
     * 下架房源（软删除，status=0，保留历史数据）
     */
    public void delete(Long userId, Long id) {
        House existing = houseMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "房源不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人的房源");
        }

        House update = new House();
        update.setId(id);
        update.setStatus(0);
        houseMapper.updateById(update);
    }

    /**
     * 实体 → 返回对象：解析图片 JSON、补封面
     */
    public HouseVO toVO(House house) {
        HouseVO vo = new HouseVO();
        vo.setId(house.getId());
        vo.setUserId(house.getUserId());
        vo.setTitle(house.getTitle());
        vo.setAddress(house.getAddress());
        vo.setLat(house.getLat());
        vo.setLng(house.getLng());
        vo.setArea(house.getArea());
        vo.setLayout(house.getLayout());
        vo.setPrice(house.getPrice());
        vo.setStatus(house.getStatus());
        vo.setCreateTime(house.getCreateTime());
        vo.setDistance(house.getDistance());

        List<String> images = parseImages(house.getImages());
        vo.setImages(images);
        vo.setCover(images.isEmpty() ? null : images.get(0));
        return vo;
    }

    /**
     * JSON 字符串 → 图片路径数组
     */
    private List<String> parseImages(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            // 数据异常时不要让整个接口挂掉
            return Collections.emptyList();
        }
    }

    /**
     * 图片路径数组 → JSON 字符串
     */
    private String toJson(List<String> images) {
        if (images == null || images.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            throw new BusinessException(500, "图片数据格式错误");
        }
    }
}