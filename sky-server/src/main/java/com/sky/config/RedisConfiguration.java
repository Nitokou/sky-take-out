package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {
    @Bean
    public RedisTemplate redisTemplate(LettuceConnectionFactory redisConnectionFactory){
        log.info("开始创建redis模板类");

        RedisTemplate redisTemplate = new RedisTemplate();

//        设置连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);

//      设置String序列化器 不设置序列化器会导致图形化界面ReDis时并不直观
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
