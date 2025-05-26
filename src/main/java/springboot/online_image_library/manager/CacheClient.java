package springboot.online_image_library.manager;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import springboot.online_image_library.exception.CacheException;
import springboot.online_image_library.exception.ErrorCode;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 多级缓存客户端，支持本地缓存（Caffeine）和 Redis 缓存，提供查询、失效和更新功能
 *
 * @author Yu'S'hui'shen
 */
@Component
@Slf4j
public class CacheClient {

    // 分布式锁前缀
    private static final String LOCK_PREFIX = "cache:lock:";
    // 空值占位符，用于缓存 null 结果
    private static final NullValue NULL_VALUE = NullValue.INSTANCE;
    // 本地缓存，使用 Caffeine 实现
    private final Cache<String, Object> localCache;
    // Redis 操作模板
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public CacheClient(Cache<String, Object> localCache, StringRedisTemplate redisTemplate) {
        this.localCache = localCache;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 查询缓存，按本地缓存 -> Redis -> 数据库的顺序查询，并回填缓存
     *
     * @param key        缓存键
     * @param type       返回数据类型（使用 TypeReference 支持泛型）
     * @param ttl        缓存生存时间
     * @param dbCallback 数据库查询回调函数
     * @param <R>        返回类型泛型
     * @return 查询结果
     * @throws CacheException 缓存操作相关异常
     */
    public <R> R queryWithCache(String key, TypeReference<R> type, Duration ttl, Supplier<R> dbCallback) throws CacheException {
        // 1. 查询本地缓存
        R result = getFromLocalCache(key, type);
        if (result != null || localCache.getIfPresent(key) == NULL_VALUE) {
            log.debug("[queryWithCache] 命中本地缓存，key={}", key);
            return result;
        }
        log.debug("[queryWithCache] 未命中本地缓存，key={}", key);

        // 2. 查询 Redis
        result = getFromRedis(key, type);
        if (result != null) {
            log.debug("[queryWithCache] 命中 Redis，key={}", key);
            return result;
        }
        log.debug("[queryWithCache] 未命中 Redis，key={}", key);

        // 3. 查询数据库并回填
        return loadFromDb(key, type, ttl, dbCallback);
    }

    /**
     * 从本地缓存获取数据
     *
     * @param key  缓存键
     * @param type 数据类型（TypeReference）
     * @param <R>  返回类型泛型
     * @return 缓存数据或 null
     */
    private <R> R getFromLocalCache(String key, TypeReference<R> type) {
        Object local = localCache.getIfPresent(key);
        if (local != null) {
            return local == NULL_VALUE ? null : JSONUtil.toBean(JSONUtil.toJsonStr(local), type.getType(), true);
        }
        return null;
    }

    /**
     * 从 Redis 获取数据并回填本地缓存
     *
     * @param key  缓存键
     * @param type 数据类型（TypeReference）
     * @param <R>  返回类型泛型
     * @return 缓存数据或 null
     * @throws CacheException Redis 操作异常
     */
    private <R> R getFromRedis(String key, TypeReference<R> type) throws CacheException {
        String redisValue = redisTemplate.opsForValue().get(key);
        if (CharSequenceUtil.isNotBlank(redisValue)) {
            try {
                R result = JSONUtil.toBean(redisValue, type.getType(), true);
                localCache.put(key, result == null ? NULL_VALUE : result);
                return result;
            } catch (Exception e) {
                throw new CacheException(ErrorCode.REDIS_DATA_DESERIALIZATION_FAILED);
            }
        }
        return null;
    }

    /**
     * 从数据库加载数据并回填缓存
     *
     * @param key        缓存键
     * @param type       数据类型（TypeReference）
     * @param ttl        缓存生存时间
     * @param dbCallback 数据库查询回调
     * @param <R>        返回类型泛型
     * @return 数据库查询结果
     * @throws CacheException 缓存回填异常
     */
    private <R> R loadFromDb(String key, TypeReference<R> type, Duration ttl, Supplier<R> dbCallback) throws CacheException {
        log.info("[queryWithCache] 未命中 Redis，开始查询数据库，key={}", key);
        String lockKey = LOCK_PREFIX + key;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(locked)) {
            try {
                // 再次检查 Redis
                R result = getFromRedis(key, type);
                if (result != null) {
                    return result;
                }
                // 查询数据库
                result = dbCallback.get();
                log.info("[loadFromDb] 数据库查询结果: {}", result);
                try {
                    // 序列化为 JSON 并存入 Redis 和本地缓存
                    redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(result != null ? result : NULL_VALUE), ttl);
                    localCache.put(key, result != null ? result : NULL_VALUE);
                } catch (Exception e) {
                    throw new CacheException(ErrorCode.REDIS_DATA_SERIALIZATION_FAILED);
                }
                return result;
            } finally {
                // 释放分布式锁
                redisTemplate.delete(lockKey);
            }
        } else {
            // 未获取锁，重试
            log.debug("[queryWithCache] 未获取分布式锁，重试，key={}", key);
            try {
                Thread.sleep(50);
                return queryWithCache(key, type, ttl, dbCallback);
            } catch (InterruptedException e) {
                log.warn("[queryWithCache] 重试中断，key={}", key);
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    /**
     * 失效缓存，同时清除本地缓存和 Redis 缓存
     *
     * @param key 缓存键
     */
    public void invalidate(String key) {
        log.debug("[invalidate] 删除本地缓存，key={}", key);
        localCache.invalidate(key);
        log.debug("[invalidate] 删除 Redis 缓存，key={}", key);
        redisTemplate.delete(key);
    }

    /**
     * 更新缓存，更新本地缓存和 Redis 缓存，并设置 TTL
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   缓存生存时间
     */
    public void update(String key, Object value, Duration ttl) {
        log.debug("[update] 更新本地缓存，key={}", key);
        localCache.put(key, value != null ? value : NULL_VALUE);
        log.debug("[update] 更新 Redis 缓存，key={}", key);
        try {
            redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value != null ? value : NULL_VALUE), ttl);
        } catch (Exception e) {
            throw new CacheException(ErrorCode.REDIS_DATA_SERIALIZATION_FAILED);
        }
    }

    private enum NullValue {
        INSTANCE
    }
}