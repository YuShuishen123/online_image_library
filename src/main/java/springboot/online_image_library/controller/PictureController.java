package springboot.online_image_library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.annotation.AuthCheck;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.CacheScheduleService;
import springboot.online_image_library.common.DeleteRequest;
import springboot.online_image_library.common.ResultUtils;
import springboot.online_image_library.constant.UserConstants;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.manager.CacheClient;
import springboot.online_image_library.modle.BO.PictureTagCategory;
import springboot.online_image_library.modle.dto.request.picture.*;
import springboot.online_image_library.modle.dto.vo.picture.PictureVO;
import springboot.online_image_library.modle.entiry.Picture;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.modle.enums.PictureReviewStatusEnum;
import springboot.online_image_library.service.PictureService;
import springboot.online_image_library.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/12
 * @description 图片相关控制器类
 */
@Api(tags = "PictureController")
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
    @Resource
    CacheClient cacheClient;
    @Resource
    private CacheScheduleService cacheScheduleService;

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
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 根据url上传图片
     */
    @ApiOperation(
            value = "根据url上传图片",
            notes = "用于图片上传功能,限制图片最大为8MB",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/upload/url")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
    public BaseResponse<PictureVO> uploadPictureByUrl(
            PictureUploadRequest pictureUploadRequest,
            String fileurl,
            HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(pictureService.uploadPictureByUrl(fileurl,pictureUploadRequest,loginUser));
    }


    @ApiOperation(
            value = "批量抓取图片",
            notes = "用于批量抓取图片功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<List<PictureVO>> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> pictureVOList = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(pictureVOList);
    }

    /**
     * 删除图片
     */
    @ApiOperation(
            value = "删除图片(管理员)",
            notes = "用于删除图片功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
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
    @ApiOperation(
            value = "根据id获取图片信息(管理员)",
            notes = "用于管理员获取图片信息功能",
            httpMethod = "GET",
            response = BaseResponse.class
    )
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
    @ApiOperation(
            value = "根据id获取图片信息(用户)",
            notes = "用于用户获取图片信息功能",
            httpMethod = "GET",
            response = BaseResponse.class
    )
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

    @ApiOperation(
            value = "分页获取图片列表(管理员)",
            notes = "用于管理员获取图片列表功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/list/admin/page")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest){
        // 提取分页相关信息
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 建立分页对象
        Page<Picture> picturePage = pictureService.page(new Page<>(current,size),pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表
     */
    @ApiOperation(
            value = "分页获取图片列表(用户)",
            notes = "用于用户获取图片列表功能,普通用户只能查看审核状态为已通过的图片(强制)",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/list/page")
    public BaseResponse<Page<PictureVO>> listPictureVoByPage(@RequestBody PictureQueryRequest pictureQueryRequest){
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
        Page<PictureVO> pictureVoPage = cacheClient.queryWithCache(
                cacheKey,
                new TypeReference<>() {
                },
                Duration.ofMinutes(TTL_MINUTES),
                () -> {
                    // 数据库查询逻辑
                    Page<Picture> picturePage = pictureService.page(
                            new Page<>(current, size),
                            pictureService.getQueryWrapper(pictureQueryRequest)
                    );
                    return pictureService.getPictureVoPage(picturePage);
                }
        );
        return ResultUtils.success(pictureVoPage);
    }


    @ApiOperation(
            value = "获取图片标签和分类",
            notes = "用于获取图片标签和分类",
            httpMethod = "GET",
            response = BaseResponse.class
    )
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

    @ApiOperation(
            value = "更新图片信息(用户端)",
            notes = "用于更新图片功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/edit")
    @AuthCheck(mustRole = UserConstants.DEFAULT_ROLE)
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

    @ApiOperation(
            value = "更新图片信息(管理员端)",
            notes = "用于更新图片功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePictureInfo(@RequestBody PictureUpdateRequest pictureUpdateRequest,HttpServletRequest httpServletRequest) {
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
    @ApiOperation(
            value = "图片审核接口(仅管理员)",
            notes = "用于图片审核功能",
            httpMethod = "POST",
            response = BaseResponse.class
    )
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }
}

