package springboot.online_image_library.modle.dto.request.space;

import lombok.Data;
import lombok.EqualsAndHashCode;
import springboot.online_image_library.common.PageRequest;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户空间查询类
 *
 * @author Yu'S'hui'shen
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
    /**
     * 用户 id
     */
    private Long userId;
    /**
     * 空间名称
     */
    private String spaceName;
    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;
}
