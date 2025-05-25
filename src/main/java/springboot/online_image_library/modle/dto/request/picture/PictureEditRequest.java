package springboot.online_image_library.modle.dto.request.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 图片修改请求，一般情况下给普通用户使用，可修改的字段范围小于更新请求
 * @author Yu'S'hui'shen
 */
@Data
public class PictureEditRequest implements Serializable {
  
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
