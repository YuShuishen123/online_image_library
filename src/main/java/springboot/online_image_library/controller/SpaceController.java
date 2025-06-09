package springboot.online_image_library.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
import springboot.online_image_library.modle.BO.SpaceLevel;
import springboot.online_image_library.modle.dto.request.space.SpaceAddRequest;
import springboot.online_image_library.modle.dto.request.space.SpaceUpdateRequest;
import springboot.online_image_library.modle.dto.vo.user.LoginState;
import springboot.online_image_library.modle.entiry.Space;
import springboot.online_image_library.service.SpaceService;
import springboot.online_image_library.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/27
 * @description 空间相关接口控制器
 */
@Tag(name = "SpaceController", description = "空间相关接口控制器")
@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceController {

    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;

    @Operation(
            summary = "更新空间参数",
            description = "用于更新空间参数功能",
            method = "POST")
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


    @Operation(
            summary = "添加空间",
            description = "用于添加空间功能",
            method = "POST")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    public BaseResponse<Space> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        LoginState loginState = userService.getLoginState(request);
        return ResultUtils.success(spaceService.addSpace(spaceAddRequest, loginState));
    }

    @Operation(
            summary = "删除空间",
            description = "用于删除空间功能",
            method = "POST")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 判断空间是否存在
        long id = deleteRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 鉴权,非创建者或者管理员则无权限
        LoginState loginState = userService.getLoginState(request);
        ThrowUtils.throwIf(!loginState.getId().equals(id) && !loginState.getUserRole().equals(UserConstants.ADMIN_ROLE), ErrorCode.NO_AUTH_ERROR);
        return ResultUtils.success(spaceService.removeById(id));
    }

    @Operation(
            summary = "获取当前用户空间信息",
            description = "用于获取当前用户空间信息功能",
            method = "POST")
    @PostMapping("/get")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    public BaseResponse<Space> getSpace(HttpServletRequest request) {
        LoginState loginState = userService.getLoginState(request);
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginState.getId());
        Space space = spaceService.getOne(queryWrapper);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "您还未拥有空间");
        return ResultUtils.success(space);
    }

    @Operation(
            summary = "获取各级空间信息",
            description = "用于获取各级空间信息",
            method = "GET")
    @GetMapping("/space")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        // 定义缓存key
        String cacheKey = "SPACE:LEVEL_LIST";
        List<SpaceLevel> result = spaceService.getSpaceLeveListJsonFromCache(cacheKey).orElseGet(() -> {
            List<SpaceLevel> levels = spaceService.buildSpaceLevelsFromEnum();
            spaceService.cacheSpaceLevels(cacheKey, levels);
            return levels;
        });
        return ResultUtils.success(result);
    }


}
