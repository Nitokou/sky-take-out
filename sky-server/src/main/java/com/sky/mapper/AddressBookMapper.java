package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    @Insert("insert into " +
            "address_book(user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default)" +
            "values (#{userId}, #{consignee}, #{sex}, #{phone}, #{provinceCode}, #{provinceName}, #{cityCode}, #{cityName}, #{districtCode}, #{districtName}, #{detail}, #{label}, #{isDefault})")
    void add(AddressBook addressBook);

    @Select("select * from address_book where user_id = #{currentId}")
    List<AddressBook> list(Long currentId);

    @Select("select * from address_book where user_id = #{currentId} and is_default = 1")
    AddressBook defaultAddress(Long currentId);

    void update(AddressBook id);

    @Update("update address_book set is_default = 0 where user_id = #{userId}")
    void cancelDefault(AddressBook address);

    @Select("select * from address_book where id =#{id} and user_id = #{userId}")
    AddressBook getAddress(AddressBook build);

    @Select("select * from address_book where address_book.id = #{addressBookId}")
    AddressBook getById(Long addressBookId);
}
