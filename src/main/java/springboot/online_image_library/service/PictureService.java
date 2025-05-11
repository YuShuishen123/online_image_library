package springboot.online_image_library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.modle.dto.request.picture.PictureUploadRequest;
import springboot.online_image_library.modle.dto.vo.picture.PictureVO;
import springboot.online_image_library.modle.dto.vo.user.UserVO;
import springboot.online_image_library.modle.entiry.Picture;

/**
* @author Yu'S'hui'shen
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-05-11 21:29:53
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUserVO
     * @return 上传结果
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            UserVO loginUserVO);

}
