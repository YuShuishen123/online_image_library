package springboot.online_image_library.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Yu'S'hui'shen
 * @description Redission分布式锁配置类
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://124.222.132.163:6379")
                .setPassword("mzyudada")
                .setDatabase(0)
                .setConnectionPoolSize(50)
                .setConnectionMinimumIdleSize(10);

        return Redisson.create(config);
    }
}
