package springboot.online_image_library.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/7
 * @description 删除响应
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;
}




