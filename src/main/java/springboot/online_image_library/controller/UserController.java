package springboot.online_image_library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import springboot.online_image_library.annotation.AuthCheck;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.DeleteRequest;
import springboot.online_image_library.common.ResultUtils;
import springboot.online_image_library.constant.UserConstants;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.modle.dto.user.*;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.modle.vo.LoginUserVO;
import springboot.online_image_library.modle.vo.UserVO;
import springboot.online_image_library.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @ApiOperation(
            value = "用户注册",
            notes = "用于用户注册功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
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
        throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @ApiOperation(
            value = "查询其他用户信息(列表)",
            notes = "用于获取用户信息,返回的是一个列表",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/get/vo")
    public BaseResponse<Page<UserVO>> getUserVobyId(@RequestBody UserQueryRequest userQueryRequest){
        throwIf(userQueryRequest==null,ErrorCode.PARAMS_ERROR);
        // 获取分页参数
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVoPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVoPage.setRecords(userVOList);
        return ResultUtils.success(userVoPage);
    }

    // --------以下为管理员操作接口

    @ApiOperation(
            value = "管理员添加用户",
            notes = "用于添加用户功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest){
        throwIf(userAddRequest == null,ErrorCode.PARAMS_ERROR);
        User user = new User();
        // 获取请求体中的字段信息
        BeanUtils.copyProperties(userAddRequest,user);
        // 默认密码
        user.setUserPassword(userService.getEncryptPassword("123456789"));
        // 调用baseService提供的save方法
        ThrowUtils.throwIf(!userService.save(user),ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @ApiOperation(
            value = "管理员获取用户信息(未脱敏)",
            notes = "用于获取用户信息",
            httpMethod = "GET",
            response = BaseResponse.class
    )
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 删除用户
     */
    @ApiOperation(
            value = "管理员删除用户",
            notes = "用于删除用户功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @ApiOperation(
            value = "管理员更新用户",
            notes = "用于更新用户功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


}
