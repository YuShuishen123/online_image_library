package springboot.online_image_library.exception;

import lombok.Getter;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/7
 * @description 错误码枚举类
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    // CACHE_ERROR
    REDIS_DATA_SERIALIZATION_FAILED(50002, "redis序列化失败"),
    REDIS_DATA_DESERIALIZATION_FAILED(50003, "redis反序列化失败"),
    CACHE_ERROR(50004, "缓存服务异常");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}

