package springboot.online_image_library.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import springboot.online_image_library.annotation.AuthCheck;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.modle.dto.vo.user.LoginState;
import springboot.online_image_library.modle.enums.UserRoleEnum;
import springboot.online_image_library.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/8
 * @description 用户权限认证切面类
 */

@Aspect
@Component
@Order(1)
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object intercept(ProceedingJoinPoint proceedingJoinPoint, AuthCheck authCheck) throws Throwable{
        // 获取所必须的权限角色
        String mustRole = authCheck.mustRole();
        // 获取请求的状态内容
        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes)attributes).getRequest();
        // 获取当前登陆用户
        LoginState loginState = userService.getLoginState(request);
        // 获取角色权限所对应的枚举类型
        UserRoleEnum mustRoleEnum = UserRoleEnum.fromValue(mustRole);
        // 获取当前用户所具有的权限
        UserRoleEnum loginUserRoleEnum = UserRoleEnum.fromValue(loginState.getUserRole());
        // 需要管理员权限,且用户没有的情况下,拒绝
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(loginUserRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 通过权限认证,执行目标方法
        return proceedingJoinPoint.proceed();
    }
}
