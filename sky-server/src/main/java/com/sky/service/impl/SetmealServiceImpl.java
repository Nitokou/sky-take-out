package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    @Transactional
    public void updateWithsetmealdish(SetmealDTO setmealDTO) {
//        新建一个Setmeal
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

//        动态更新条件
        setmealMapper.update(setmeal);

//        同步跟新setmealDish
//        首先是通过setmealId来删除 套餐中菜品的id
        setmealDishMapper.deleteBysetmealId(setmeal.getId());

//        获得setmealDishesID
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
//        表示当前有值
        if (setmealDishes != null && !setmealDishes.isEmpty()){
//            因此批量插入 首先需要设置setmealId
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
            setmealDishMapper.InsertBatch(setmealDishes);
        }

    }

    @Override
    public SetmealVO getById(Long id) {

        SetmealVO setmealVO = setmealMapper.getById(id);
//        查询到了有值
        if (setmealVO != null){
            List<SetmealDish> setmealDishes = setmealDishMapper.getsetmealDishesBySetmealId(id);
            setmealVO.setSetmealDishes(setmealDishes);
        }
        return setmealVO;
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO pageQueryDTO) {
//        开启分页功能
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

//        这个地方就通过pageQuery后查询
        Page<SetmealVO> page = setmealMapper.pageQuery(pageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void stopOrStart(Integer status, Long id) {
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);

        setmealMapper.update(setmeal);
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
//        主要涉及到两处
//        第一处是在setmeal中删除
        setmealMapper.deleteByIds(ids);
//        第二处是在setmeal_Dish中删除


    }

    @Override
    public void InsertBatch(SetmealDTO setmealDTO) {
//        有两个
        Long id = BaseContext.getCurrentId();
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        setmeal.setUpdateUser(id);
        setmeal.setCreateUser(id);
//        一个是插入setmeal
        setmealMapper.InsertBatch(setmeal);
//        另一个是插入到setmeal_dish

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()){
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
            setmealDishMapper.InsertBatch(setmealDishes);
        }

    }

    @Override
    public List<Setmeal> getByCategoryId(Setmeal setmeal) {
        return setmealMapper.getByCategoryId(setmeal);
    }

    @Override
    public List<DishItemVO> queryDishsBySetmealId(Long setmealId) {


        return setmealMapper.getDishItemBySetmealId(setmealId);
    }
}
