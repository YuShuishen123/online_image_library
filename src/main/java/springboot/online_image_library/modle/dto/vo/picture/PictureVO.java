package springboot.online_image_library.modle.dto.vo.picture;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import springboot.online_image_library.modle.dto.vo.user.UserVO;
import springboot.online_image_library.modle.entiry.Picture;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片上传成功响应类
 * @author Yu'S'hui'shen
 */
@Data
public class
PictureVO implements Serializable {
  
    /**  
     * id  
     */  
    private Long id;  
  
    /**  
     * 图片 url  
     */
    private String url;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

    /**
     * 原图 url
     */
    private String originalImageurl;
  
    /**  
     * 图片名称  
     */  
    private String name;  
  
    /**  
     * 简介  
     */  
    private String introduction;  
  
    /**  
     * 标签  
     */  
    private List<String> tags;
  
    /**  
     * 分类  
     */  
    private String category;  
  
    /**  
     * 文件体积  
     */  
    private Long picSize;  
  
    /**  
     * 图片宽度  
     */  
    private Integer picWidth;  
  
    /**  
     * 图片高度  
     */  
    private Integer picHeight;  
  
    /**  
     * 图片比例  
     */  
    private Double picScale;  
  
    /**  
     * 图片格式  
     */  
    private String picFormat;  
  
    /**  
     * 用户 id  
     */  
    private Long userId;  
  
    /**  
     * 创建时间  
     */  
    private Date createTime;

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 编辑时间
     */
    private Date editTime;


    /**  
     * 创建用户信息  
     */  
    private UserVO user;
    /**
     * 所属空间id
     */
    private Long spaceId;
  
    /**  
     * 封装类转对象  
     */  
    public static Picture voToObj(PictureVO pictureVO) {
        if (pictureVO == null) {  
            return null;  
        }  
        Picture picture = new Picture();  
        BeanUtils.copyProperties(pictureVO, picture);
        // 类型不同，需要转换  
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;  
    }  
  
    /**  
     * 对象转封装类  
     */  
    public static PictureVO objToVo(Picture picture) {
        if (picture == null) {  
            return null;  
        }  
        PictureVO pictureVO = new PictureVO();  
        BeanUtils.copyProperties(picture, pictureVO);  
        // 类型不同，需要转换
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;  
    }  
}
