package com.github.anicmv.anibox;

import jakarta.annotation.Resource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;


/**
 * @author anicmv
 * @date 2025/2/27 15:30
 * @description 入口类
 */
@SpringBootApplication
@MapperScan("com.github.anicmv.anibox.mapper")
public class AniBoxApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AniBoxApplication.class, args);
    }

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 初始化redis 不乱码
     */
    @Override
    public void run(String... args) {
        //redis的key采用string进行序列化
        redisTemplate.setKeySerializer(RedisSerializer.string());
        //redis的hashKey采用string进行序列化
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        //redis的hashValue采用string进行序列化
        redisTemplate.setHashValueSerializer(RedisSerializer.string());
        //如果放入redis的时候是一个对象，那么建议采用json序列化
        redisTemplate.setValueSerializer(RedisSerializer.json());
    }

}
