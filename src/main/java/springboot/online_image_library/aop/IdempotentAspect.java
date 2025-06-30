package springboot.online_image_library.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import springboot.online_image_library.annotation.IdempotentCheck;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/28
 * @description 幂等拦截切面
 */
@Order(2)
@Component
@Aspect
public class IdempotentAspect {
    // 获取请求的资源路径


    // 获取请求用户的身份



    private static final String SUCCESS_LOCK_KEY = "idempotent:success:";
    private static final String PROCESSING_LOCK_KEY = "idempotent:prcessing:";
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Around("@annotation(idempotentCheck)")
    public Object intercept(ProceedingJoinPoint proceedingJoinPoint, IdempotentCheck idempotentCheck) throws Throwable {

        // 获取超时时间
        long timeOut = idempotentCheck.timeOut();
        TimeUnit timeUnit = idempotentCheck.timeUnit();
        // 从请求上下文当中获取前端传入的请求唯一ID
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String requestId = requestAttributes.getRequest().getHeader("X-Request-ID");
        // 为空则直接抛出错误
        ThrowUtils.throwIf(requestId == null, ErrorCode.PARAMS_ERROR, "请求ID不能为空");
        // 不为空则判断是否幂等
        String successLockKey = SUCCESS_LOCK_KEY + requestId;
        String processingLockKey = PROCESSING_LOCK_KEY + requestId;
        // 幂等则直接抛出错误
        ThrowUtils.throwIf(redisTemplate.hasKey(successLockKey), ErrorCode.OPERATION_ERROR, "请勿重复请求");
        ThrowUtils.throwIf(redisTemplate.hasKey(processingLockKey), ErrorCode.OPERATION_ERROR, "正在处理中");
        // 非幂等,设置处理中标记
        redisTemplate.opsForValue().set(processingLockKey, "处理中", timeOut, timeUnit);
        // 开始处理业务
        try {
            Object result = proceedingJoinPoint.proceed();
            // 删除处理中标记
            redisTemplate.delete(processingLockKey);
            // 设置幂等标记
            redisTemplate.opsForValue().set(successLockKey, "处理成功", timeOut, timeUnit);
            return result;
        } catch (Throwable throwable) {
            redisTemplate.delete(processingLockKey);
            throw throwable;
        }

    }
}
