package springboot.online_image_library.modle.dto.request.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 图片上传请求体
 * @author Yu'S'hui'shen
 */
@Data
public class PictureUploadRequest implements Serializable {
  
    /**  
     * 图片 id（用于修改,可以不传）
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 空间id
     */
    private Long spaceId;

    @Serial
    private static final long serialVersionUID = 1L;  
}
