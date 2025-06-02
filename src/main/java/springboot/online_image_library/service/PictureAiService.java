package springboot.online_image_library.service;

import springboot.online_image_library.api.aliyunAi.model.Request.ExpansionTaskRequestFromTheFrontend;
import springboot.online_image_library.api.aliyunAi.model.Response.CreateOutPaintingTaskResponse;
import springboot.online_image_library.modle.entiry.User;

/**
 * AI图片编辑服务接口类
 *
 * @author Yu'S'hui'shen
 */
public interface PictureAiService {

    /**
     * 创建图扩图任务
     *
     * @param expansionTaskRequestFromTheFrontend 包含外绘任务参数的请求对象
     * @return CreateOutPaintingTaskResponse 包含任务创建结果的响应对象
     */
    CreateOutPaintingTaskResponse createOutPaintingTask(ExpansionTaskRequestFromTheFrontend expansionTaskRequestFromTheFrontend, User logUser);

}
