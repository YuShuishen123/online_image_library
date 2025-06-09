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
        // 获取缓存数据
        String json = redisTemplate.opsForValue().get(key);
        // 如果获取的数据非空
        if (CharSequenceUtil.isNotBlank(json)) {
            try {
                // 把获取到的结果反序列化成对象并返回
                return JSONUtil.toBean(json, type.getType(), true);
            } catch (Exception e) {
                log.warn("[getFromRedis] Redis 数据反序列化失败，key={}, error={}", key, e.getMessage());
                throw new CacheException(ErrorCode.REDIS_DATA_DESERIALIZATION_FAILED);
            }
        }
        return null;
    }

    private <R> R loadFromDb(String key, TypeReference<R> type, Duration ttl, Supplier<R> dbCallback) {
        String lockKey = LOCK_PREFIX + key;
        // 最大重试次数
        int maxAttempts = 5;
        // 初始睡眠时间
        int baseSleepMs = 50;
        // 循环尝试获取锁
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                try {
                    // 再次尝试从Redis获取，防止并发下缓存刚被别人设置
                    R result = getFromRedis(key, type);
                    if (result != null) {
                        saveToLocalCache(key, result, ttl);
                        return result;
                    }

                    // 从数据库获取
                    result = dbCallback.get();

                    // 空值处理，防止缓存击穿
                    String jsonStr = JSONUtil.toJsonStr(result != null ? result : NULL_VALUE);
                    redisTemplate.opsForValue().set(key, jsonStr, ttl);

                    // 保存到本地缓存
                    saveToLocalCache(key, result, ttl);
                    return result;
                } catch (Exception e) {
                    throw new CacheException(ErrorCode.REDIS_DATA_SERIALIZATION_FAILED, e.getMessage());
                } finally {
                    redisTemplate.delete(lockKey);
                }
            }
            // 未获得锁，指数退避后重试
            try {
                /*
                  位运算：将数字 1 左移 attempt 位
                      attempt = 0：1L << 0 = 01  = 1
                      attempt = 1：1L << 1 = 10  = 2
                      attempt = 2：1L << 2 = 100 = 4
                      attempt = 3：1L << 3 = 1000= 8
                 */
                long sleepTime = baseSleepMs * (1L << attempt);
                log.debug("[query] 第 {} 次未获取锁，等待 {}ms 后重试，key={}", attempt + 1, sleepTime, key);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                log.warn("[query] 获取锁被中断，key={}", key);
                Thread.currentThread().interrupt();
                return null;
            }
        }

        log.warn("[query] 超过最大重试次数仍未获取锁，放弃获取，key={}", key);
        return null;
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
