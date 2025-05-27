package springboot.online_image_library.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 多级缓存客户端，支持Redis 缓存，提供查询、失效和更新功能
 *
 * @author Yu'S'hui'shen
 */
@Component
@Slf4j
public class CacheClientNoLocal extends AbstractCacheClient {

    public CacheClientNoLocal(StringRedisTemplate redisTemplate) {
        super(redisTemplate);
    }

    @Override
    protected <R> void saveToLocalCache(String key, R value, Duration ttl) {
        // 无本地缓存
    }

    @Override
    protected <R> R getFromLocalCache(String key, TypeReference<R> type) {
        return null;
    }

    @Override
    protected void invalidateLocalCache(String key) {
        // 无本地缓存
    }

}
