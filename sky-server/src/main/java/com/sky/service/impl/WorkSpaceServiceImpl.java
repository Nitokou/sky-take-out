package com.sky.service.impl;

import com.sky.dto.StatusDTO;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @Autowired
    OrderMapper orderMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public BusinessDataVO getBusinessData(LocalDateTime dayStart, LocalDateTime dayEnd) {
//        获取今天系统时间
//        数据库查询今日

//    private Double turnover;//营业额
//
//    private Integer validOrderCount;//有效订单数
//
//    private Double orderCompletionRate;//订单完成率
//
//    private Double unitPrice;//平均客单价
//
//    private Integer newUsers;//新增用户数


//        获取新用户数
        HashMap map = new HashMap();
        map.put("begin", dayStart);
        map.put("end", dayEnd);

        Integer newUsers = Math.toIntExact(orderMapper.sumUserByMap(map));
        newUsers = (newUsers == null ? 0: newUsers);
//        全部订单数
        Integer totalOrderCount = orderMapper.sumOrderByMap(map);
        totalOrderCount = (totalOrderCount == null ? 0: totalOrderCount);

//        营业额
        map.put("status", Orders.COMPLETED);
        Double turnover = orderMapper.sumByMap(map);
        turnover = (turnover == null ? 0.0: turnover);

//        有效订单数
        Integer validOrderCount = orderMapper.sumOrderByMap(map);
        validOrderCount = (validOrderCount == null ? 0: validOrderCount);


//        订单完成率
        Double orderCompletionRate = (totalOrderCount == 0 ? 0.0 : (double)validOrderCount / totalOrderCount);

        Double unitPrice = (totalOrderCount == 0 ? 0.0 : (double)turnover / totalOrderCount);

        BusinessDataVO build = BusinessDataVO.builder()
                .newUsers(newUsers)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .turnover(turnover).unitPrice(unitPrice).build();


        return build;






    }

    @Override
    public OrderOverViewVO getOverviewOrders() {
//        得到一天的开始和结束
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayEnd =
                now.toLocalDate().atTime(LocalTime.MAX);
        LocalDateTime dayStart =
                now.toLocalDate().atTime(LocalTime.MIN);
        HashMap hashMap = new HashMap();
        hashMap.put("begin", dayStart);
        hashMap.put("end", dayEnd);
        List<StatusDTO> statusByMap = orderMapper.sumStatusByMap(hashMap);


//        Integer totalCount = statusByMap.values().stream().mapToInt(Integer::intValue).sum();

        Map<Integer, Integer> collect = statusByMap.stream().collect(Collectors.toMap(StatusDTO::getStatus, StatusDTO::getCnt));
        Integer totalCount = collect.values().stream().mapToInt(Integer::intValue).sum();
        OrderOverViewVO orderOverViewVO = OrderOverViewVO.builder()
                .cancelledOrders(collect.getOrDefault(Orders.CANCELLED,0))
                .completedOrders(collect.getOrDefault(Orders.COMPLETED,0))
                .deliveredOrders(collect.getOrDefault(Orders.DELIVERY_IN_PROGRESS,0))
                .waitingOrders(collect.getOrDefault(Orders.CONFIRMED,0))
                .allOrders(totalCount).build();




//        return orderOverViewVO;
        return orderOverViewVO;
    }

    @Override
    public DishOverViewVO getOverviewDishes() {

        //        得到一天的开始和结束

        List<StatusDTO> statusByMap = dishMapper.sumDishStatus();


//        Integer totalCount = statusByMap.values().stream().mapToInt(Integer::intValue).sum();

        Map<Integer, Integer> collect = statusByMap.stream().collect(Collectors.toMap(StatusDTO::getStatus, StatusDTO::getCnt));
////        Integer totalCount = collect.values().stream().mapToInt(Integer::intValue).sum();
        DishOverViewVO dishOverViewVO = DishOverViewVO.builder()
                .sold(collect.getOrDefault(1,0))
                .discontinued(collect.getOrDefault(0,0))
                .build();

//        return orderOverViewVO;
        return dishOverViewVO;
//        return null;
    }

    @Override
    public SetmealOverViewVO getOverviewSetmeals() {
        List<StatusDTO> statusByMap = setmealMapper.sumSetmealStatus();


//        Integer totalCount = statusByMap.values().stream().mapToInt(Integer::intValue).sum();

        Map<Integer, Integer> collect = statusByMap.stream().collect(Collectors.toMap(StatusDTO::getStatus, StatusDTO::getCnt));
////        Integer totalCount = collect.values().stream().mapToInt(Integer::intValue).sum();
        SetmealOverViewVO setmealOverViewVO = SetmealOverViewVO.builder()
                .sold(collect.getOrDefault(1,0))
                .discontinued(collect.getOrDefault(0,0))
                .build();

//        return orderOverViewVO;
        return setmealOverViewVO;
    }

}
