package springboot.online_image_library.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.online_image_library.annotation.AuthCheck;
import springboot.online_image_library.annotation.IdempotentCheck;
import springboot.online_image_library.api.aliyunAi.model.Request.ExpansionTaskRequestFromTheFrontend;
import springboot.online_image_library.api.aliyunAi.model.Response.CreateOutPaintingTaskResponse;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.ResultUtils;
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
@RestController
@RequestMapping("/pictureAiService")
public class PictureAIServiceController {

    @Resource
    private PictureAiService pictureAiService;

    @Resource
    private UserService userService;

    /**
     * 创建扩图任务
     */
    @Operation(summary = "创建扩图任务",
            description = "创建扩图任务",
            method = "POST")
    @AuthCheck(mustRole = "ROLE_USER")
    @PostMapping("/pictureAiService/createOutPaintingTask")
    @IdempotentCheck
    public BaseResponse<CreateOutPaintingTaskResponse> createOutPaintingTask(HttpServletRequest request, @RequestBody ExpansionTaskRequestFromTheFrontend expansionTaskRequestFromTheFrontend) {
        ThrowUtils.throwIf(expansionTaskRequestFromTheFrontend == null || expansionTaskRequestFromTheFrontend.getPictureId() == null, ErrorCode.PARAMS_ERROR, "参数为空");
        User logUser = userService.getLoginUser(request);
        return ResultUtils.success(pictureAiService.createOutPaintingTask(expansionTaskRequestFromTheFrontend, logUser));
    }

}
