package com.sky.service;

import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    OrderVO queryOrderDetail(Long id);

    PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    void cancelOrder(Long id);

    void repetitionOrder(Long id);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO getStatistics();

    void rejection(OrdersRejectionDTO rejectionDTO);

    void confirm(Orders orders);

    void cancel(OrdersCancelDTO cancelDTO);

    void complete(Long id);

    void delivery(Long id);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    void paySuccess(String outTradeNo);

    void reminder(Long id);
}
