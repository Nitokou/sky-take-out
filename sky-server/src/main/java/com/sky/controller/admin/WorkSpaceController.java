package com.sky.controller.admin;

import com.sky.entity.OrderDetail;
import com.sky.result.Result;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/workspace")
@Api("工作台相关接口")
public class WorkSpaceController {

    @Autowired
    private WorkSpaceService workSpaceService;

    @GetMapping("/businessData")
    @ApiOperation("今日运营数据")
    public Result<BusinessDataVO> businessData(){
        log.info("今日运营数据查询");
        return Result.success(workSpaceService.getBusinessData());
    }

    @GetMapping("/overviewOrders")
    @ApiOperation("今日运营数据")
    public Result<OrderOverViewVO> overviewOrders(){
        log.info("查询订单管理数据");
        return Result.success(workSpaceService.getOverviewOrders());
    }

    @GetMapping("/overviewDishes")
    @ApiOperation("今日运营数据")
    public Result<DishOverViewVO> overviewDishes(){
        log.info("查询菜品总览");
        return Result.success(workSpaceService.getOverviewDishes());
    }

    @GetMapping("/overviewSetmeals")
    @ApiOperation("今日运营数据")
    public Result<SetmealOverViewVO> overviewSetmeals(){
        log.info("查询菜品总览");
        return Result.success(workSpaceService.getOverviewSetmeals());
    }

}
