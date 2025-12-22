package com.sky.controller.admin;


import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
//import jdk.jpackage.internal.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api("菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){

        log.info("新增菜品:{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        String key = "dish_" + dishDTO.getCategoryId();

        cleanCache(key);

        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}", dishPageQueryDTO);
        PageResult res = dishService.pageQuery(dishPageQueryDTO);

        return Result.success(res);
    }

    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除");
        dishService.deleteBatch(ids);

//        清楚掉所有dish开头的key
        cleanCache("dish_*");

        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id来进行查询")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("更具Id来查询菜品详细信息:{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);

        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品以及口味:{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);

        //        清楚掉所有dish开头的key
        cleanCache("dish_*");

        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品停售")
    public Result stopOrStart(@PathVariable Integer status, @RequestParam Long id){
        log.info("菜品启停:{}, {}", status, id);
        dishService.stopOrStart(status, id);

        //        清楚掉所有dish开头的key
        cleanCache("dish_*");

        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("通过分类id查询菜品")
    public Result getByCategoryId(@RequestParam Long categoryId){
        log.info("通过分类id查询菜品:{}", categoryId);

        List<Dish> dishs = dishService.getByCategoryId(categoryId);
        return Result.success(dishs);
    }

    private void cleanCache(String patten){
        Set keys = redisTemplate.keys(patten);
        redisTemplate.delete(keys);
    }

}
