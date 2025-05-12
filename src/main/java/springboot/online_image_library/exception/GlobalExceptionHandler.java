package springboot.online_image_library.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.ResultUtils;

import java.sql.SQLException;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/7
 * @description 全局异常捕获类
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    @ExceptionHandler(SQLException.class)
    public BaseResponse<?> sqlExceptionHandler(SQLException e) {
        log.error("SQLException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "数据库错误");
    }
}


