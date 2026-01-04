package com.sky.controller.user;


import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("UserOrderController")
@Slf4j
@RequestMapping("/user/order")
@Api("用户端订单相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单，参数为：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);

        return Result.success(orderSubmitVO);

    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详细")
    public Result<OrderVO> orderDetail(@PathVariable Long id){
        log.info("查询订单详细：{}", id);
        OrderVO OrderVO = orderService.queryOrderDetail(id);

        return Result.success(OrderVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("查询历史订单")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO){
//        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
//        ordersPageQueryDTO.setPage(page);
//        ordersPageQueryDTO.setPageSize(pageSize);
        PageResult pageResult = orderService.historyOrders(ordersPageQueryDTO);

        return Result.success(pageResult);
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id){

        orderService.cancelOrder(id);

        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("取消订单")
    public Result repetition(@PathVariable Long id){

        orderService.repetitionOrder(id);

        return Result.success();
    }



}
