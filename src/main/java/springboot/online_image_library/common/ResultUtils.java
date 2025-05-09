package springboot.online_image_library.common;

import springboot.online_image_library.exception.ErrorCode;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/7
 * @description 封装响应
 */
@SuppressWarnings("unused")
public class ResultUtils {

    /**
     * 私有构造函数，防止工具类被实例化
     */
    private ResultUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 成功
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, data, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code    错误码
     * @param message 错误信息
     * @return 响应
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}
