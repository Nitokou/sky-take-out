package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    @Select("select * from orders where id =#{id}")
    Orders getById(Long id);
    @Select("select * from orders where user_id =#{userId}")
    Page<OrdersDTO> getByUserId(Long userId);

//    Page<Orders> getPageResult(OrdersPageQueryDTO ordersPageQueryDTO);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    void update(Orders orders);

//    @MapKey("delivery_status")
    OrderStatisticsVO statistics();
}
