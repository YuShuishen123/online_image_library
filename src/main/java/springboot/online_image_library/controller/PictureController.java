package springboot.online_image_library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.annotation.AuthCheck;
import springboot.online_image_library.annotation.IdempotentCheck;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.CacheScheduleService;
import springboot.online_image_library.common.DeleteRequest;
import springboot.online_image_library.common.ResultUtils;
import springboot.online_image_library.constant.UserConstants;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.manager.AbstractCacheClient;
import springboot.online_image_library.modle.BO.PictureTagCategory;
import springboot.online_image_library.modle.dto.request.picture.*;
import springboot.online_image_library.modle.dto.vo.picture.PictureVO;
import springboot.online_image_library.modle.dto.vo.user.LoginState;
import springboot.online_image_library.modle.entiry.Picture;
import springboot.online_image_library.modle.entiry.Space;
import springboot.online_image_library.modle.enums.PictureReviewStatusEnum;
import springboot.online_image_library.service.PictureService;
import springboot.online_image_library.service.SpaceService;
import springboot.online_image_library.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/12
 * @description 图片相关控制器类
 */
@Tag(name = "PictureController", description = "图片相关控制器类")
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;
    private static final String TAG_LIST_KEY = "picture:tag_list";
    private static final String CATEGORY_LIST_KEY = "picture:category_list";
    private static final String PICTUREVO_QUERY_KEY = "picture:picturevo_query_list:";
    private static final Long TTL_MINUTES = (long) 10;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    // 二级缓存
    @Resource(name = "cacheClient")
    AbstractCacheClient cacheClient;
    // 一级缓存
    @Resource(name = "cacheClientNoLocal")
    AbstractCacheClient cacheClientNoLocal;
    @Resource
    private CacheScheduleService cacheScheduleService;
    @Resource
    private SpaceService spaceService;

    /**
     * 上传图片（可重新上传）
     */
    @Operation(
            summary = "从本地上传或更新图片",
            description = "用于从本地更新或上传图片,限制图片最大为8MB",
            method = "POST")
    @PostMapping(value = "/upload")
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        LoginState loginState = userService.getLoginState(request);
        PictureVO pictureVO = pictureService.uploadPictureByLocal(multipartFile, pictureUploadRequest, loginState);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 根据url上传图片
     */
    @Operation(
            summary = "根据url上传图片",
            description = "用于图片上传功能,限制图片最大为8MB",
            method = "POST")
    @PostMapping("/upload/url")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    public BaseResponse<PictureVO> uploadPictureByUrl(
            PictureUploadRequest pictureUploadRequest,
            String fileurl,
            HttpServletRequest request){
        LoginState loginState = userService.getLoginState(request);
        return ResultUtils.success(pictureService.uploadPictureByUrl(fileurl, pictureUploadRequest, loginState));
    }

    @Operation(
            summary = "批量抓取图片",
            description = "用于批量抓取图片功能",
            method = "POST")
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<List<PictureVO>> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        LoginState loginState = userService.getLoginState(request);
        List<PictureVO> pictureVOList = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginState);
        return ResultUtils.success(pictureVOList);
    }

    /**
     * 删除图片
     */
    @Operation(
            summary = "删除图片",
            description = "用于删除图片功能",
            method = "POST")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        boolean result = pictureService.deletePicture(deleteRequest, request);
        // 异步删除缓存
        if (result) {
            log.debug("删除图片成功,开始删除缓存");
            cacheScheduleService.scheduleInvalidateForSingePicture(deleteRequest.getId(), 0, pictureService);
        }
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取图片信息(仅管理员可用)
     */
    @Operation(
            summary = "根据id获取图片信息(管理员)",
            description = "用于管理员获取图片信息功能",
            method = "GET")
    @GetMapping("/admin/get")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<Picture>  getPictureById(Long id){
        ThrowUtils.throwIf(id == null || id <= 0,ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getPictureById(id);
        return ResultUtils.success(picture);
    }

    /**
     * 根据id获取图片信息(用户可用)
     */
    @Operation(
            summary = "根据id获取图片信息(用户)",
            description = "用于用户获取图片信息功能",
            method = "GET")
    @GetMapping("/get")
    public BaseResponse<PictureVO>  getPictureVoById(Long id){
        ThrowUtils.throwIf(id == null || id <= 0,ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getPictureById(id);
        // 返回VO数据
        return ResultUtils.success(pictureService.getPictureVO(picture));
    }

    /**
     * 分页获取图片列表
     */

    @Operation(
            summary = "分页获取图片列表(管理员)",
            description = "用于管理员获取图片列表功能",
            method = "POST")
    @PostMapping("/list/admin/page")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPicturePage(@RequestBody PictureQueryRequest pictureQueryRequest) {

        // 根据pictureQueryRequest,生成一个唯一的key
        String cacheKey = PICTUREVO_QUERY_KEY + pictureQueryRequest.generateCacheKey();

        // 提取分页相关信息
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();

        // 使用一级缓存管理器查询数据
        Page<Picture> picturePage = cacheClientNoLocal.query(
                cacheKey,
                new TypeReference<>() {
                },
                Duration.ofMinutes(TTL_MINUTES),
                () -> {
                    // 数据库查询逻辑
                    return pictureService.page(
                            new Page<>(current, size),
                            pictureService.getQueryWrapper(pictureQueryRequest, 0)
                    );
                }
        );
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表
     */
    @Operation(
            summary = "分页获取图片列表(用户)",
            description = "用于用户获取图片列表功能,普通用户只能查看审核状态为已通过的图片(强制)",
            method = "POST")
    @PostMapping("/list/page")
    public BaseResponse<Page<PictureVO>> listPictureVoPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        // 提取分页相关信息
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();

        // 限制爬虫,限制每次最多获取20条数据
        ThrowUtils.throwIf(size > 24,ErrorCode.PARAMS_ERROR);

        // 普通用户只能查看审核状态为已通过的图片(强制)
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 根据pictureQueryRequest,生成一个唯一的key
        String cacheKey = PICTUREVO_QUERY_KEY + pictureQueryRequest.generateCacheKey();

        // 使用多级缓存管理器查询数据
        Page<PictureVO> pictureVoPage = cacheClient.query(
                cacheKey,
                new TypeReference<>() {
                },
                Duration.ofMinutes(TTL_MINUTES),
                () -> {
                    // 数据库查询逻辑
                    Page<Picture> picturePage = pictureService.page(
                            new Page<>(current, size),
                            pictureService.getQueryWrapper(pictureQueryRequest, 0)
                    );
                    return pictureService.getPictureVoPage(picturePage);
                }
        );
        return ResultUtils.success(pictureVoPage);
    }


    @Operation(
            summary = "获取图片标签和分类",
            description = "用于获取图片标签和分类",
            method = "GET")
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();

        // 尝试从 Redis 获取 tagList
        List<String> tagList = redisTemplate.opsForList().range(TAG_LIST_KEY, 0, -1);
        if (tagList != null && !tagList.isEmpty()) {
            log.info("从 Redis 获取 tagList 成功");
            pictureTagCategory.setTagList(tagList);
        } else {
            // 回退到原逻辑
            tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
            // 存入 Redis
            redisTemplate.opsForList().rightPushAll(TAG_LIST_KEY, tagList);
            pictureTagCategory.setTagList(tagList);
        }
        // 尝试从 Redis 获取 categoryList
        List<String> categoryList = redisTemplate.opsForList().range(CATEGORY_LIST_KEY, 0, -1);
        if (categoryList != null && !categoryList.isEmpty()) {
            log.info("从 Redis 获取 categoryList 成功");
            pictureTagCategory.setCategoryList(categoryList);
        } else {
            // 回退到原逻辑
            categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
            // 存入 Redis
            redisTemplate.opsForList().rightPushAll(CATEGORY_LIST_KEY, categoryList);
            pictureTagCategory.setCategoryList(categoryList);
        }
        return ResultUtils.success(pictureTagCategory);
    }

    @Operation(
            summary = "更新图片信息(用户端)",
            description = "用于更新图片功能",
            method = "POST")
    @PostMapping("/edit")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    @IdempotentCheck(timeOut = 5, timeUnit = TimeUnit.SECONDS)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest,
                                             HttpServletRequest request) {
        Picture picture = pictureService.validateAndBuildPictureUpdate(
                pictureEditRequest,true, request);
        // 第一次删除缓存
        pictureService.invalidateSingerPicture(picture.getId());
        // 操作数据库
        ThrowUtils.throwIf(!pictureService.updateById(picture),
                ErrorCode.OPERATION_ERROR, "更新失败");
        // 第二次延迟删除缓存
        cacheScheduleService.scheduleInvalidateForSingePicture(picture.getId(), 500, pictureService);
        return ResultUtils.success(true);
    }

    @Operation(
            summary = "更新图片信息(管理员端)",
            description = "用于更新图片功能",
            method = "POST")
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePictureInfo(@RequestBody PictureUpdateRequest pictureUpdateRequest,HttpServletRequest httpServletRequest) {
        // 权限验证并且构建数据库更新字段
        Picture picture = pictureService.validateAndBuildPictureUpdate(
                pictureUpdateRequest,false, httpServletRequest);
        // 第一次删除缓存
        pictureService.invalidateSingerPicture(picture.getId());
        // 操作数据库
        ThrowUtils.throwIf(!pictureService.updateById(picture),
                ErrorCode.OPERATION_ERROR, "更新失败");
        // 第二次延迟删除缓存
        cacheScheduleService.scheduleInvalidateForSingePicture(picture.getId(), 500, pictureService);
        return ResultUtils.success(true);
    }

    /**
     * 图片审核接口
     */
    @Operation(
            summary = "图片审核接口(仅管理员)",
            description = "用于图片审核功能",
            method = "POST")
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        LoginState loginState = userService.getLoginState(request);
        pictureService.doPictureReview(pictureReviewRequest, loginState);
        return ResultUtils.success(true);
    }


    /**
     * 获取当前用户个人空间内的所有图片,仅限自己获取
     */
    @Operation(
            summary = "查询空间内所有图片(分页)",
            description = "用于分页查询当前用户个人空间内的所有图片(仅限自己获取)",
            method = "POST")
    @PostMapping("/user/picture")
//    @IdempotentCheck(timeOut = 5, timeUnit = TimeUnit.SECONDS)
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    public BaseResponse<Page<Picture>> listSpacePicturePage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest httpServletRequest) {
        // 提取分页相关信息
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 获取登陆用户
        LoginState loginState = userService.getLoginState(httpServletRequest);
        // 获取用户空间
        Space space = spaceService.getUserSpaceFromLogUser(loginState);
        // 进行分页查询
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest, space.getId()));
        // 返回Picture数据
        return ResultUtils.success(picturePage);

    }
}

