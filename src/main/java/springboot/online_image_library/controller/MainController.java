package springboot.online_image_library.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.ResultUtils;
import springboot.online_image_library.constant.UserConstants;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 系统基础接口
 * @author Yu'S'hui'shen
 */
@Api(tags = "系统调试", value = "系统基础功能接口")
@RestController
@RequestMapping("/")
public class MainController {

    @Resource
    UserService userService;

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
        return ResultUtils.success("切换成功,由"+loginUser.getUserRole()+"切换至"+newUser.getUserRole());
    }
}
