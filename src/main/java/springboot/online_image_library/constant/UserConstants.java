package springboot.online_image_library.constant;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/8
 * @description 用户相关常量（工具类形式）
 */

public final class UserConstants {
    // 将 UserConstant 接口中定义的常量迁移到更合适的类或枚举类型中
    // 私有构造，防止实例化
    private UserConstants() {}

    /**
     * 用户登录态键
     */
    public static final String USER_LOGIN_STATE = "user_login";

    /**
     * 默认角色
     */
    public static final String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    public static final String ADMIN_ROLE = "admin";

    /**
     * 用户账号
     */
    public static final String USER_ACCOUNT_NICKNAME = "userAccount";
}


