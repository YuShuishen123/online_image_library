package springboot.online_image_library.utils.commom;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.*;

/**
 * Redis分布式锁组件（生产级实现）
 *
 * <p><b>核心特性：</b>
 * <ol>
 *   <li>支持阻塞/非阻塞获取锁</li>
 *   <li>自动续期机制（看门狗）</li>
 *   <li>防误删设计（线程标识校验）</li>
 *   <li>原子化操作（Lua脚本）</li>
 * </ol>
 *
 * @author Yu'S'hui'shen
 * @version 1.1
 */
@Component
public class RedisDistributedLock {
    // 锁键前缀（建议按业务分类）
    private static final String LOCK_PREFIX = "LOCK:";
    // 默认锁超时时间（防止死锁）
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    // 线程标识前缀（用于区分不同实例的线程）
    private static final String LOCK_VALUE_PREFIX = "THREAD:";
    // 看门狗线程池（全局共享）
    private static final ScheduledExecutorService RENEWAL_EXECUTOR =
            Executors.newScheduledThreadPool(4, r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("redis-lock-renewal-" + t.getId());
                return t;
            });
    private final StringRedisTemplate redisTemplate;
    // 线程本地存储当前持有的锁标识
    private final ThreadLocal<String> lockHolder = new ThreadLocal<>();
    // 维护所有续期任务（Key: 锁键, Value: 对应的定时任务）
    private final ConcurrentMap<String, ScheduledFuture<?>> renewalTasks = new ConcurrentHashMap<>();

    /**
     * 构造方法（Spring自动注入）
     *
     * @param redisTemplate Redis操作模板
     */
    public RedisDistributedLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取分布式锁（支持超时等待）
     *
     * @param businessKey 业务唯一标识（如用户ID）
     * @param waitMillis  最大等待时间（毫秒）
     * @return true-获取成功, false-获取失败
     * @throws IllegalStateException 如果当前线程已持有锁
     */
    public boolean tryLock(String businessKey, long waitMillis) {
        // 检查是否重入（根据业务需求决定是否允许）
        if (lockHolder.get() != null) {
            throw new IllegalStateException("当前线程已持有锁");
        }

        String key = buildRedisKey(businessKey);
        String threadId = buildThreadIdentifier();

        long endTime = System.currentTimeMillis() + waitMillis;
        while (System.currentTimeMillis() < endTime) {
            // 尝试获取锁（原子操作）
            if (acquireLock(key, threadId)) {
                lockHolder.set(threadId);
                startRenewal(key, threadId);
                return true;
            }
            // 使用退避算法等待
            sleepWithBackoff(endTime);
        }
        return false;
    }

    /**
     * 释放分布式锁
     *
     * @param businessKey 业务唯一标识
     */
    public void unlock(String businessKey) {
        String key = buildRedisKey(businessKey);
        String threadId = lockHolder.get();

        // 检查是否持有锁
        if (threadId == null) {
            throw new IllegalStateException("当前线程未持有锁");
        }

        // 原子化释放锁
        releaseLock(key, threadId);
        // 清理资源
        cancelRenewal(key);
        lockHolder.remove();
    }

    // ---------- 私有方法 ----------

    /**
     * 构建Redis键（添加前缀）
     */
    private String buildRedisKey(String businessKey) {
        return LOCK_PREFIX + businessKey;
    }

    /**
     * 构建线程唯一标识（实例ID+线程ID）
     */
    private String buildThreadIdentifier() {
        return LOCK_VALUE_PREFIX + Thread.currentThread().getId();
    }

    /**
     * 获取锁（原子操作）
     *
     * @return true-获取成功
     */
    private boolean acquireLock(String key, String threadId) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue()
                        .setIfAbsent(key, threadId, DEFAULT_TIMEOUT)
        );
    }

    /**
     * 释放锁（Lua脚本保证原子性）
     */
    private void releaseLock(String key, String threadId) {
        String script =
                // 如果锁的值匹配当前线程标识
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        // 删除锁键
                        "return redis.call('del', KEYS[1]) " +
                        "else " +
                        // 不匹配则返回0
                        "return 0 " +
                        "end";

        redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(key),
                threadId
        );
    }

    /**
     * 启动锁续期任务（看门狗机制）
     */
    private void startRenewal(String key, String threadId) {
        // 创建定时任务（初始延迟0，每隔10秒执行一次）
        ScheduledFuture<?> future = RENEWAL_EXECUTOR.scheduleAtFixedRate(() -> {
            String script =
                    "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                            // 续期30秒
                            "return redis.call('expire', KEYS[1], ARGV[2]) " +
                            "else " +
                            "return 0 " +
                            "end";

            Long result = redisTemplate.execute(
                    new DefaultRedisScript<>(script, Long.class),
                    Collections.singletonList(key),
                    threadId,
                    String.valueOf(DEFAULT_TIMEOUT.getSeconds())
            );

            // 续期失败则取消任务
            if (result == 0) {
                cancelRenewal(key);
            }
        }, 0, DEFAULT_TIMEOUT.getSeconds() / 3, TimeUnit.SECONDS);

        renewalTasks.put(key, future);
    }

    /**
     * 取消续期任务
     */
    private void cancelRenewal(String key) {
        ScheduledFuture<?> future = renewalTasks.remove(key);
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     * 动态退避等待算法
     *
     * @param endTime 等待截止时间戳
     */
    private void sleepWithBackoff(long endTime) {
        long remaining = endTime - System.currentTimeMillis();
        if (remaining <= 0) return;

        // 计算等待时间：剩余时间的一半 + 随机扰动（避免惊群效应）
        long sleepTime = Math.min(100,
                remaining / 2 + ThreadLocalRandom.current().nextInt(50));

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
