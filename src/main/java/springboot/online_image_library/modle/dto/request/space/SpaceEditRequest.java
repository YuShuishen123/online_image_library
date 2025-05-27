package springboot.online_image_library.modle.dto.request.space;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户编辑空间请求类
 *
 * @author Yu'S'hui'shen
 */
@Data
public class SpaceEditRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 空间 id
     */
    private Long id;
    /**
     * 空间名称
     */
    private String spaceName;
}
