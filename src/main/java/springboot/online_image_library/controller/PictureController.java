package springboot.online_image_library.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.annotation.AuthCheck;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.ResultUtils;
import springboot.online_image_library.constant.UserConstants;
import springboot.online_image_library.modle.dto.request.picture.PictureUploadRequest;
import springboot.online_image_library.modle.dto.vo.picture.PictureVO;
import springboot.online_image_library.modle.dto.vo.user.UserVO;
import springboot.online_image_library.service.PictureService;
import springboot.online_image_library.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/12
 * @description 图片相关控制器类
 */
@Api(tags = "图片相关操作")
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    PictureService pictureService;

    @Resource
    private UserService userService;

    /**
     * 上传图片（可重新上传）
     */
    @ApiOperation(
            value = "图片更新或上传",
            notes = "用于图片更新或上传,限制图片最大为8MB",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        UserVO loginUserVO = userService.getUserVO(userService.getLoginUser(request));
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUserVO);
        return ResultUtils.success(pictureVO);
    }

}
