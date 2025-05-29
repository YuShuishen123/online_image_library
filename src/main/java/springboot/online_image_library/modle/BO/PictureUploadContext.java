package springboot.online_image_library.modle.BO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import springboot.online_image_library.modle.entiry.Picture;
import springboot.online_image_library.modle.entiry.User;

/**
 * 用于存储上传图片的上下文信息
 *
 * @author Yu'S'hui'shen
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PictureUploadContext {

    /**
     * 处理图片上传后的业务逻辑，包括新增或更新操作
     *
     * @param picture 图片实体对象，用于保存或更新数据库记录
     * @param uploadResult 图片上传结果信息，包含URL、缩略图地址等
     * @param loginUser 当前登录用户，用于设置图片的创建者或更新者
     * @param isUpdate 是否为更新操作，true 表示更新，false 表示新增
     * @param oldPictureUrl 旧图片的URL（仅在更新时使用，用于删除旧文件）
     * @param oldPictureThumbnailUrl 旧图片的缩略图URL（仅在更新时使用，用于删除旧文件）
     * @param oldPictureOriginalImageurl 旧图片的原图URL（仅在更新时使用，用于删除旧文件）
     * @param spaceId 所属空间ID，表示图片所属的分类或空间
     * @return 返回处理后的图片视图对象 PictureVO，包含完整的图片信息
     */

    private Picture picture;
    private UploadPictureResult uploadResult;
    private User loginUser;
    private boolean isUpdate;
    private String oldPictureUrl;
    private String oldPictureThumbnailUrl;
    private String oldPictureOriginalImageurl;
    private long spaceId;
}
