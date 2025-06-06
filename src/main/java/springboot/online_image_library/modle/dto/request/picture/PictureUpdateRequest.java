package springboot.online_image_library.modle.dto.request.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 图片更新请求体
 * @author Yu'S'hui'shen
 */
@Data
public class PictureUpdateRequest implements Serializable {
  
    /**  
     * id  
     */  
    private Long id;  
  
    /**  
     * 图片名称  
     */  
    private String name;  
  
    /**  
     * 简介  
     */  
    private String introduction;  
  
    /**  
     * 分类  
     */  
    private String category;  
  
    /**  
     * 标签  
     */  
    private List<String> tags;

    @Serial
    private static final long serialVersionUID = 1L;
}
