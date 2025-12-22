package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {


        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

//        向菜品表加入1条数据
        dishMapper.insert(dish);

//        注意这里的操作比较多 上面的是插入后生成了主键 并将主键的值赋值给了dish
        Long id = dish.getId();

//        向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && !flavors.isEmpty()){
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(id));

            dishFlavorMapper.insertBatch(flavors);
        }


    }

//    菜品分页查询
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
//        是否存在起售中菜品
        for (Long id: ids){
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

//        判断是否被当前的套餐被关联

        List<Long> setmealIds =  setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

//        删除彩屏中的菜品数据

//        for (Long id: ids){
//            dishMapper.deleteById(id);
//            //        删除口味数据
//            dishFlavorMapper.deleteByDishId(id);
//        }
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);

    }


    @Override
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();

        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
//        BeanUtils.copyProperties(dishFlavors, dishVO);
        return dishVO;
    }

    /**
     * 更新菜品
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        List<DishFlavor> flavors = dishDTO.getFlavors();


        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishDTO.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    @Override
    public void stopOrStart(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);

        dishMapper.update(dish);

    }

    @Override
    public List<Dish> getByCategoryId(Long categoryId) {
        List<Dish> dishs = dishMapper.getByCategoryId(categoryId);

        return dishs;
    }


    public List<DishVO> getDishesByCategoryId(Long categoryId) {
        List<Dish> dishes = dishMapper.getByCategoryId(categoryId);
        if (dishes == null || dishes.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 提取 dishId
        List<Long> dishIds = dishes.stream()
                .map(Dish::getId)
                .collect(Collectors.toList());

        // 2. 一次性查所有口味
        List<DishFlavor> flavors = dishFlavorMapper.getByDishIds(dishIds);

        // 3. 按 dishId 分组
        Map<Long, List<DishFlavor>> flavorMap = flavors.stream()
                .collect(Collectors.groupingBy(DishFlavor::getDishId));

        // 4. 组装 VO
        return dishes.stream().map(dish -> {
            DishVO vo = new DishVO();
            BeanUtils.copyProperties(dish, vo);
            vo.setFlavors(flavorMap.getOrDefault(dish.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DishVO> listWithFlavor(Dish query) {
        List<Dish> dishes = dishMapper.getByCategoryIdWithStatus(query);
        if (dishes == null || dishes.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 提取 dishId
        List<Long> dishIds = dishes.stream()
                .map(Dish::getId)
                .collect(Collectors.toList());

        // 2. 一次性查所有口味
        List<DishFlavor> flavors = dishFlavorMapper.getByDishIds(dishIds);

        // 3. 按 dishId 分组
        Map<Long, List<DishFlavor>> flavorMap = flavors.stream()
                .collect(Collectors.groupingBy(DishFlavor::getDishId));

        // 4. 组装 VO
        return dishes.stream().map(dish -> {
            DishVO vo = new DishVO();
            BeanUtils.copyProperties(dish, vo);
            vo.setFlavors(flavorMap.getOrDefault(dish.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());

    }


}
