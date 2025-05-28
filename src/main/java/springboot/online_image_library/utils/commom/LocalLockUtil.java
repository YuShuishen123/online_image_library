package springboot.online_image_library.utils.commom;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 本地锁工具类（基于JVM锁实现）
 *
 * <p><b>适用场景：</b>
 * <ul>
 *   <li>单机应用内的线程安全控制</li>
 *   <li>替代synchronized的更灵活方案</li>
 *   <li>需要细粒度锁控制的场景</li>
 * </ul>
 *
 * @author YourName
 */
public class LocalLockUtil {

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<String, Lock> LOCK_MAP = new ConcurrentHashMap<>();

    private LocalLockUtil() {
    }

    /**
     * 获取锁并执行代码块,但是不带返回值
     *
     * @param lockKey 锁键（建议按业务命名）
     * @param task    需要加锁执行的逻辑
     */
    public static void executeWithLock(String lockKey, Runnable task) {
        // 为每个key创建独立的ReentrantLock（惰性初始化）
        Lock lock = LOCK_MAP.computeIfAbsent(lockKey, k -> new ReentrantLock());

        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取锁并执行代码块，带返回值
     *
     * @param lockKey 锁键
     * @param task    需要执行的逻辑
     * @param <T>     返回值类型
     * @return T 返回值
     */
    public static <T> T executeWithLockAndReturn(String lockKey, Supplier<T> task) {
        Lock lock = LOCK_MAP.computeIfAbsent(lockKey, k -> new ReentrantLock());
        lock.lock();
        try {
            return task.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 尝试获取锁（非阻塞）
     *
     * @param lockKey 锁键
     * @param task    需要执行的逻辑
     * @return true-获取锁成功并执行, false-获取锁失败
     */
    public static boolean tryExecuteWithLock(String lockKey, Runnable task) {
        Lock lock = LOCK_MAP.computeIfAbsent(lockKey, k -> new ReentrantLock());

        if (lock.tryLock()) {
            try {
                task.run();
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    /**
     * 清理不再使用的锁（防止内存泄漏）
     *
     * @param lockKey 要移除的锁键
     */
    public static void removeLock(String lockKey) {
        LOCK_MAP.remove(lockKey);
    }
}
