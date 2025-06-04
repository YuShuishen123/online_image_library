package springboot.online_image_library.service;

import springboot.online_image_library.api.aliyunAi.model.Request.ExpansionTaskRequestFromTheFrontend;
import springboot.online_image_library.api.aliyunAi.model.Request.Text2ImageRequest;
import springboot.online_image_library.api.aliyunAi.model.Request.UniversalImageEditingRequestBody;
import springboot.online_image_library.api.aliyunAi.model.Response.CreateTaskResponse;
import springboot.online_image_library.modle.entiry.User;

/**
 * AI图片编辑服务接口类
 *
 * @author Yu'S'hui'shen
 */
public interface PictureAiService {

    /**
     * 创建扩图任务
     *
     * @param expansionTaskRequestFromTheFrontend 包含外绘任务参数的请求对象
     * @return CreateOutPaintingTaskResponse 包含任务创建结果的响应对象
     */
    CreateTaskResponse createOutPaintingTask(ExpansionTaskRequestFromTheFrontend expansionTaskRequestFromTheFrontend, User logUser);


    /**
     * 创建文字转图片任务
     *
     * @param text2ImageRequest 包含文字转图片任务参数的请求对象
     * @return CreateTextToImageTaskResponse 包含任务创建结果的响应对象
     */
    CreateTaskResponse createTextToImageTask(Text2ImageRequest text2ImageRequest);


    /**
     * 创建通用图片编辑任务
     *
     * @param universalImageEditingRequestBody 包含通用图片编辑任务参数的请求对象
     * @return CreateUniversalImageTaskResponse 包含任务创建结果的响应对象
     */
    CreateTaskResponse createUniversalImageTask(UniversalImageEditingRequestBody universalImageEditingRequestBody);



}
