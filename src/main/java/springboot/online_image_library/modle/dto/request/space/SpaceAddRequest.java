package springboot.online_image_library.modle.dto.request.space;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建空间请求参数
 *
 * @author Yu'S'hui'shen
 */
@Data
public class SpaceAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 空间名称
     */
    private String spaceName;
    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;
}
