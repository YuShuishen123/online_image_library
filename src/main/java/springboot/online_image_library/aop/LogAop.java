package springboot.online_image_library.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/11
 * @description 日志记录所有被调用的controller层方法
 */
@Slf4j
@Aspect
@Component
public class LogAop {

    // 定义切入点：所有Controller类的方法
    @Pointcut("execution(* springboot.online_image_library.controller..*.*(..))")
    public void controllerPointcut() {}


    // 环绕通知：记录方法调用前后信息
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }
        // 获取方法签名信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 记录请求信息
        log.debug("======= 请求开始 =======");
        if (request != null) {
            log.info("请求地址: {}", request.getRequestURL().toString());
            log.debug("HTTP方法: {}", request.getMethod());
        }
        log.info("类方法: {}.{}",
                method.getDeclaringClass().getName(),
                method.getName());
        log.info("方法参数: {}", Arrays.toString(joinPoint.getArgs()));

        // 记录方法执行时间
        long startTime = System.currentTimeMillis();
        Object result;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();

            // 记录返回结果和执行时间
            log.debug("方法返回: {}", result);
            log.debug("执行耗时: {} ms", endTime - startTime);
        } catch (Exception e) {
            log.error("方法异常: {}", e.getMessage());
            throw e;
        } finally {
            log.debug("======= 请求结束 =======");
        }

        return result;
    }
}
