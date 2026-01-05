package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.controller.user.ShoppingCartController;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.SearchHttpSN;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class
OrderServiceImpl implements OrderService {

    @Autowired
    OrderMapper orderMapper;
    @Autowired
    ShoppingCartMapper shoppingCartMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    AddressBookMapper addressBookMapper;

    @Autowired
    SearchHttpSN searchHttpSN;

    @Value("${sky.shop.address}")
    String shopAddress;

    @Autowired
    UserMapper userMapper;

    @Autowired
    WebSocketServer webSocketServer;

    @Autowired
    private WeChatPayUtil weChatPayUtil;


    private static final double EARTH_RADIUS = 6371.0; // km

    @SuppressWarnings("unchecked")
    private static double[] extractLatLng(Map<String, Object> map) {
        Map<String, Object> result = (Map<String, Object>) map.get("result");
        Map<String, Object> location = (Map<String, Object>) result.get("location");

        double lng = ((Number) location.get("lng")).doubleValue();
        double lat = ((Number) location.get("lat")).doubleValue();

        return new double[]{lat, lng};
    }



    public static double distanceKm(
            double lat1, double lng1,
            double lat2, double lng2) {

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }


    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

//        处理各种异常(地址 和 购物车)
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

//        判断 地址是否超出距离
//        查询地址

        try {
            Map<String, Object> locationA = searchHttpSN.getLocation(
                    addressBook.getProvinceName()
                            + addressBook.getCityName()
                            + addressBook.getDistrictName()
                            + addressBook.getDetail()
            );

            Map<String, Object> locationB = searchHttpSN.getLocation(shopAddress);

            double[] p1 = extractLatLng(locationA);
            double[] p2 = extractLatLng(locationB);

            double distance = distanceKm(p1[0], p1[1], p2[0], p2[1]);

            if (distance > 5.0) {
                throw new OrderBusinessException("超出配送距离");
            }

        } catch (OrderBusinessException e) {
            throw e; // ✅ 业务异常原样抛出

        } catch (Exception e) {
            log.error("校验配送距离失败", e);
            throw new RuntimeException(MessageConstant.UNKNOWN_ERROR, e);
        }


        ShoppingCart shoppingCart = new ShoppingCart();
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

//        购物车
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

//      向订单表插入一条数据

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setUserId(currentId);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setOrderTime(LocalDateTime.now());
//        设置是由谁提交的
        orders.setUserId(currentId);

        orderMapper.insert(orders);
//        向订单明细表插入n条数据
//        对 stream（流）中的每个元素执行“处理逻辑”，并返回一个新的元素。
        List<OrderDetail> orderDetails = shoppingCartList.stream().map(cart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());  // 设置订单明细ID
            return orderDetail;
        }).collect(Collectors.toList());

        orderDetailMapper.insertBatch(orderDetails);
//
//        清空购物车
        shoppingCartMapper.clean(currentId);
//
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount()).build();


        return orderSubmitVO;
    }

    @Override
    public OrderVO queryOrderDetail(Long id) {
//        两步
//        第一步 查找 orders
        Orders orders = orderMapper.getById(id);
//        第二步 查找OrdersDetail
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());

//        组装
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);

        return orderVO;
    }

    @Override
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 设置分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
//        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
//        ordersPageQueryDTO.setStatus(status);

        // 分页条件查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList();

        // 查询出订单明细，并封装入OrderVO进行响应
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                Long orderId = orders.getId();// 订单id

                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);

//        return new PageResult(page.getTotal(), collect);
    }

    @Override
    public void cancelOrder(Long id) {
//        取消订单只要
//        将status设置为6 肯定是需要本人取消的
//        因此
//        Orders order = orderMapper.getById(id);
        Long currentId = BaseContext.getCurrentId();

//        if (order.getUserId() == null || !order.getUserId().equals(currentId)){
//            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
//        }

//        接下来就是更新账单信息
        Orders orders = new Orders();
        orders.setId(id);
        orders.setUserId(currentId);
        orders.setStatus(Orders.CANCELLED);

        orderMapper.update(orders);


    }

    @Override
    public void repetitionOrder(Long id) {
//        再来一单的逻辑
//        第一个 查你当前的订单id
//        保证当前order 中的订单和当前登录用户的一致
        Orders order = orderMapper.getById(id);
        Long currentId = BaseContext.getCurrentId();
        if (order == null || !order.getUserId().equals(currentId)){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
//        Long orderId = id;
//        查你当前的orderDetail
        List<OrderDetail> OrderDetails = orderDetailMapper.getByOrderId(id);
//
////        重新插入到orders中
//        order.setId(null);
//        orderMapper.insert(order);
//
////        换个order_id重新插入到orderDetail 中
//        List<OrderDetail> collect = OrderDetails.stream().peek(orderDetail -> {
//            orderDetail.setOrderId(order.getId());  // 插入新的orderId
//        }).collect(Collectors.toList());
//
////        插入orderDetail
//        orderDetailMapper.insertBatch(collect);

//        只要重新加入到购物车中就可以
        List<ShoppingCart> collect = OrderDetails.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(currentId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(collect);

    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());


        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        // 这里的OrderVo是继承至Order 因此 拥有Order所有的属性
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);

    }

    @Override
    public OrderStatisticsVO getStatistics() {

//        统计数量 2待接单 3已接单 4派送中
//        Map<Integer, Integer> statistics =
        OrderStatisticsVO orderStatisticsVO = orderMapper.statistics();
//        待派送 = 已接单
//        orderStatisticsVO.setToBeConfirmed(statistics.getOrDefault(Orders.TO_BE_CONFIRMED, 0)); //2
//        orderStatisticsVO.setConfirmed(statistics.getOrDefault(Orders.CONFIRMED, 0)); //3
//        orderStatisticsVO.setDeliveryInProgress(statistics.getOrDefault(Orders.DELIVERY_IN_PROGRESS, 0)); //


        return orderStatisticsVO;
    }

    @Override
    @Transactional
    public void rejection(OrdersRejectionDTO rejectionDTO) {
//        直接将原因和 id 写入
        Orders orders = new Orders();
        orders.setRejectionReason(rejectionDTO.getRejectionReason());
        orders.setId(rejectionDTO.getId());

//        同时需要将已接单取消
        orders.setStatus(Orders.CANCELLED);

        orderMapper.update(orders);
    }

    @Override
    public void confirm(Orders orders) {
//        Orders orders = new Orders();
//        orders.setRejectionReason(rejectionDTO.getRejectionReason());
//        orders.setId(id);

        orders.setStatus(Orders.CONFIRMED);

        orderMapper.update(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO cancelDTO) {
//        拒单 就需要把订单状态改为 6 并把取消原因写入

        Orders orders = new Orders();
//        Orders orders = new Orders();
        orders.setId(cancelDTO.getId());
        orders.setCancelReason(cancelDTO.getCancelReason());
//        设置状态
        orders.setStatus(Orders.CANCELLED);

        orderMapper.update(orders);


    }

    @Override
    public void complete(Long id) {
        Orders orders = new Orders();

        orders.setId(id);

        orders.setStatus(Orders.COMPLETED);

        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders orders = new Orders();

        orders.setId(id);

        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
//        return null;
    }

    @Override
    public void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);

//        通过websocket 向客户端推送数据
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);

    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();

        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);

                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList 这一部分可以优化
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }
}
