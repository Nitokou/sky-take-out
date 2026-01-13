package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.dto.StatusDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    @Select("select setmeal.*,category.name as category_name " +
            "from setmeal LEFT JOIN category ON setmeal.category_id = category.id " +
            "where setmeal.id = #{id}")
    SetmealVO getById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    Page<SetmealVO> pageQuery(SetmealPageQueryDTO pageQueryDTO);

    void deleteByIds(List<Long> ids);

    @AutoFill(OperationType.INSERT)
    void InsertBatch(Setmeal setmeal);

    @Select("select * from setmeal where category_id = #{categoryId} and status = #{status}")
    List<Setmeal> getByCategoryId(Setmeal setmeal);

    @Select("select * from setmeal_dish LEFT JOIN dish ON dish_id = dish.id where setmeal_dish.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

    @Select("select * from setmeal where id = #{id}")
    Setmeal getByIdUser(Long id);

    List<StatusDTO> sumSetmealStatus();
}
