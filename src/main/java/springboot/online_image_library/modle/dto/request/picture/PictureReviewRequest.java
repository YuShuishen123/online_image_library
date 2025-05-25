package springboot.online_image_library.modle.dto.request.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用于管理员发起审核请求
 * @author Yu'S'hui'shen
 */
@Data
public class PictureReviewRequest implements Serializable {
  
    /**  
     * id  
     */  
    private Long id;  
  
    /**  
     * 状态：0-待审核, 1-通过, 2-拒绝  
     */  
    private Integer reviewStatus;  
  
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 审核信息
     */
    private String reviewMessage;
}
