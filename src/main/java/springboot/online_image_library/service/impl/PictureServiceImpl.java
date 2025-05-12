package springboot.online_image_library.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.common.DeleteRequest;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.manager.CosManager;
import springboot.online_image_library.mapper.PictureMapper;
import springboot.online_image_library.modle.BO.UploadPictureResult;
import springboot.online_image_library.modle.dto.request.picture.*;
import springboot.online_image_library.modle.dto.vo.picture.PictureVO;
import springboot.online_image_library.modle.dto.vo.user.UserVO;
import springboot.online_image_library.modle.entiry.Picture;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.modle.enums.PictureReviewStatusEnum;
import springboot.online_image_library.service.PictureService;
import springboot.online_image_library.service.UserService;
import springboot.online_image_library.utils.picture.FileUploadUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;


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
    private FileUploadUtil fileUploadUti;
    @Resource
    private UserService userService;
    @Resource
    private CosManager cosManager;
    @Resource
    private PictureMapper pictureMapper;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile,
                                   PictureUploadRequest pictureUploadRequest,
                                   User loginUser) {

        // 1. 参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(),
                ErrorCode.PARAMS_ERROR, "请选择图片");

        // 2. 路径处理
        String uploadPathPrefix = String.format("public/%d", loginUser.getId());

        // 3. 区分新增/更新
        Picture picture;
        if (pictureUploadRequest.getId() != null) {
            // 更新操作：保留原ID
            picture = Optional.ofNullable(this.getById(pictureUploadRequest.getId()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在"));
            // 权限校验
            if (!loginUser.getId().equals(picture.getUserId())) {
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
        picture.setUserId(loginUser.getId());
        picture.setUpdateTime(new Date());

        // 重置更新时间(第一次上传的图片更新时间既是上传时间)
        picture.setEditTime(new Date());

        // 补充审核信息
        fillReviewParams(picture, loginUser);

        // 6. 保存
        if (!this.saveOrUpdate(picture)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片保存失败");
        }
        // 设置上传者信息
        PictureVO pictureVO = PictureVO.objToVo(picture);
        pictureVO.setUser(userService.getUserVO(loginUser));
        return pictureVO;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }

        // 获取查询参数
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        // 多字段搜索（名称或简介）
        if (ObjectUtil.isNotEmpty(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText));
        }

        // 精确匹配字段
        queryWrapper.eq(ObjectUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtil.isNotEmpty(userId), "userId", userId);

        // 模糊匹配字段
        queryWrapper.like(ObjectUtil.isNotEmpty(name), "name", name);
        queryWrapper.like(ObjectUtil.isNotEmpty(introduction), "introduction", introduction);
        queryWrapper.like(ObjectUtil.isNotEmpty(picFormat), "picFormat", picFormat);

        // 其他字段
        queryWrapper.eq(ObjectUtil.isNotEmpty(category), "category", category);
        queryWrapper.eq(ObjectUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjectUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjectUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjectUtil.isNotEmpty(picScale), "picScale", picScale);

        // 标签查询（JSON数组格式）
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        // 排序
        queryWrapper.orderBy(ObjectUtil.isNotEmpty(sortField),
                "ascend".equals(sortOrder),
                sortField);

        return queryWrapper;
    }


    @Override
    public PictureVO getPictureVO(Picture picture) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        UserVO userVO = null;
        if (userId != null && userId > 0) {
            userVO = userService.getUserVO(userService.getById(userId));
        }
        pictureVO.setUser(userVO);
        // 对象转封装类
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVoPage(@NotNull Page<Picture> picturePage) {
        // 先获取到分页中的数据
        List<Picture> pictureList = picturePage.getRecords();
        // 创建一个记录类型为PictureVO类型的额page对象,同时把页码和大小以及总数先设置好
        Page<PictureVO> pictureVoPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        // 把对象列表封装为VO列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1.关联查询用户信息
        Set<Long> userIdset = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long,List<User>> userIdUserListMap = userService.listByIds(userIdset).stream().collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVoPage.setRecords(pictureVOList);
        return pictureVoPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjectUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (CharSequenceUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (CharSequenceUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public boolean deletePicture(DeleteRequest deleteRequest, HttpServletRequest request) {
        // 先判断 deleteRequest 是否为 null
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断图片是否存在
        Picture oldPicture = pictureMapper.selectById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        String url = oldPicture.getUrl();
        // 示例:https://image-1329241986.cos.ap-chengdu.myqcloud.com/public/1920415149873795074/6f7b6909-1ead-4e12-9d65-4952ba20cf14.jpg
        // 获取桶String,示例中为:image-1329241986
        int startIndexBucket = url.indexOf("://") + 3;
        int endIndexBucket = url.indexOf(".cos");
        String buketName = url.substring(startIndexBucket, endIndexBucket);
        // 获取key,示例中为: public/1920415149873795074/6f7b6909-1ead-4e12-9d65-4952ba20cf14.jpg
        int startIndexKey = url.indexOf("/", url.indexOf(".cos") + 4);
        String key = url.substring(startIndexKey + 1);
        // 获取当前登录用户
        var loginUser = userService.getLoginUser(request);

        // 仅本人或者管理员可以删除
        if (!oldPicture.getUserId().equals(loginUser.getId()) &&!userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 调用删除服务（假设 pictureService 有 delete 方法）
        // 1.数据库删除
        ThrowUtils.throwIf(pictureMapper.deleteById(id) != 1, ErrorCode.OPERATION_ERROR, "图片删除失败");
        // 2.存储桶内删除
        ThrowUtils.throwIf(!cosManager.deleteObject(buketName, key), ErrorCode.OPERATION_ERROR, "云端删除失败");
        return true;
    }


    @Override
    public Picture validateAndBuildPictureUpdate(Object request, Boolean needCheckOwner,HttpServletRequest httpServletRequest) {
        // 空值判断
        Long id = null;
        String name = null;
        String introduction = null;
        String category = null;
        List<String> tags = null;
        User loginUser = userService.getLoginUser(httpServletRequest);

        if (request instanceof PictureEditRequest) {
            PictureEditRequest editRequest = (PictureEditRequest) request;
            id = editRequest.getId();
            name = editRequest.getName();
            introduction = editRequest.getIntroduction();
            category = editRequest.getCategory();
            tags = editRequest.getTags();
        } else if (request instanceof PictureUpdateRequest) {
            PictureUpdateRequest updateRequest = (PictureUpdateRequest) request;
            id = updateRequest.getId();
            name = updateRequest.getName();
            introduction = updateRequest.getIntroduction();
            category = updateRequest.getCategory();
            tags = updateRequest.getTags();
        }

        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        // 检查至少有一个有效更新字段
        boolean hasValidUpdate = StringUtils.isNotBlank(name)
                || StringUtils.isNotBlank(introduction)
                || StringUtils.isNotBlank(category)
                || CollectionUtils.isNotEmpty(tags);
        ThrowUtils.throwIf(!hasValidUpdate, ErrorCode.PARAMS_ERROR, "至少需要一个有效更新字段");

        // 检查所有者权限
        if (Boolean.TRUE.equals(needCheckOwner)) {
            // 用户上传,需要校验是否为图片所拥有者
            Long oldPictureUserId = this.getById(id).getUserId();
            ThrowUtils.throwIf(!oldPictureUserId.equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR,"无权限修改");
        }
        // 构建图片对象
        Picture picture = new Picture();
        picture.setId(id);
        if (StringUtils.isNotBlank(name)) {
            picture.setName(name);
        }
        if (StringUtils.isNotBlank(introduction)) {
            picture.setIntroduction(introduction);
        }
        if (StringUtils.isNotBlank(category)) {
            picture.setCategory(category);
        }
        Optional.ofNullable(tags)
                .filter(CollectionUtils::isNotEmpty)
                .map(JSONUtil::toJsonStr)
                .ifPresent(picture::setTags);
        // 校验图片信息
        validPicture(picture);
        // 补充审核参数
        fillReviewParams(picture,loginUser);
        return picture;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 图片id必须存在且为有效值
        ThrowUtils.throwIf(pictureReviewRequest.getId() == null
                || pictureReviewRequest.getId() <= 0
                || pictureReviewRequest.getReviewStatus() == null,ErrorCode.PARAMS_ERROR);
        // 提交的审核状态不能是未通过
        ThrowUtils.throwIf(pictureReviewRequest.getReviewStatus().equals(PictureReviewStatusEnum.REVIEWING.getValue()),ErrorCode.PARAMS_ERROR,"提交状态不能为待审核");
        // 判断图片是否存在
        Picture oldPicture = getById(pictureReviewRequest.getId());
        ThrowUtils.throwIf(oldPicture == null,ErrorCode.NOT_FOUND_ERROR,"审核的图片不存在");
        // 提交状态不能与当前状态一致
        ThrowUtils.throwIf(pictureReviewRequest.getReviewStatus().equals(oldPicture.getReviewStatus()),ErrorCode.PARAMS_ERROR,"审核状态重复");
        // 新建一个Picture,用来赋值
        Picture newPicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, newPicture);
        // 保存管理人员id,以及审核时间
        newPicture.setReviewerId(loginUser.getId());
        newPicture.setReviewTime(new Date());
        // 操作数据库
        ThrowUtils.throwIf(!updateById(newPicture),ErrorCode.OPERATION_ERROR,"审核提交失败");
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        } else {
            // 非管理员，创建或编辑都要改为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }



}
