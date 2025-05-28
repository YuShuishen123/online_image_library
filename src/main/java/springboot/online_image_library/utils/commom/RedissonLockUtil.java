package springboot.online_image_library.utils.commom;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author Yu'S'hui'shen
 * @description Redisson分布式锁工具类
 */
@Component
public class RedissonLockUtil {

    private final RedissonClient redissonClient;

    public RedissonLockUtil(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 执行不带返回值的任务，加锁保护
     *
     * @param lockKey  锁的键（建议业务唯一）
     * @param task     执行的任务
     * @param waitSec  最多等待时间（秒）
     * @param leaseSec 自动释放锁时间（秒） - Redisson默认支持锁续期机制
     */
    public void executeNoResult(String lockKey, Runnable task, long waitSec, long leaseSec) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitSec, leaseSec, TimeUnit.SECONDS);
            if (locked) {
                task.run();
            } else {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取锁失败：" + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取锁中断：" + lockKey);
        } finally {
            // 只有当前线程持有锁才解锁，避免误释放其他线程持有的锁
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 执行带返回值的任务，加锁保护
     *
     * @param lockKey  锁键
     * @param supplier 带返回值的逻辑
     * @param waitSec  等待锁时间（秒）
     * @param leaseSec 租约时间（秒）
     * @return T 返回结果
     */
    public <T> T executeWithResult(String lockKey, Supplier<T> supplier, long waitSec, long leaseSec) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitSec, leaseSec, TimeUnit.SECONDS);
            if (locked) {
                return supplier.get();
            } else {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取锁失败：" + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取锁中断：" + lockKey);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


}
