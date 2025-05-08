package springboot.online_image_library.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springboot.online_image_library.annotation.AuthCheck;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.ResultUtils;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.modle.dto.UserLoginRequest;
import springboot.online_image_library.modle.dto.UserRegistRequest;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.modle.vo.LoginUserVO;
import springboot.online_image_library.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static springboot.online_image_library.constant.UserConstants.ADMIN_ROLE;
import static springboot.online_image_library.exception.ThrowUtils.throwIf;

/**
 * 系统基础接口
 * @author Yu'S'hui'shen
 */
@Api(tags = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册接口
     */
    @ApiOperation(
            value = "用户注册",
            notes = "用于用户注册功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegistRequest userRegistRequest) {
        throwIf(userRegistRequest == null, ErrorCode.PARAMS_ERROR, "注册参数为空");
        String userAccount = userRegistRequest.getUserAccount();
        String userPassword = userRegistRequest.getUserPassword();
        String checkPassword = userRegistRequest.getCheckPassword();
        Long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    @ApiOperation(
            value = "用户登陆",
            notes = "用于用户登陆功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    @ApiOperation(
            value = "获取当前登陆用户信息",
            notes = "用于获取当前登陆用户信息",
            httpMethod = "GET",
            response = BaseResponse.class
    )
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    @ApiOperation(
            value = "用户登出",
            notes = "用于用户登出功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }




}
