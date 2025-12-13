package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j  // 这个是一个日志管理依赖的插件
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public void update(CategoryDTO categoryDto) {
//        将DTO 转换为实体类
        Category category = new Category();
//        属性拷贝
        BeanUtils.copyProperties(categoryDto, category);
//        设置更新人以及更新时间
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.update(category);
    }

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {

        //        动态帮你计算查询, 通过拦截器来进行 修改你下一步的调参结果 实际是就是自动帮我们加如Limt
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

//        构建page对象
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);

        long total = page.getTotal();

        List<Category> result = page.getResult();

        log.info(result.toString());

//        返回pageResult
        return new PageResult(total, result);
    }

    @Override
    public void startOrStop(Integer status, Long id){
//        新建一个分类对象
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
//        放入数据库中进行操作
        categoryMapper.update(category);
    }

    @Override
    public void addCategory(CategoryDTO categoryDTO){
        Category category = new Category();

//        复制属性
        BeanUtils.copyProperties(categoryDTO, category);

        category.setStatus(1);
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateTime(LocalDateTime.now());

        category.setUpdateUser(BaseContext.getCurrentId());
        category.setCreateUser(BaseContext.getCurrentId());

        categoryMapper.add(category);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryMapper.delete(id);
    }

    @Override
    public List<Category> queryTypeCategory(Integer type) {

        return categoryMapper.queryTypeCategory(type);
    }


}
