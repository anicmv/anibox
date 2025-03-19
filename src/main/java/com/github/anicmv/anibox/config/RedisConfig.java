package com.github.anicmv.anibox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author anicmv
 * @date 2025/2/28 10:06
 * @description redis配置类 专门存储二进制文件
 */
@Configuration
public class RedisConfig {

    @Bean("binaryRedisTemplate")
    public RedisTemplate<String, byte[]> binaryRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        // 设置专门用于二进制数据的序列化
        template.setValueSerializer(RedisSerializer.byteArray());
        return template;
    }
}