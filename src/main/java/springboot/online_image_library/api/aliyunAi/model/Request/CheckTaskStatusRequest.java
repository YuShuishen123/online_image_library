package springboot.online_image_library.api.aliyunAi.model.Request;

import lombok.Data;

/**
 * @author Yu'S'hui'shen
 * @date 2025/6/4
 * @description 查询任务状态通用请求体
 */
@Data
public class CheckTaskStatusRequest {

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 任务类型
     */
    private Integer taskType;


}
