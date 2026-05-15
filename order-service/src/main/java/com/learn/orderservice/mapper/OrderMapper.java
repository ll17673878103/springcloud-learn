package com.learn.orderservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learn.orderservice.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper
 *
 * @author MangoPie
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    // 继承 BaseMapper 后，自动拥有 insert、update、delete、select 等方法
}
