package springboot.online_image_library.modle.dto.request.space;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户空间升级请求类
 *
 * @author Yu'S'hui'shen
 */
@Data
public class SpaceUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
    /**
     * 空间名称
     */
    private String spaceName;
    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;
    /**
     * 空间最大容量
     */
    private Long maxSize;
    /**
     * 空间图片的最大数量
     */
    private Long maxCount;
}
