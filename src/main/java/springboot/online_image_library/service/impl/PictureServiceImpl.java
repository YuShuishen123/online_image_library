package springboot.online_image_library.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.mapper.PictureMapper;
import springboot.online_image_library.modle.BO.UploadPictureResult;
import springboot.online_image_library.modle.dto.request.picture.PictureUploadRequest;
import springboot.online_image_library.modle.dto.vo.picture.PictureVO;
import springboot.online_image_library.modle.dto.vo.user.UserVO;
import springboot.online_image_library.modle.entiry.Picture;
import springboot.online_image_library.service.PictureService;
import springboot.online_image_library.utils.picture.FileUploadUtil;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

/**
* @author Yu'S'hui'shen
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-05-11 21:29:53
*/
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService {

    @Resource
    FileUploadUtil fileUploadUti;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile,
                                   PictureUploadRequest pictureUploadRequest,
                                   UserVO loginUserVO) {
        // 1. 参数校验
        ThrowUtils.throwIf(loginUserVO == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(),
                ErrorCode.PARAMS_ERROR, "请选择图片");

        // 2. 路径处理
        String uploadPathPrefix = String.format("public/%d", loginUserVO.getId());

        // 3. 区分新增/更新
        Picture picture;
        if (pictureUploadRequest.getId() != null) {
            // 更新操作：保留原ID
            picture = Optional.ofNullable(this.getById(pictureUploadRequest.getId()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在"));
            // 权限校验
            if (!loginUserVO.getId().equals(picture.getUserId())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "无权修改他人图片");
            }

        } else {
            // 新增操作
            picture = new Picture();
            picture.setCreateTime(new Date());
        }

        // 4. 文件上传
        UploadPictureResult uploadResult = fileUploadUti.uploadPicture(multipartFile, uploadPathPrefix);

        // 5. 映射字段
        picture.setName(uploadResult.getPicName());
        picture.setUrl(uploadResult.getUrl());
        picture.setPicSize(uploadResult.getPicSize());
        picture.setPicWidth(uploadResult.getPicWidth());
        picture.setPicHeight(uploadResult.getPicHeight());
        picture.setPicScale(uploadResult.getPicScale());
        picture.setPicFormat(uploadResult.getPicFormat());
        picture.setUserId(loginUserVO.getId());
        picture.setUpdateTime(new Date());

        // 重置更新时间(第一次上传的图片更新时间几十上传时间)
        picture.setEditTime(new Date());

        // 6. 保存
        if (!this.saveOrUpdate(picture)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片保存失败");
        }

        return PictureVO.objToVo(picture,loginUserVO);
    }
}




