package springboot.online_image_library.api.aliyunAi.model.Request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 前端传入的图片扩展任务创建请求类
 *
 * @author Yu'S'hui'shen
 */
@Data
public class ExpansionTaskRequestFromTheFrontend implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 图片 id
     */
    private Long pictureId;
    /**
     * 扩图参数
     */
    private ExpansionTaskRequestSentToAPI.Parameters parameters;
}
