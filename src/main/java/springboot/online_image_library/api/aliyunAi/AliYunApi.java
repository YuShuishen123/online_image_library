package springboot.online_image_library.api.aliyunAi;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import springboot.online_image_library.api.aliyunAi.model.Request.ExpansionTaskRequestSentToAPI;
import springboot.online_image_library.api.aliyunAi.model.Request.Text2ImageRequest;
import springboot.online_image_library.api.aliyunAi.model.Request.UniversalImageEditingRequestBody;
import springboot.online_image_library.api.aliyunAi.model.Response.CreateTaskResponse;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;

/**
 * 阿里云 AI 扩图接口
 *
 * @author Yu'S'hui'shen
 */
@Slf4j
@Component
public class AliYunApi {
    // 创建扩图任务地址
    private static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
    // 创建文生图任务地址
    private static final String CREATE_TEXT_TO_IMAGE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis";
    // 创建图生图任务地址
    private static final String CREATE_IMAGE_TO_IMAGE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/image-synthesis";
    // 查询任务状态
    private static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";
    // 读取配置文件中的ApiKey
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    /**
     * 通用的POST请求处理方法
     *
     * @param url 请求地址
     * @param body 请求体
     * @param errorMessage 错误提示信息
     * @return CreateTaskResponse
     */
    private CreateTaskResponse executePostRequest(String url, Object body, String errorMessage) {
        if (body == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, errorMessage + "参数为空");
        }

        HttpRequest httpRequest = HttpRequest.post(url)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .header("X-DashScope-Async", "enable")
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                .body(JSONUtil.toJsonStr(body));

        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, errorMessage + "失败: " + httpResponse.body());
            }
            CreateTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateTaskResponse.class);
            String errorCode = response.getCode();
            if (CharSequenceUtil.isNotBlank(errorCode)) {
                String errMsg = response.getMessage();
                log.error("{}失败，errorCode:{}, errorMessage:{}", errorMessage, errorCode, errMsg);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, errorMessage + "接口响应异常");
            }
            return response;
        }
    }

    /**
     * 创建扩图任务
     *
     * @param expansionTaskRequestSentToApi 扩图请求参数
     * @return CreateTaskResponse
     */
    public CreateTaskResponse createOutPaintingTask(ExpansionTaskRequestSentToAPI expansionTaskRequestSentToApi) {
        return executePostRequest(CREATE_OUT_PAINTING_TASK_URL, expansionTaskRequestSentToApi, "AI扩图");
    }

    /**
     * 查询任务状态
     *
     * @param taskId 任务 ID
     * @param clazz 要反序列化成的类
     * @param <T> 泛型类型
     * @return 泛型对象 T
     */
    public <T> T getOutPaintingTask(String taskId, Class<T> clazz) {
        if (CharSequenceUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务ID不能为空");
        }

        try (HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务失败");
            }

            log.info("获取任务结果：{}", httpResponse.body());
            return JSONUtil.toBean(httpResponse.body(), clazz);
        }
    }

    /**
     * 创建文生图任务
     *
     * @param text2ImageRequest 文生图请求参数
     * @return CreateTaskResponse
     */
    public CreateTaskResponse createTextToImageTask(Text2ImageRequest text2ImageRequest) {
        return executePostRequest(CREATE_TEXT_TO_IMAGE_TASK_URL, text2ImageRequest, "AI文生图");
    }

    /**
     * 创建通用图像编辑任务
     *
     * @param universalImageEditingRequestBody 通用图像编辑请求参数
     * @return CreateTaskResponse
     */
    public CreateTaskResponse createUniversalImageTask(UniversalImageEditingRequestBody universalImageEditingRequestBody) {
        return executePostRequest(CREATE_IMAGE_TO_IMAGE_TASK_URL, universalImageEditingRequestBody, "AI通用图像编辑");
    }
}