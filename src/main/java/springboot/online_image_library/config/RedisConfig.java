package springboot.online_image_library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author Yu'S'hui'shen
 * @description Redis配置类
 */
@Configuration
public class RedisConfig {
    /**
     * 定义了一个RedisTemplate Bean，用于操作Redis数据库。
     * 设置了Redis连接工厂、键和值的序列化方式。
     *
     * @param factory Redis连接工厂
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        // 设置连接工厂，使RedisTemplate能够与Redis服务器通信
        redisTemplate.setConnectionFactory(factory);
        // 设置键的序列化方式为StringRedisSerializer，保证键以字符串形式存储
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 设置值的序列化方式为StringRedisSerializer，保证值以字符串形式存储
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}