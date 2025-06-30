package springboot.online_image_library.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author Yu'S'hui'shen
 * @description 幂等控制注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdempotentCheck {

    /**
     * @return 超时时间, 默认30
     */
    long timeOut() default 30;

    /**
     * @return 时间单位, 默认秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

}
