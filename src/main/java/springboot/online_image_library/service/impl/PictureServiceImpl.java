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
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.common.DeleteRequest;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.manager.AbstractCacheClient;
import springboot.online_image_library.mapper.PictureMapper;
import springboot.online_image_library.modle.BO.PictureUploadContext;
import springboot.online_image_library.modle.BO.UploadPictureResult;
import springboot.online_image_library.modle.dto.request.picture.*;
import springboot.online_image_library.modle.dto.vo.picture.PictureVO;
import springboot.online_image_library.modle.dto.vo.user.LoginState;
import springboot.online_image_library.modle.dto.vo.user.UserVO;
import springboot.online_image_library.modle.entiry.Picture;
import springboot.online_image_library.modle.entiry.Space;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.modle.enums.PictureReviewStatusEnum;
import springboot.online_image_library.service.PictureService;
import springboot.online_image_library.service.SpaceService;
import springboot.online_image_library.service.UserService;
import springboot.online_image_library.utils.picture.FileDeleteUtil;
import springboot.online_image_library.utils.picture.FileUploadUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
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
    private FileUploadUtil fileUploadUtil;
    @Resource
    private UserService userService;
    @Resource
    private PictureMapper pictureMapper;
    @Resource
    private FileDeleteUtil fileDeleteUtil;
    // 缓存单张图片键前缀
    private static final String SINGLE_IMAGE_CACHE = "picture:singleImageCache:";
    // 单张图片缓存过期时间
    private static final Duration SINGLE_IMAGE_CACHE_EXPIRE_TIME = Duration.ofSeconds((long) 15 * 60);
    @Resource(name = "cacheClient")
    AbstractCacheClient cacheClient;
    private final SpaceService spaceService;

    public PictureServiceImpl(SpaceService spaceService) {
        this.spaceService = spaceService;
    }


    /**
     * 处理图片上传的公共方法，支持本地文件上传和URL上传两种方式
     *
     * @param pictureUploadRequest 图片上传请求参数，包含图片ID（用于更新）和空间ID等信息
     * @param loginUser            当前登录用户，用于权限校验和设置图片所属用户
     * @param uploadPathPrefix     上传路径前缀，若为空则默认为 "public/{userId}"
     * @param isUrlUpload          是否为URL上传模式，true 表示通过URL上传，false 表示本地文件上传
     * @param fileurl              图片的URL地址（仅在 isUrlUpload 为 true 时使用）
     * @param multipartFile        上传的MultipartFile文件对象（仅在 isUrlUpload 为 false 时使用）
     * @return 返回处理后的 PictureVO 对象，包含上传结果信息
     */
    private PictureVO handleCommonPictureUpload(PictureUploadRequest pictureUploadRequest, LoginState loginUser,
                                                String uploadPathPrefix, boolean isUrlUpload, String fileurl, MultipartFile multipartFile) {
        // 1. 参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 根据上传方式校验参数
        if (isUrlUpload) {
            ThrowUtils.throwIf(fileurl == null || fileurl.isEmpty(),
                    ErrorCode.PARAMS_ERROR, "请填写图片url");
        } else {
            ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(),
                    ErrorCode.PARAMS_ERROR, "请选择图片");
        }

        long oldPictureSpaceId = 0;
        // 检验是否携带空间id
        if (pictureUploadRequest.getSpaceId() != null) {
            // 检验空间id参数是否合法
            ThrowUtils.throwIf(pictureUploadRequest.getSpaceId() <= 0, ErrorCode.PARAMS_ERROR, "空间id参数非法");
            // 判断空间是否存在
            Space space = spaceService.getById(pictureUploadRequest.getSpaceId());
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 判断权限,只有空间拥有者才能上传图片到空间
            ThrowUtils.throwIf(!spaceService.isSpaceOwner(loginUser, pictureUploadRequest.getSpaceId()),
                    ErrorCode.NO_AUTH_ERROR, "无权限上传图片到空间");
            // 赋值空间id
            oldPictureSpaceId = space.getId();
        }

        // 2. 路径处理
        String finalUploadPathPrefix = uploadPathPrefix != null ? uploadPathPrefix :
                String.format("public/%d", loginUser.getId());

        // 3. 区分新增/更新
        Picture picture;
        String oldPictureUrl = "";
        String oldPictureThumbnailUrl = "";
        String oldPictureOriginalImageurl = "";
        boolean isUpdate = pictureUploadRequest.getId() != null;
        if (isUpdate) {
            // 更新操作：保留原ID
            picture = Optional.ofNullable(this.getById(pictureUploadRequest.getId()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图片不存在"));
            // 判断图片空间ip和更新请求中的图片空间id是否相同
            ThrowUtils.throwIf(!picture.getSpaceId().equals(oldPictureSpaceId), ErrorCode.PARAMS_ERROR, "空间id不符合,上传失败");
            // 权限校验
            if (!loginUser.getId().equals(picture.getUserId())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "无权修改他人图片");
            }
            // 保留原始空间id
            oldPictureSpaceId = picture.getSpaceId();
            // 删除对象存储当中的旧的图片
            oldPictureUrl = picture.getUrl();
            oldPictureThumbnailUrl = picture.getThumbnailUrl();
            oldPictureOriginalImageurl = picture.getOriginalImageurl();
        } else {
            // 新增操作
            picture = new Picture();
            picture.setCreateTime(new Date());
        }

        // 设置图片名称（统一使用 StringUtils.isNotBlank）
        if (StringUtils.isNotBlank(pictureUploadRequest.getName())) {
            picture.setName(pictureUploadRequest.getName());
        }

        // 4. 文件上传（根据上传方式选择不同的方法）
        UploadPictureResult uploadResult = isUrlUpload ?
                fileUploadUtil.uploadPictureByUrl(fileurl, finalUploadPathPrefix) :
                fileUploadUtil.uploadPictureUtil(multipartFile, finalUploadPathPrefix);

        // 5. 处理上传结果
        return handlePictureUpload(PictureUploadContext.builder()
                .picture(picture)
                .uploadResult(uploadResult)
                .loginState(loginUser)
                .isUpdate(isUpdate)
                .oldPictureUrl(oldPictureUrl)
                .oldPictureThumbnailUrl(oldPictureThumbnailUrl)
                .oldPictureOriginalImageurl(oldPictureOriginalImageurl)
                .spaceId(oldPictureSpaceId)
                .build());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PictureVO uploadPictureByLocal(MultipartFile multipartFile,
                                          PictureUploadRequest pictureUploadRequest,
                                          LoginState loginState) {
        // 调用公共方法，传递本地上传的参数
        return handleCommonPictureUpload(pictureUploadRequest, loginState,
                String.format("public/%d", loginState.getId()), false, null, multipartFile);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PictureVO uploadPictureByUrl(String fileurl,
                                        PictureUploadRequest pictureUploadRequest,
                                        LoginState loginState) {
        // 调用公共方法，传递URL上传的参数
        return handleCommonPictureUpload(pictureUploadRequest, loginState,
                String.format("public/%d", loginState.getId()), true, fileurl, null);
    }

    @Override
    public List<PictureVO> uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, LoginState loginState) {
        // 参数校验
        fileUploadUtil.validateBatchUploadParams(pictureUploadByBatchRequest);

        // 获取并解析网页内容
        Document document = fileUploadUtil.fetchAndParseWebPage(pictureUploadByBatchRequest.getSearchText());

        // 提取图片元素
        List<FileUploadUtil.ImageData> imageDataList = fileUploadUtil.extractImageData(document);

        log.info(imageDataList.toString());

        // 上传图片
        return uploadImages(imageDataList, pictureUploadByBatchRequest.getCount(), loginState);
    }

    @Override
    public Picture getPictureById(Long id) {
        return cacheClient.query(
                SINGLE_IMAGE_CACHE + id,
                new TypeReference<>() {
                },
                SINGLE_IMAGE_CACHE_EXPIRE_TIME, () -> {
                    // 查询数据库
                    Picture pictureFromDb = pictureMapper.selectById(id);
                    ThrowUtils.throwIf(pictureFromDb == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
                    return pictureFromDb;
                });
    }


    /**
     * 上传图片
     *
     * @param imageDataList 图片数据列表
     * @param maxCount      最大上传数量
     * @param loginState     登录用户
     * @return 成功上传的数量
     */
    private List<PictureVO> uploadImages(List<FileUploadUtil.ImageData> imageDataList, int maxCount, LoginState loginState) {
        int uploadSuccessCount = 0;
        List<PictureVO> pictureVOList = new ArrayList<>();
        PictureUploadRequest uploadRequest = new PictureUploadRequest();
        for (FileUploadUtil.ImageData imageData : imageDataList) {
            // 检查是否已达到最大上传数量
            if (uploadSuccessCount >= maxCount) {
                break;
            }

            String fileUrl = imageData.getImageUrl();
            if (!CharSequenceUtil.isBlank(fileUrl)) {
                try {
                    uploadRequest.setName(imageData.getTitle());
                    // 使用注入的代理调用事务方法
                    // 从代理对象调用,绕过this调用
                    PictureVO pictureVO = ((PictureService) AopContext.currentProxy()).uploadPictureByUrl(fileUrl, uploadRequest, loginState);
                    pictureVOList.add(pictureVO);
                    log.info("第{}张图片上传成功, id = {},url = {}", uploadSuccessCount + 1, pictureVO.getId(), pictureVO.getUrl());
                    uploadSuccessCount++;
                } catch (Exception e) {
                    log.warn("图片上传失败, url: {}", fileUrl, e);
                }
            } else {
                log.info("当前链接为空，已跳过");
            }
        }
        log.info("图片上传完成: 成功 {} 张", uploadSuccessCount);
        return pictureVOList;
    }


    @Override
    public PictureVO handlePictureUpload(PictureUploadContext context) {
        // 从上下文对象中解构所需参数
        Picture picture = context.getPicture();
        UploadPictureResult uploadResult = context.getUploadResult();
        LoginState loginState = context.getLoginState();
        boolean isUpdate = context.isUpdate();
        String oldPictureUrl = context.getOldPictureUrl();
        String oldPictureThumbnailUrl = context.getOldPictureThumbnailUrl();
        String oldPictureOriginalImageurl = context.getOldPictureOriginalImageurl();
        long spaceId = context.getSpaceId();

        // 映射字段
        // 设置图片名称：优先使用传入的名称，否则使用上传结果的名称
        if (StringUtils.isNotBlank(picture.getName())) {
            picture.setName(picture.getName());
        } else {
            picture.setName(uploadResult.getPicName());
        }
        // 设置URL相关属性
        picture.setUrl(uploadResult.getUrl());
        picture.setThumbnailUrl(uploadResult.getThumbnailUrl());
        picture.setOriginalImageurl(uploadResult.getOriginalImageurl());
        // 设置尺寸属性
        picture.setPicSize(uploadResult.getPicSize());
        picture.setPicWidth(uploadResult.getPicWidth());
        picture.setPicHeight(uploadResult.getPicHeight());
        // 设置格式属性
        picture.setPicScale(uploadResult.getPicScale());
        picture.setPicFormat(uploadResult.getPicFormat());
        // 设置系统字段
        picture.setUserId(loginState.getId());
        picture.setUpdateTime(new Date());
        picture.setSpaceId(spaceId);

        // 重置更新时间
        picture.setEditTime(new Date());

        // 补充审核信息（如审核状态等）
        fillReviewParams(picture, loginState);

        // 保存图片实体到数据库
        if (!this.saveOrUpdate(picture)) {
            // 保存失败时抛出业务异常
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片保存失败");
        }
        // 存入缓存
        // 1. 获取缓存键（使用图片ID构建）
        String pictureUid = SINGLE_IMAGE_CACHE + picture.getId();
        // 2. 存入缓存（设置过期时间）
        cacheClient.update(pictureUid, picture, SINGLE_IMAGE_CACHE_EXPIRE_TIME);
        // 如果是更新操作且存在旧图片URL，尝试删除旧图片
        // 检查旧URL是否被其他图片引用（避免误删）
        if (isUpdate && pictureMapper.selectCount(new QueryWrapper<Picture>().ne("id", picture.getId()).eq("url", oldPictureUrl)) == 0) {
            // 异步删除存储桶中的旧图片文件
            fileDeleteUtil.deleteFile(oldPictureUrl);
            fileDeleteUtil.deleteFile(oldPictureThumbnailUrl);
            fileDeleteUtil.deleteFile(oldPictureOriginalImageurl);
        }
        // 异步更新空间信息（增加空间已用容量）
        spaceService.asyncUpdateSpacePictureInfo(spaceId, uploadResult.getPicSize(), true);
        // 转换实体为VO对象并设置上传者信息
        PictureVO pictureVO = PictureVO.objToVo(picture);
        UserVO userVO = userService.getUserVO(userService.getById(loginState.getId()));
        pictureVO.setUser(userVO);
        return pictureVO;
    }


    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest request, long spaceId) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (request == null) {
            return queryWrapper;
        }

        // 基础查询条件
        applyBasicConditions(queryWrapper, request, spaceId);
        // 模糊搜索条件
        applySearchCondition(queryWrapper, request.getSearchText());
        // 标签查询条件
        applyTagConditions(queryWrapper, request.getTags());
        // 排序条件
        applySortCondition(queryWrapper, request.getSortField(), request.getSortOrder());

        return queryWrapper;
    }

    /**
     * 应用基础查询条件
     *
     * @param wrapper 查询条件包装器
     * @param request 图片查询请求参数
     * @param spaceId 空间ID
     */
    private void applyBasicConditions(QueryWrapper<Picture> wrapper, PictureQueryRequest request, long spaceId) {
        // 空间ID条件
        wrapper.eq("spaceId", spaceId);

        // 精确匹配字段
        wrapper.eq(ObjectUtil.isNotEmpty(request.getId()), "id", request.getId())
                .eq(ObjectUtil.isNotEmpty(request.getUserId()), "userId", request.getUserId())
                .eq(ObjectUtil.isNotEmpty(request.getCategory()), "category", request.getCategory())
                .eq(ObjectUtil.isNotEmpty(request.getPicWidth()), "picWidth", request.getPicWidth())
                .eq(ObjectUtil.isNotEmpty(request.getPicHeight()), "picHeight", request.getPicHeight())
                .eq(ObjectUtil.isNotEmpty(request.getPicSize()), "picSize", request.getPicSize())
                .eq(ObjectUtil.isNotEmpty(request.getPicScale()), "picScale", request.getPicScale())
                .eq(ObjectUtil.isNotEmpty(request.getReviewStatus()), "reviewStatus", request.getReviewStatus())
                .eq(ObjectUtil.isNotEmpty(request.getReviewerId()), "reviewerId", request.getReviewerId());

        // 模糊匹配字段
        wrapper.like(ObjectUtil.isNotEmpty(request.getName()), "name", request.getName())
                .like(ObjectUtil.isNotEmpty(request.getIntroduction()), "introduction", request.getIntroduction())
                .like(ObjectUtil.isNotEmpty(request.getPicFormat()), "picFormat", request.getPicFormat())
                .like(ObjectUtil.isNotEmpty(request.getReviewMessage()), "reviewMessage", request.getReviewMessage());
    }

    /**
     * 应用搜索条件
     *
     * @param wrapper    查询条件包装器
     * @param searchText 搜索文本
     */
    private void applySearchCondition(QueryWrapper<Picture> wrapper, String searchText) {
        if (ObjectUtil.isNotEmpty(searchText)) {
            wrapper.and(qw -> qw.like("name", searchText).or().like("introduction", searchText));
        }
    }

    /**
     * 应用标签查询条件
     *
     * @param wrapper  查询条件包装器
     * @param tags     标签列表
     */
    private void applyTagConditions(QueryWrapper<Picture> wrapper, List<String> tags) {
        if (CollUtil.isNotEmpty(tags)) {
            tags.forEach(tag -> wrapper.like("tags", "\"" + tag + "\""));
        }
    }

    /**
     * 应用排序条件
     *
     * @param wrapper     查询条件包装器
     * @param sortField   排序字段
     * @param sortOrder   排序方式
     */
    private void applySortCondition(QueryWrapper<Picture> wrapper, String sortField, String sortOrder) {
        wrapper.orderBy(ObjectUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
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
        // 判断pictureList是否为空
        if (CollUtil.isEmpty(pictureList)) {
            // 为空则直接返回空
            return new Page<>();
        }
        // 创建一个记录类型为PictureVO类型的额page对象,同时把页码和大小以及总数先设置好
        Page<PictureVO> pictureVoPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        // 把对象列表封装为VO列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).toList();
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
    @Transactional
    public boolean deletePicture(DeleteRequest deleteRequest, HttpServletRequest request) {
        // 先判断 deleteRequest 是否为 null
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断图片是否存在
        Picture oldPicture = getPictureById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        String url = oldPicture.getUrl();
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        String originalImageurl = oldPicture.getOriginalImageurl();
        // 获取当前登录用户
        LoginState loginState = userService.getLoginState(request);

        // 仅本人或者管理员可以删除
        if (!oldPicture.getUserId().equals(loginState.getId()) && !userService.isAdmin(loginState)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 1.数据库删除
        ThrowUtils.throwIf(pictureMapper.deleteById(id) != 1, ErrorCode.OPERATION_ERROR, "图片删除失败");

        // 2.如果空间id不为空,说明图片属于某一私有空间,更新空间容量和图片数量信息
        if (oldPicture.getSpaceId() != null && oldPicture.getSpaceId() > 0) {
            spaceService.asyncUpdateSpacePictureInfo(oldPicture.getSpaceId(), oldPicture.getPicSize(), false);
        }

        // 3.存储桶内删除(异步删除)
        if (pictureMapper.selectCount(new QueryWrapper<Picture>().eq("url", url)) == 0) {
            fileDeleteUtil.deleteFile(url);
            fileDeleteUtil.deleteFile(thumbnailUrl);
            fileDeleteUtil.deleteFile(originalImageurl);
        }
        return true;
    }


    /**
     * 构建并校验图片更新请求
     *
     * @param request            请求体（支持 PictureEditRequest / PictureUpdateRequest）
     * @param needCheckOwner     是否需要校验用户是否是图片所有者
     * @param httpServletRequest 请求上下文（用于获取当前用户）
     * @return 构建完成的 Picture 对象，用于更新
     */
    @Override
    public Picture validateAndBuildPictureUpdate(Object request, Boolean needCheckOwner, HttpServletRequest httpServletRequest) {
        // 提取参数
        Long id = null;
        String name = null;
        String introduction = null;
        String category = null;
        List<String> tags = null;
        Long spaceId = null;

        if (request instanceof PictureEditRequest editRequest) {
            id = editRequest.getId();
            name = editRequest.getName();
            introduction = editRequest.getIntroduction();
            category = editRequest.getCategory();
            tags = editRequest.getTags();
            spaceId = editRequest.getSpaceId();
        } else if (request instanceof PictureUpdateRequest updateRequest) {
            id = updateRequest.getId();
            name = updateRequest.getName();
            introduction = updateRequest.getIntroduction();
            category = updateRequest.getCategory();
            tags = updateRequest.getTags();
        }

        // 参数校验
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(spaceId != null && spaceId < 0, ErrorCode.PARAMS_ERROR);

        boolean hasValidUpdate = StringUtils.isNotBlank(name)
                || StringUtils.isNotBlank(introduction)
                || StringUtils.isNotBlank(category)
                || CollectionUtils.isNotEmpty(tags);
        ThrowUtils.throwIf(!hasValidUpdate, ErrorCode.PARAMS_ERROR, "至少需要一个有效更新字段");

        LoginState loginState = userService.getLoginState(httpServletRequest);
        Picture oldPicture = getPictureById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        // 权限校验
        if (Boolean.TRUE.equals(needCheckOwner)) {
            ThrowUtils.throwIf(!oldPicture.getUserId().equals(loginState.getId()), ErrorCode.NO_AUTH_ERROR, "无权限修改");
        }

        // 空间处理
        if (spaceId != null) {
            handleSpaceChange(oldPicture, spaceId, loginState);
        }

        // 构建 picture 对象
        Picture picture = new Picture();
        picture.setId(id);
        if (StringUtils.isNotBlank(name)) picture.setName(name);
        if (StringUtils.isNotBlank(introduction)) picture.setIntroduction(introduction);
        if (StringUtils.isNotBlank(category)) picture.setCategory(category);
        Optional.ofNullable(tags)
                .filter(CollectionUtils::isNotEmpty)
                .map(JSONUtil::toJsonStr)
                .ifPresent(picture::setTags);
        if (spaceId != null) picture.setSpaceId(spaceId == 0 ? 0L : spaceId);
        // 图片合法性校验
        validPicture(picture);
        fillReviewParams(picture, loginState);
        return picture;
    }

    /**
     * 处理图片空间变更逻辑
     *
     * @param oldPicture 旧图片对象
     * @param newSpaceId 新空间 ID
     * @param loginState  当前登录用户
     */
    private void handleSpaceChange(Picture oldPicture, Long newSpaceId, LoginState loginState) {
        Long oldSpaceId = oldPicture.getSpaceId();
        Long picSize = oldPicture.getPicSize();

        // 移入私有空间
        if (oldSpaceId == 0 && newSpaceId != 0) {
            log.info("移入私有空间");
            Space space = getAndCheckSpaceOwnership(newSpaceId, loginState);
            ThrowUtils.throwIf(space.getTotalSize() >= space.getMaxSize(), ErrorCode.OPERATION_ERROR, "空间容量不足");
            spaceService.asyncUpdateSpacePictureInfo(newSpaceId, picSize, true);
        }

        // 相同空间（非0）报错
        else if (oldSpaceId.equals(newSpaceId)) {
            log.info("相同空间");
            ThrowUtils.throwIf(newSpaceId != 0, ErrorCode.OPERATION_ERROR, "图片已属于该空间");
        }

        // 移出到公共空间
        else if (newSpaceId == 0) {
            log.info("移到公共空间");
            Space oldSpace = getAndCheckSpaceOwnership(oldSpaceId, loginState);
            ThrowUtils.throwIf(!oldSpace.getUserId().equals(loginState.getId()), ErrorCode.NO_AUTH_ERROR, "无权限修改");
            spaceService.asyncUpdateSpacePictureInfo(oldSpaceId, picSize, false);
        }
    }

    /**
     * 检查空间存在性和拥有权
     *
     * @param spaceId   空间 ID
     * @param loginState 当前用户
     * @return 合法的空间对象
     */
    private Space getAndCheckSpaceOwnership(Long spaceId, LoginState loginState) {
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtils.throwIf(!space.getUserId().equals(loginState.getId()), ErrorCode.NO_AUTH_ERROR, "无权限修改");
        return space;
    }


    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, LoginState loginState) {
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
        newPicture.setReviewerId(loginState.getId());
        newPicture.setReviewTime(new Date());
        // 操作数据库
        ThrowUtils.throwIf(!updateById(newPicture),ErrorCode.OPERATION_ERROR,"审核提交失败");
    }

    @Override
    public void fillReviewParams(Picture picture, LoginState loginState) {
        if (userService.isAdmin(loginState)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginState.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        } else {
            // 非管理员，创建或编辑都要改为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public void invalidateSingerPicture(long id) {
        cacheClient.invalidate(SINGLE_IMAGE_CACHE + id);
    }


}
