package springboot.online_image_library.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.common.DeleteRequest;
import springboot.online_image_library.modle.BO.UploadPictureResult;
import springboot.online_image_library.modle.dto.request.picture.PictureQueryRequest;
import springboot.online_image_library.modle.dto.request.picture.PictureReviewRequest;
import springboot.online_image_library.modle.dto.request.picture.PictureUploadByBatchRequest;
import springboot.online_image_library.modle.dto.request.picture.PictureUploadRequest;
import springboot.online_image_library.modle.dto.vo.picture.PictureVO;
import springboot.online_image_library.modle.entiry.Picture;
import springboot.online_image_library.modle.entiry.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Yu'S'hui'shen
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-05-11 21:29:53
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile 图片文件
     * @param pictureUploadRequest     图片id(更新时附带)
     * @param loginUser 登录用户
     * @return 上传结果
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


    /**
     * 处理图片上传的公共逻辑(负责实体映射, 文件上传, 删除旧图片,审核设置等)
     * @param picture 图片实体
     * @param uploadResult 上传结果
     * @param loginUser 当前登录用户
     * @param isUpdate 是否为更新操作
     * @param oldPictureUrl 旧图片URL(仅更新时使用)
     * @return 图片VO对象
     */
    PictureVO handlePictureUpload(Picture picture,
                                  UploadPictureResult uploadResult,
                                  User loginUser,
                                  boolean isUpdate,
                                  String oldPictureUrl,
                                  String oldPictureThumbnailUrl,
                                  String oldPictureOriginalImageurl);

    /**
     * 将图片查询请求转为 QueryWrapper 对象
     * @param pictureQueryRequest 图片查询请求
     * @return 转换后的QueryWrapper 对象
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 为图片视图封装图片上传者信息
     * @param picture 图片对象
     * @return 完整图片视图对象
     */
    PictureVO getPictureVO(Picture picture);

    /**
     * 把原始图片page转换为视图图片page
     * @param picturePage 分页信息
     * @return 分页图片视图列表
     */
    Page<PictureVO> getPictureVoPage(Page<Picture> picturePage);

    /**
     * 图片校验,用于图片信息更新或者修改时
     * @param picture 原始图片信息
     */
    void validPicture(Picture picture);

    /**
     * 删除图片
     * @param deleteRequest 删除请求对象
     * @param request HTTP 请求对象
     * @return 是否删除成功
     */
    boolean deletePicture(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 校验并构建图片更新对象
     * @param request 更新请求
     * @param needCheckOwner 是否需要检查所有者
     * @return 构建好的图片对象
     */
    Picture validateAndBuildPictureUpdate(Object request,Boolean needCheckOwner,HttpServletRequest httpServletRequest);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser 审核人员
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 根据上传人员身份填充图片审核参数
     *
     * @param picture 图片对象
     * @param loginUser 登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 通过url上传图片
     * @param fileurl 图片url
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser 登录用户
     * @return 上传结果
     */
    PictureVO uploadPictureByUrl(String fileurl, PictureUploadRequest pictureUploadRequest, User loginUser);


    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest 批量上传请求体
     * @param loginUser                   登录用户,用于获取长传者信息
     * @return 成功创建的图片数
     */
    List<PictureVO> uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );

    /**
     * @param id 图片id
     */
    Picture getPictureById(Long id);


    /**
     * 失效单张图片缓存
     *
     * @param id 图片id
     */
    void invalidateSingerPicture(long id);
}
