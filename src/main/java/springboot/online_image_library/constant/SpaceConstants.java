package springboot.online_image_library.constant;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/27
 * @description 用于规定空间相关的常量
 */
public final class SpaceConstants {
    /**
     * 空间实体缓存前缀
     */
    public static final String CACHE_ENTITY_PREFIX = "space:entity:";

    // ==================== 缓存相关 ====================
    /**
     * 空间图片列表缓存前缀
     */
    public static final String CACHE_PICTURE_LIST_PREFIX = "space:picturelist:";
    /**
     * 缓存过期时间
     */
    public static final Duration CACHE_EXPIRE_TIME = Duration.ofMinutes(30);
    /**
     * 默认空间名称
     */
    public static final String DEFAULT_SPACE_NAME = "默认空间";

    // ==================== 业务默认值 ====================
    /**
     * 默认空间级别
     */
    public static final int DEFAULT_SPACE_LEVEL = 1;
    /**
     * 用户锁映射表
     */
    private static final ConcurrentHashMap<Long, Object> USER_LOCKS = new ConcurrentHashMap<>();

    // ==================== 并发控制 ====================
    private SpaceConstants() {
    }

    /**
     * 获取用户锁对象
     */
    public static Object getUserLock(Long userId) {
        return USER_LOCKS.computeIfAbsent(userId, k -> new Object());
    }
}

