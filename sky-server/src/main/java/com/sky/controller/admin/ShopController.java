package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

//这里其实有冲突
@RestController("adminShopController")
@Api("商店相关接口")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {

    public static String SHOP_STATUS = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    @PutMapping("/{status}")
    @ApiOperation("设置商店状态")
    public Result setShopStatus(@PathVariable Integer status){
        log.info("设置营业状态为, {}", status==1 ? "营业中": "打烊了");
//        这个状态其实存在Redis 上最好
        redisTemplate.opsForValue().set(SHOP_STATUS, status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("查询营业状态")
    public Result<Integer> queryShopStatus(){

//        强转
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS);
        return Result.success(status);
    }




}
