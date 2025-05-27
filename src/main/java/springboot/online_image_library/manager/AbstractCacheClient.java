package springboot.online_image_library.manager;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import springboot.online_image_library.exception.CacheException;
import springboot.online_image_library.exception.ErrorCode;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author Yu'S'hui'shen
 */
@Slf4j
@Component
public abstract class AbstractCacheClient {

    private static final String LOCK_PREFIX = "cache:lock:";
    private static final NullValue NULL_VALUE = NullValue.INSTANCE;
    protected final StringRedisTemplate redisTemplate;
    protected AbstractCacheClient(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <R> R query(String key, TypeReference<R> type, Duration ttl, Supplier<R> dbCallback) throws CacheException {
        // 1. 查询本地缓存（由子类决定是否使用）
        R result = getFromLocalCache(key, type);
        if (result != null) {
            log.debug("[query] 命中本地缓存，key={}", key);
            return result;
        }

        // 2. 查询 Redis 缓存
        result = getFromRedis(key, type);
        if (result != null) {
            log.debug("[query] 命中 Redis 缓存，key={}", key);
            saveToLocalCache(key, result, ttl);
            return result;
        }

        // 3. 加锁并回源数据库
        return loadFromDb(key, type, ttl, dbCallback);
    }

    private <R> R getFromRedis(String key, TypeReference<R> type) {
        String json = redisTemplate.opsForValue().get(key);
        if (CharSequenceUtil.isNotBlank(json)) {
            try {
                return JSONUtil.toBean(json, type.getType(), true);
            } catch (Exception e) {
                throw new CacheException(ErrorCode.REDIS_DATA_DESERIALIZATION_FAILED);
            }
        }
        return null;
    }

    private <R> R loadFromDb(String key, TypeReference<R> type, Duration ttl, Supplier<R> dbCallback) {
        String lockKey = LOCK_PREFIX + key;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(locked)) {
            try {
                R result = getFromRedis(key, type);
                if (result != null) {
                    saveToLocalCache(key, result, ttl);
                    return result;
                }

                result = dbCallback.get();
                String jsonStr = JSONUtil.toJsonStr(result != null ? result : NULL_VALUE);
                redisTemplate.opsForValue().set(key, jsonStr, ttl);
                saveToLocalCache(key, result, ttl);
                return result;
            } catch (Exception e) {
                throw new CacheException(ErrorCode.REDIS_DATA_SERIALIZATION_FAILED, e.getMessage());
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            try {
                Thread.sleep(50);
                return query(key, type, ttl, dbCallback);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    public void invalidate(String key) {
        redisTemplate.delete(key);
        invalidateLocalCache(key);
        log.debug("[invalidate] 缓存失效：{}", key);
    }

    public void update(String key, Object value, Duration ttl) {
        String json = JSONUtil.toJsonStr(value != null ? value : NULL_VALUE);
        redisTemplate.opsForValue().set(key, json, ttl);
        saveToLocalCache(key, value, ttl);
    }

    protected abstract <R> void saveToLocalCache(String key, R value, Duration ttl);

    protected abstract <R> R getFromLocalCache(String key, TypeReference<R> type);

    protected abstract void invalidateLocalCache(String key);

    protected enum NullValue {
        INSTANCE
    }
}
