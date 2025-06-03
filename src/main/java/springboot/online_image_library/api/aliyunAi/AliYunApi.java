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
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
    // 创建文生图任务地址
    public static final String CREATE_TEXT_TO_IMAGE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis";
    // 查询任务状态
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";
    // 读取配置文件中的ApiKey
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    /**
     * 创建扩图任务
     *
     * @param expansionTaskRequestSentToApi
     * @return
     */
    public CreateTaskResponse createOutPaintingTask(ExpansionTaskRequestSentToAPI expansionTaskRequestSentToApi) {
        if (expansionTaskRequestSentToApi == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
        }
        // 发送请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                // 必须开启异步处理，设置为enable。
                .header("X-DashScope-Async", "enable")
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                .body(JSONUtil.toJsonStr(expansionTaskRequestSentToApi));
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
            }
            CreateTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateTaskResponse.class);
            String errorCode = response.getCode();
            if (CharSequenceUtil.isNotBlank(errorCode)) {
                String errorMessage = response.getMessage();
                log.error("AI 扩图失败，errorCode:{}, errorMessage:{}", errorCode, errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图接口响应异常");
            }
            return response;
        }
    }

    /**
     * 查询创建的扩图任务
     *
     * @param taskId   任务 ID
     * @param clazz    要反序列化成的类
     * @param <T>      泛型类型
     * @return 泛型对象 T
     */
    public <T> T getOutPaintingTask(String taskId, Class<T> clazz) {
        if (CharSequenceUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务 id 不能为空");
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
     */
    public CreateTaskResponse createTextToImageTask(Text2ImageRequest text2ImageRequest) {
        // 建立请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_TEXT_TO_IMAGE_TASK_URL)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                .header("X-DashScope-Async", "enable")
                .body(JSONUtil.toJsonStr(text2ImageRequest));

        // 发送请求
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 文生图失败");
            }
            CreateTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateTaskResponse.class);
            String errorCode = response.getCode();
            if (CharSequenceUtil.isNotBlank(errorCode)) {
                String errorMessage = response.getMessage();
                log.error("AI 文生图失败，errorCode:{}, errorMessage:{}", errorCode, errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 文生图接口响应异常");
            }
            return response;
        }
    }

}