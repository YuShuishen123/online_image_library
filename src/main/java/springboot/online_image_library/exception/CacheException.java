package springboot.online_image_library.exception;

import lombok.Getter;

/**
 * 自定义缓存异常类
 *
 * @author Yu'S'hui'shen
 */
@Getter
public class CacheException extends RuntimeException {
    /**
     * 错误码
     */
    private final int code;

    public CacheException(int code, String message) {
        super(message);
        this.code = code;
    }

    public CacheException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public CacheException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}