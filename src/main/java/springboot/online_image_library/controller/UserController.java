package springboot.online_image_library.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;
import springboot.online_image_library.annotation.AuthCheck;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.DeleteRequest;
import springboot.online_image_library.common.ResultUtils;
import springboot.online_image_library.constant.UserConstants;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.modle.dto.request.user.*;
import springboot.online_image_library.modle.dto.vo.user.LoginUserVO;
import springboot.online_image_library.modle.dto.vo.user.UserVO;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static springboot.online_image_library.exception.ThrowUtils.throwIf;

/**
 * 系统基础接口
 * @author Yu'S'hui'shen
 */
@Api(tags = "UserController")
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
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletResponse response) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, response,0);
        return ResultUtils.success(loginUserVO);
    }


    @ApiOperation(
            value = "管理员登陆",
            notes = "用于管理员登陆功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/admin/login")
    public BaseResponse<LoginUserVO> adminLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletResponse response) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, response,1);
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
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
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
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        // 获取请求体中的字段信息
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码
        user.setUserPassword(userService.getEncryptPassword("123456789"));
        try {
            userService.save(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已经存在");
        }
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
    public BaseResponse<User> getUserById(@RequestParam(defaultValue = "0") Long id) {
        if (id == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"id参数不能为空或为0");
        }
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR,"查询的用户不存在");
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
        // 判断用户是否存在
        throwIf(userService.getById(deleteRequest.getId()) == null,ErrorCode.NOT_FOUND_ERROR);
        throwIf(!userService.removeById(deleteRequest.getId()),ErrorCode.OPERATION_ERROR,"删除用户失败");
        return ResultUtils.success(true);
    }

    /**
     * 更新用户
     */
    @ApiOperation(
            value = "用户个人信息更新",
            notes = "用于更新用户个人信息,普通用户只能更新自己的信息,管理员可以更新所有用户的信息",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断更新的用户是否存在
        User oldUser = userService.getById(userUpdateRequest.getId());
        throwIf(oldUser == null,ErrorCode.NOT_FOUND_ERROR,"不存在的用户");
        // 获取当前登陆用户
        User loginUser = userService.getLoginUser(request);
        // 比对用户,普通用户只能修改自己的信息,管理员可以直接修改
        if (!oldUser.getId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        User newUser = new User();
        // 把request内存在且非空的字段赋值给newUser
        BeanUtil.copyProperties(userUpdateRequest, newUser, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        // 更新信息
        boolean result = userService.updateById(newUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

}
