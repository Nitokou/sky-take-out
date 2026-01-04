package com.sky.controller.admin;


import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController("AdminOrderController")
//@RestController，SpringBoot 自动启用了 JSON 序列化
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "管理员订单管理")
public class OrdersController {
    @Autowired
    OrderService orderService;


    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info(" 订单查询 {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

//    订单统计
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO OrderStatisticsVO = orderService.getStatistics();
        return Result.success(OrderStatisticsVO);
    }

    @GetMapping("/details/{id}")
    public Result<OrderVO> queryDetail(@PathVariable Long id){
        OrderVO orderVO = orderService.queryOrderDetail(id);
        return Result.success(orderVO);
    }

    @PutMapping("/rejection")
    public Result rejection(@RequestBody OrdersRejectionDTO rejectionDTO){
        orderService.rejection(rejectionDTO);
        return Result.success();
    }

    @PutMapping("/confirm")
    public Result confirm(@RequestBody Orders orders){
        orderService.confirm(orders);
        return Result.success();
    }

    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO cancelDTO){
        orderService.cancel(cancelDTO);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id){
        orderService.complete(id);
        return Result.success();
    }

    @PutMapping("delivery/{id}")
    public Result delivery(@PathVariable Long id){
        orderService.delivery(id);
        return Result.success();
    }







}
