package springboot.online_image_library.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.online_image_library.annotation.AuthCheck;
import springboot.online_image_library.annotation.IdempotentCheck;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.ResultUtils;
import springboot.online_image_library.constant.UserConstants;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.manager.CacheClient;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * 系统基础接口
 * @author Yu'S'hui'shen
 */
@Slf4j
@Tag(name = "MainController", description = "系统基础功能测试接口")
@RestController
@RequestMapping("/")
public class MainController {

    private static final String USER_LOGIN_STATE = "user:login:state:";
    @Resource
    UserService userService;
    private static final String SESSION_TOKEN = "session_token";
    // 登录状态过期时间，30分钟
    private static final long LOGIN_EXPIRE_TIME = 24 * 60 * (long) 60;
    @Resource
    CacheClient cacheClient;

    @Operation(
            summary = "幂等测试",
            description = "用于检测幂等切面是否生效",
            method = "POST")
    @PostMapping("/idempotent")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    @IdempotentCheck(timeOut = 20, timeUnit = TimeUnit.SECONDS)
    public BaseResponse<String> idempotentTest() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            // 恢复线程中断状态
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求被中断");
        }
        return ResultUtils.success("成功");
    }



    /**
     * 服务健康检查接口
     */
    @ApiOperation(
            value = "健康检查",
            notes = "用于检测服务是否正常运行",
            httpMethod = "GET",
            response = BaseResponse.class
    )
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }

    /**
     * 切换用户和管理员接口,方便测试
     */
    @ApiOperation(
            value = "切换用户和管理员",
            notes = "用于切换用户和管理员",
            httpMethod = "GET",
            response = BaseResponse.class
    )
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    @GetMapping("/switch")
    public BaseResponse<String> switchUser(HttpServletRequest hettpServletRequest) {
        User loginUser =  userService.getLoginUser(hettpServletRequest);
        User newUser = new User();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",loginUser.getId());
        if(Objects.equals(loginUser.getUserRole(), UserConstants.DEFAULT_ROLE)){
            newUser.setUserRole(UserConstants.ADMIN_ROLE);
            userService.update(newUser,queryWrapper);
        }else if (Objects.equals(loginUser.getUserRole(), UserConstants.ADMIN_ROLE)){
            newUser.setUserRole(UserConstants.DEFAULT_ROLE);
            userService.update(newUser,queryWrapper);
        }
        // 更新用户登陆状态的缓存
        Cookie[] cookies = hettpServletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SESSION_TOKEN.equals(cookie.getName()) && cookie.getValue() != null) {
                    log.info("角色切换成功,更新缓存");
                    User user = userService.getById(loginUser.getId());
                    cacheClient.update(USER_LOGIN_STATE + cookie.getValue(), user, Duration.ofSeconds(LOGIN_EXPIRE_TIME));
                }
            }
        }
        return ResultUtils.success("切换成功,由"+loginUser.getUserRole()+"切换至"+newUser.getUserRole());
    }
}
