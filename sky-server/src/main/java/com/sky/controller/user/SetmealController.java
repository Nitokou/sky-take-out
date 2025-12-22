package com.sky.controller.user;


import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Api("用户套餐相关")
public class SetmealController {


    @Autowired
    SetmealService setmealService;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    @Cacheable(cacheNames = "setmealCache", key = "#categoryId") // setmealCache::categoryId
    public Result<List<Setmeal>> querySetmealByCategoryId(@RequestParam  Long categoryId){

        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(StatusConstant.ENABLE);
        setmeal.setCategoryId(categoryId);

        List<Setmeal> lists = setmealService.getByCategoryId(setmeal);
        return Result.success(lists);

    }
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐Id查询包含的菜品")
    public Result<List<DishItemVO>> queryDishsBySetmealId(@PathVariable(name = "id") Long setmealId){

        List<DishItemVO> lists = setmealService.queryDishsBySetmealId(setmealId);
        return Result.success(lists);

    }


}
