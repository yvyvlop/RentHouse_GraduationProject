package com.rental.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rental.server.entity.User;

/**
 * user 表 Mapper
 * 继承 BaseMapper 即拥有 insert / deleteById / updateById / selectById / selectList
 * 等常用方法，
 * 无需编写 XML。入口类的 @MapperScan("com.rental.server.mapper") 会自动扫描本接口。
 */
public interface UserMapper extends BaseMapper<User> {
}
