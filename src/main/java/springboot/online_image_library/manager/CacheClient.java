package springboot.online_image_library.manager;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存客户端，支持本地缓存（Caffeine）和 Redis 缓存，提供查询、失效和更新功能
 * @author Yu'S'hui'shen
 */
@Slf4j
@Component
public class CacheClient extends AbstractCacheClient {

    private final Cache<String, String> localCache;

    public CacheClient(StringRedisTemplate redisTemplate) {
        super(redisTemplate);
        this.localCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    @Override
    protected <R> void saveToLocalCache(String key, R value, Duration ttl) {
        localCache.put(key, JSONUtil.toJsonStr(value));
    }

    @Override
    protected <R> R getFromLocalCache(String key, TypeReference<R> type) {
        String json = localCache.getIfPresent(key);
        if (CharSequenceUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type.getType(), true);
        }
        return null;
    }

    @Override
    protected void invalidateLocalCache(String key) {
        localCache.invalidate(key);
    }
}

