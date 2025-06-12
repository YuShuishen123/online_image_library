package springboot.online_image_library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springboot.online_image_library.annotation.AuthCheck;
import springboot.online_image_library.api.aliyunAi.AliYunApi;
import springboot.online_image_library.api.aliyunAi.model.Request.CheckTaskStatusRequest;
import springboot.online_image_library.api.aliyunAi.model.Request.ExpansionTaskRequestFromTheFrontend;
import springboot.online_image_library.api.aliyunAi.model.Request.Text2ImageRequest;
import springboot.online_image_library.api.aliyunAi.model.Request.UniversalImageEditingRequestBody;
import springboot.online_image_library.api.aliyunAi.model.Response.CreateTaskResponse;
import springboot.online_image_library.api.aliyunAi.model.TaskTypeEnum;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.ResultUtils;
import springboot.online_image_library.constant.UserConstants;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.modle.dto.vo.user.LoginState;
import springboot.online_image_library.service.PictureAiService;
import springboot.online_image_library.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Yu'S'hui'shen
 * @date 2025/6/1
 * @description 图片ai服务控制器
 */
@Tag(name = "AIServiceController", description = "图片ai服务控制器")
@RestController
@RequestMapping("/pictureAiService")
@Slf4j
public class PictureAIServiceController {

    @Resource
    private PictureAiService pictureAiService;

    @Resource
    private UserService userService;

    private final AliYunApi aliYunApi;

    public PictureAIServiceController(AliYunApi aliYunApi) {
        this.aliYunApi = aliYunApi;
    }

    /**
     * 创建扩图任务
     */
    @Operation(summary = "创建扩图任务",
            description = "创建扩图任务,参数填写说明文档https://bailian.console.aliyun.com/?utm_content=m_1000400275&tab=api#/api/?type=model&url=https%3A%2F%2Fhelp.aliyun.com%2Fdocument_detail%2F2796845.html&renderType=iframe:",
            method = "POST")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    @PostMapping("/pictureAiService/createOutPaintingTask")
    public BaseResponse<CreateTaskResponse> createOutPaintingTask(HttpServletRequest request, @RequestBody ExpansionTaskRequestFromTheFrontend expansionTaskRequestFromTheFrontend) {
        ThrowUtils.throwIf(expansionTaskRequestFromTheFrontend == null || expansionTaskRequestFromTheFrontend.getPictureId() == null, ErrorCode.PARAMS_ERROR, "参数为空");
        LoginState loginState = userService.getLoginState(request);
        return ResultUtils.success(pictureAiService.createOutPaintingTask(expansionTaskRequestFromTheFrontend, loginState));
    }

    /**
     * 查询任务状态
     */
    @Operation(summary = "查询任务",
            description = "查询任务",
            method = "POST")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    @PostMapping("/pictureAiService/queryOutPaintingTask")
    public BaseResponse<Object> queryOutPaintingTask(@RequestBody CheckTaskStatusRequest checkTaskStatusRequest) {
        // 参数校验
        ThrowUtils.throwIf(checkTaskStatusRequest == null || checkTaskStatusRequest.getTaskId() == null || checkTaskStatusRequest.getTaskType() == null, ErrorCode.PARAMS_ERROR, "参数为空");

        // 获取任务类型枚举
        TaskTypeEnum taskTypeEnum = TaskTypeEnum.getEnumByValue(checkTaskStatusRequest.getTaskType());
        ThrowUtils.throwIf(taskTypeEnum == null, ErrorCode.PARAMS_ERROR, "任务类型错误");

        // 获取任务类型对应的 Class 对象
        Class<?> clazz = taskTypeEnum.getClazz();

        // 调用 API 获取任务结果
        Object result = aliYunApi.getOutPaintingTask(checkTaskStatusRequest.getTaskId(), clazz);

        // 返回成功响应
        return ResultUtils.success(result);
    }

    /**
     * 创建文生图任务
     */
    @Operation(summary = "创建文生图任务",
            description = "创建文生图任务,参数填写说明文档https://bailian.console.aliyun.com/console?tab=api#/api/?type=model&url=https%3A%2F%2Fhelp.aliyun.com%2Fdocument_detail%2F2862677.html",
            method = "POST")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    @PostMapping("/pictureAiService/createTextToImageTask")
    public BaseResponse<CreateTaskResponse> createTextToImageTask(@RequestBody Text2ImageRequest text2ImageRequest) {
        ThrowUtils.throwIf(text2ImageRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");
        return ResultUtils.success(pictureAiService.createTextToImageTask(text2ImageRequest));
    }


    /**
     * 创建通用图片编辑任务
     */
    @Operation(summary = "创建通用图片编辑任务",
            description = "创建通用图片编辑任务,参数填写说明文档https://bailian.console.aliyun.com/?utm_content=m_1000400275&tab=api#/api/?type=model&url=https%3A%2F%2Fhelp.aliyun.com%2Fdocument_detail%2F2796845.html&renderType=iframe:",
            method = "POST")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    @PostMapping("/pictureAiService/createUniversalImageTask")
    public BaseResponse<CreateTaskResponse> createUniversalImageTask(@RequestBody UniversalImageEditingRequestBody universalImageEditingRequestBody) {
        ThrowUtils.throwIf(universalImageEditingRequestBody == null, ErrorCode.PARAMS_ERROR, "参数为空");
        return ResultUtils.success(pictureAiService.createUniversalImageTask(universalImageEditingRequestBody));
    }


}
