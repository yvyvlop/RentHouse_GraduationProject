package com.rental.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rental.server.entity.House;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * house 表 Mapper
 * 继承 BaseMapper 拥有常用 CRUD 方法；附近查询和关键词搜索在 XML 中手写 SQL
 */
public interface HouseMapper extends BaseMapper<House> {

    /**
     * 附近房源：矩形粗筛 + Haversine 精算排序
     * 矩形边界由 Service 根据半径计算后传入
     */
    List<House> selectNearby(@Param("lat") double lat,
                             @Param("lng") double lng,
                             @Param("minLat") double minLat,
                             @Param("maxLat") double maxLat,
                             @Param("minLng") double minLng,
                             @Param("maxLng") double maxLng,
                             @Param("radius") double radius,
                             @Param("limit") int limit);

    /**
     * 关键词搜索（标题/地址模糊匹配），传经纬度时按距离升序
     */
    List<House> search(@Param("keyword") String keyword,
                       @Param("lat") Double lat,
                       @Param("lng") Double lng,
                       @Param("limit") int limit);
}