package springboot.online_image_library.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.online_image_library.annotation.AuthCheck;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.DeleteRequest;
import springboot.online_image_library.common.ResultUtils;
import springboot.online_image_library.constant.UserConstants;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.modle.dto.request.space.SpaceAddRequest;
import springboot.online_image_library.modle.dto.request.space.SpaceUpdateRequest;
import springboot.online_image_library.modle.entiry.Space;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.service.SpaceService;
import springboot.online_image_library.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/27
 * @description 空间相关接口控制器
 */
@Api(tags = "SpaceController")
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;

    @ApiOperation(
            value = "更新空间参数",
            notes = "用于更新空间参数功能",
            httpMethod = "POST"
    )
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        // 数据校验
        spaceService.validSpace(space, false);
        // 判断空间是否存在
        long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @ApiOperation(
            value = "添加空间",
            notes = "用于添加空间功能",
            httpMethod = "POST"
    )
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    public BaseResponse<Space> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = userService.getLoginUser(request);
        return ResultUtils.success(spaceService.addSpace(spaceAddRequest, user));
    }

    @ApiOperation(
            value = "删除空间",
            notes = "用于删除空间功能",
            httpMethod = "POST"
    )
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 判断空间是否存在
        long id = deleteRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 鉴权,非创建者或者管理员则无权限
        User user = userService.getLoginUser(request);
        ThrowUtils.throwIf(!user.getId().equals(id) && !user.getUserRole().equals(UserConstants.ADMIN_ROLE), ErrorCode.NO_AUTH_ERROR);
        return ResultUtils.success(spaceService.removeById(id));
    }

    @ApiOperation(
            value = "获取当前用户空间信息",
            notes = "用于获取当前用户空间信息功能",
            httpMethod = "POST"
    )
    @PostMapping("/get")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    public BaseResponse<Space> getSpace(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", user.getId());
        Space space = spaceService.getOne(queryWrapper);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "您还未拥有空间");
        return ResultUtils.success(space);

    }
}
