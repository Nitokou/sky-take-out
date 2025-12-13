package com.sky.controller.admin;


import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.EmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@RestController，SpringBoot 自动启用了 JSON 序列化
@RequestMapping("/admin/category")
@Slf4j
@Api(tags = "分类相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PutMapping
    @ApiOperation("修改分类操作")
    public Result editCategory(@RequestBody CategoryDTO categoryDto){
//        传入的是id name sort type
        log.info("修改分类操作， 参数为：{}", categoryDto);
        categoryService.update(categoryDto);

        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分类页面查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分类分页查询， 参数为：{}", categoryPageQueryDTO);
        PageResult pageResult =  categoryService.pageQuery(categoryPageQueryDTO);

        return Result.success(pageResult);

    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    public Result startOrStop(@PathVariable("status") Integer status, @RequestParam Long id){
        log.info("启用禁用分类， 参数为：{}, {}", status, id);
        categoryService.startOrStop(status, id);

        return Result.success();
    }

    @PostMapping
    @ApiOperation("新增分类")
    public Result addCategory(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类， 参数为：{}", categoryDTO);
        categoryService.addCategory(categoryDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("删除分类")
    public Result deleteCategory(Long id){
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("按类型查询")
    public Result queryTypeCategory(@RequestParam(required = false) Integer type){
        List<Category> categoryList =  categoryService.queryTypeCategory(type);
        return Result.success(categoryList);
    }


}
