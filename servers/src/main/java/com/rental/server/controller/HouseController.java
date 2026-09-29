package com.rental.server.controller;

import com.rental.server.common.BusinessException;
import com.rental.server.common.Result;
import com.rental.server.entity.House;
import com.rental.server.service.HouseService;
import com.rental.server.vo.HouseVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房源接口
 * 需要登录的接口由 JWT 拦截器注入 userId；详情接口游客可访问
 */
@RestController
@RequestMapping("/api/house")
public class HouseController {

    private final HouseService houseService;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    /**
     * 发布房源
     */
    @PostMapping
    public Result<Long> publish(HttpServletRequest request, @RequestBody House house) {
        Long userId = (Long) request.getAttribute("userId");
        validate(house);

        // 没传标题时自动生成一个，保证列表卡片有内容显示
        if (house.getTitle() == null || house.getTitle().isBlank()) {
            house.setTitle(house.getLayout() + " · " + house.getArea() + "㎡");
        }

        Long id = houseService.publish(userId, house);
        return Result.success(id);
    }

    /**
     * 我发布的房源列表
     */
    @GetMapping("/mine")
    public Result<List<HouseVO>> listMine(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(houseService.listMine(userId));
    }

    /**
     * 附近房源（游客可访问）：按距离升序返回
     *
     * @param lat    当前纬度
     * @param lng    当前经度
     * @param radius 搜索半径（公里），默认 5
     * @param limit  最多返回条数，默认 50
     */
    @GetMapping("/nearby")
    public Result<List<HouseVO>> nearby(@RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radius,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(houseService.nearby(lat, lng, radius, limit));
    }

    /**
     * 关键词搜索（游客可访问）：标题或地址模糊匹配
     * 传了经纬度则按距离升序
     */
    @GetMapping("/search")
    public Result<List<HouseVO>> search(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(houseService.search(keyword, lat, lng, limit));
    }

    /**
     * 房源详情（含房东信息）
     */
    @GetMapping("/detail/{id}")
    public Result<HouseVO> detail(@PathVariable Long id) {
        return Result.success(houseService.getDetail(id));
    }

    /**
     * 编辑房源
     */
    @PutMapping("/{id}")
    public Result<Void> update(HttpServletRequest request, @PathVariable Long id, @RequestBody House house) {
        Long userId = (Long) request.getAttribute("userId");
        validate(house);
        houseService.update(userId, id, house);
        return Result.success();
    }

    /**
     * 下架房源（软删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        houseService.delete(userId, id);
        return Result.success();
    }

    /**
     * 发布/编辑时的必填校验
     */
    private void validate(House house) {
        if (house.getAddress() == null || house.getAddress().isBlank()) {
            throw new BusinessException(400, "请填写详细地址");
        }
        if (house.getLat() == null || house.getLng() == null) {
            throw new BusinessException(400, "请选择地址定位");
        }
        if (house.getArea() == null) {
            throw new BusinessException(400, "请填写房间大小");
        }
        if (house.getLayout() == null || house.getLayout().isBlank()) {
            throw new BusinessException(400, "请填写房间格局");
        }
        if (house.getPrice() == null) {
            throw new BusinessException(400, "请填写租金");
        }
        if (house.getImageList() == null || house.getImageList().isEmpty()) {
            throw new BusinessException(400, "请上传至少 1 张房间图片");
        }
    }
}