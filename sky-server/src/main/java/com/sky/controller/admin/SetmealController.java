package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api("套餐管理相关")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @PutMapping
    @ApiOperation("修改套餐内容")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result<SetmealDTO> update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐内容 ：{}", setmealDTO);
        setmealService.updateWithsetmealdish(setmealDTO);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据id查询套餐 ：{}", id);
        SetmealVO date = setmealService.getById(id);

        return Result.success(date);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
//    DTO传输数据不要添加@RequestParam
    public Result<PageResult> getById(SetmealPageQueryDTO pageQueryDTO){
        log.info("分页查询 ：{}", pageQueryDTO);
        PageResult date = setmealService.pageQuery(pageQueryDTO);
        return Result.success(date);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result stopOrStart(@PathVariable Integer status, @RequestParam Long id){
        log.info("套餐起售 ：{}", id);
        setmealService.stopOrStart(status, id);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("删除套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除套餐 ：{}", ids);
        setmealService.deleteByIds(ids);

        return Result.success();
    }

    @PostMapping
    @ApiOperation("添加套餐")
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("添加套餐 ：{}", setmealDTO);
        setmealService.InsertBatch(setmealDTO);

        return Result.success();
    }


}
