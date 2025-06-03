package springboot.online_image_library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springboot.online_image_library.annotation.AuthCheck;
import springboot.online_image_library.api.aliyunAi.AliYunApi;
import springboot.online_image_library.api.aliyunAi.model.Request.ExpansionTaskRequestFromTheFrontend;
import springboot.online_image_library.api.aliyunAi.model.Request.Text2ImageRequest;
import springboot.online_image_library.api.aliyunAi.model.Response.CreatText2ImageTaskResponse;
import springboot.online_image_library.api.aliyunAi.model.Response.CreateTaskResponse;
import springboot.online_image_library.api.aliyunAi.model.Response.GetTaskResponse;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.ResultUtils;
import springboot.online_image_library.constant.UserConstants;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.modle.entiry.User;
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
        User logUser = userService.getLoginUser(request);
        return ResultUtils.success(pictureAiService.createOutPaintingTask(expansionTaskRequestFromTheFrontend, logUser));
    }

    /**
     * 查询扩图任务
     */
    @Operation(summary = "查询扩图任务",
            description = "查询扩图任务",
            method = "GET")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    @GetMapping("/pictureAiService/queryOutPaintingTask")
    public BaseResponse<GetTaskResponse> queryOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "参数为空");
        GetTaskResponse getTaskResponse = aliYunApi.getOutPaintingTask(taskId, GetTaskResponse.class);
        return ResultUtils.success(getTaskResponse);
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
     * 查询文生图任务结果
     */
    @Operation(summary = "查询文生图任务结果",
            description = "查询文生图任务结果",
            method = "GET")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    @GetMapping("/pictureAiService/queryText2ImagePaintingTask")
    public BaseResponse<CreatText2ImageTaskResponse> queryText2ImagePaintingTask(String taskId) {
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "参数为空");
        CreatText2ImageTaskResponse creatText2ImageTaskResponse = aliYunApi.getOutPaintingTask(taskId, CreatText2ImageTaskResponse.class);
        log.info("查询文生图任务结果 creatText2ImageTaskResponse:{}", creatText2ImageTaskResponse);

        // 检查 TaskMetrics 是否为 null
        if (creatText2ImageTaskResponse.getOutput() == null ||
                creatText2ImageTaskResponse.getOutput().getTaskMetrics() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "正在生成中,请稍等");
        }

        if (creatText2ImageTaskResponse.getOutput().getTaskMetrics().getSucceeded() == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文生图任务失败");
        }
        return ResultUtils.success(creatText2ImageTaskResponse);
    }









}
