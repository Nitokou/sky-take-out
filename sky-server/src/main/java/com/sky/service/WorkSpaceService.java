package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface WorkSpaceService {

    BusinessDataVO getBusinessData(LocalDateTime dayStart, LocalDateTime dayEnd);

    OrderOverViewVO getOverviewOrders();

    DishOverViewVO getOverviewDishes();

    SetmealOverViewVO getOverviewSetmeals();
}
