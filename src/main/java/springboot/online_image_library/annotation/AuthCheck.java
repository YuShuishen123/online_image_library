package springboot.online_image_library.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/8
 * @description 用户权限校验的注解定义类
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {


    /**
     * @return 必须具有某个角色
     */
    String mustRole() default "";


}
