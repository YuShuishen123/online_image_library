package springboot.online_image_library.utils.picture;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.http.*;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.config.CosClientConfig;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.manager.CosManager;
import springboot.online_image_library.modle.BO.UploadPictureResult;
import springboot.online_image_library.modle.dto.request.picture.PictureUploadByBatchRequest;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.function.Function;

/**
 * @author Yu'S'hui'shen
 * 文件长传工具类
 */
@Slf4j
@Component
public class FileUploadUtil {

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 处理文件上传的公共逻辑
     * @param multipartFile 上传的文件
     * @param basePath 存储的基础路径
     * @param fileProcessor 文件处理函数式接口
     * @return 处理结果
     */
    private <T> T handleFileUpload(MultipartFile multipartFile, String basePath, Function<FileUploadContext, T> fileProcessor) {
        // 校验图片
        validPicture(multipartFile);
        // 校验文件名
        String originalFilename = Optional.ofNullable(multipartFile.getOriginalFilename())
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空"));

        // 生成唯一文件名（UUID + 后缀）
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID() + fileExtension;

        // 拼接传入的前缀和此处生成的唯一文件名,构建存储路径
        String filePath = String.format("%s/%s", basePath, uniqueFileName);

        // 处理临时文件进行上传,并且提取上传所需的参数
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload_", fileExtension);
            multipartFile.transferTo(tempFile);
            // 使用上下文对象封装参数
            FileUploadContext context = new FileUploadContext(filePath, tempFile, originalFilename, multipartFile);
            return fileProcessor.apply(context);
        } catch (Exception ex) {
            log.error("文件上传失败: {}", originalFilename, ex);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败");
        } finally {
            deleteTempFile(tempFile);
        }
    }

    /**
     * 根据图片文件和基础路径进行上传
     *
     * @param multipartFile 图片文件
     * @param basePath      基础路径
     * @return 封装后的上传结果
     */
    public UploadPictureResult uploadPictureUtil(MultipartFile multipartFile, String basePath) {
        return handleFileUpload(multipartFile, basePath, context -> {
            // 调用cosManager,通过multipartFile和filePath进行上传
            PutObjectResult putObjectResult = cosManager.putPictureObject(context.filePath, context.tempFile);
            // 获取上传返回的图片信息(包含图片处理结果等信息)
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 获取调用图片处理(目前两种处理方法:格式变换,生成缩略图)所单独产生的处理结果集
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            // 调用图片处理集处理方法,对应图片处理字段的进行封装
            UploadPictureResult result = processingImageProcessingResults(processResults);
            // 填写图片信息(此处为原始图片信息,非缩略图或格式转换后的图片信息)
            result.setOriginalImageurl(cosClientConfig.getHost() + "/" + context.filePath);
            result.setPicName(context.originalFilename);
            result.setPicFormat(imageInfo.getFormat());
            result.setPicHeight(imageInfo.getHeight());
            result.setPicWidth(imageInfo.getWidth());
            result.setPicScale(NumberUtil.round(imageInfo.getWidth() * 1.0 / imageInfo.getHeight(), 2).doubleValue());
            result.setPicSize(context.multipartFile.getSize());
            return result;
        });
    }

    // 重构后的文件上传方法
    public String uploadFile(MultipartFile multipartFile, String basePath) {
        return handleFileUpload(multipartFile, basePath, context -> {
            cosManager.putObject(context.filePath, context.tempFile);
            return context.filePath;
        });
    }


    /**
     * 构建上传图片结果信息，将压缩图和缩略图的CIObject对象存入UploadPictureResult
     *
     * @param compressedCiObject 压缩图的CIObject对象（如webp格式）
     * @param thumbnailCiObject  缩略图的CIObject对象
     * @return 返回封装好的UploadPictureResult对象，包含压缩图和缩略图的URL信息
     */
    private UploadPictureResult buildCiObjectResult(CIObject compressedCiObject, CIObject thumbnailCiObject) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        // 设置图片为压缩后的地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        return uploadPictureResult;
    }

    /**
     * 通过URL上传图片
     * @param fileurl 图片的URL
     * @param basePath 存储的基础路径
     * @return 上传结果
     */
    public UploadPictureResult uploadPictureByUrl(String fileurl, String basePath) {
        // 校验URL及其文件类型和大小
        validurl(fileurl);
        // 获取文件扩展名
        String fileExtension = FileUtil.getSuffix(fileurl);
        String originalFilename = fileurl.substring(fileurl.lastIndexOf("/") + 1);
        // 处理扩展名：如果没有扩展名或不是标准图片格式，则添加JPEG(暂时解决必应上传图片无后缀的方法)
        if (fileExtension.isEmpty() || !isImageExtension(fileExtension)) {
            fileExtension = "jpeg";
        }
        // 生成唯一文件名
        String uniqueFileName = UUID.randomUUID() + "." + fileExtension;

        // 构建存储路径
        String filePath = String.format("%s/%s", basePath, uniqueFileName);

        // 构建临时文件
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload_", "." + fileExtension);
            HttpUtil.downloadFile(fileurl, tempFile);
            // 校验文件大小（再次确认）
            long fileSize = tempFile.length();
            long maxFileSize = 8 * 1024 * 1024L;
            ThrowUtils.throwIf(fileSize > maxFileSize, ErrorCode.PARAMS_ERROR, "图片大小不能大于8MB");
            // 上传到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, tempFile);
            // 提取图片上传后的基本信息以及处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            UploadPictureResult result = processingImageProcessingResults(processResults);
            // 设置图片内容(以原图为默认)
            result.setOriginalImageurl(cosClientConfig.getHost() + "/" + filePath);
            result.setPicName(originalFilename);
            result.setPicFormat(imageInfo.getFormat());
            result.setPicHeight(imageInfo.getHeight());
            result.setPicWidth(imageInfo.getWidth());
            result.setPicScale(NumberUtil.round(imageInfo.getWidth() * 1.0 / imageInfo.getHeight(), 2).doubleValue());
            result.setPicSize(fileSize);
            return result;
        } catch (Exception ex) {
            log.error("URL图片上传失败: {},错误信息:{}", fileurl, ex.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "URL图片上传失败");
        } finally {
            deleteTempFile(tempFile);
        }
    }

    private UploadPictureResult processingImageProcessingResults(ProcessResults processResults) {
        List<CIObject> objectList = processResults.getObjectList();
        UploadPictureResult result = new UploadPictureResult();
        if (CollUtil.isNotEmpty(objectList)) {
            CIObject compressedCiObject = objectList.get(0);
            CIObject thumbnailCiObject = objectList.get(1);
            // 填入处理结果部分的字段
            result = buildCiObjectResult(compressedCiObject, thumbnailCiObject);
        }
        return result;
    }


    /**
     * 上下文对象，封装调用云存储上传所需参数
     *
     * @param filePath         文件唯一路径
     * @param tempFile         临时文件
     * @param originalFilename 原始文件名
     * @param multipartFile    文件
     */
    private record FileUploadContext(String filePath,
                                     File tempFile,
                                     String originalFilename,
                                     MultipartFile multipartFile) {
    }

    /**
     * 检查是否是图片扩展名
     *
     * @param ext 文件扩展名
     * @return 是否是图片格式
     */
    private boolean isImageExtension(String ext) {
        if (ext == null || ext.isEmpty()) {
            return false;
        }
        String[] imageExtensions = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
        return Arrays.asList(imageExtensions).contains(ext.toLowerCase());
    }


    /**
     * 校验文件
     *
     * @param multipartFile multipart 文件
     */
    public void validPicture(MultipartFile multipartFile){
        // 大小校验
        long finaSize = multipartFile.getSize();
        // 最大文件大小
        long maxFileSize = 8 * 1024 * 1024L;
        ThrowUtils.throwIf(finaSize > maxFileSize,ErrorCode.PARAMS_ERROR,"图片大小不能大于8MB");
        // 文件后缀检验
        final List<String> allowFormatList = Arrays.asList("jpeg", "jpg", "png", "webp", "gif", "bmp", "svg");
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!allowFormatList.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }


    /**
     * 图片url文件格式校验
     * @param fileurl 文件url
     */
    public void validurl(String fileurl){
        ThrowUtils.throwIf(CharSequenceUtil.isBlank(fileurl),ErrorCode.PARAMS_ERROR,"文件url不能为空");
        // url格式校验
        try{
            new URL(fileurl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件url格式错误");
        }
        // 校验url协议
        ThrowUtils.throwIf(!fileurl.startsWith("http://") && !fileurl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR,"文件url协议错误");
        // 校验url是否包含点号
        ThrowUtils.throwIf(!fileurl.contains("."),ErrorCode.PARAMS_ERROR,"文件url协议不支持");
//         发送head请求,通过返回的请求头判断url的具体信息,以免直接下载图片内容,消耗服务器流量
        HttpResponse response = null;
        try{
            // 创建http头请求
            HttpRequest request = HttpUtil.createRequest(Method.HEAD, fileurl);
            response = request.execute();
            // 未正常返回
            if(response.getStatus() != HttpStatus.HTTP_OK){
                return;
            }
            // 返回正常,校验请求头内的内容
            // 校验文件类型
            String contenType = response.header("Content-Type");
            // 允许的图片类型
            final List<String> allowFormatListForContentType = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");
            ThrowUtils.throwIf(!allowFormatListForContentType.contains(contenType),ErrorCode.PARAMS_ERROR,"文件类型错误");

            // 校验文件大小
            try {
                String contentLength = response.header("Content-Length");
                ThrowUtils.throwIf(Long.parseLong(contentLength) > 8 * 1024 * 1024L,ErrorCode.PARAMS_ERROR,"文件大小不能大于8MB");
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
            }
        }finally {
            // 如果响应不为空,关闭响应
            if(response != null){
                response.close();
            }
        }
    }

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        try {
            Files.delete(file.toPath());
        } catch (NoSuchFileException e) {
            // 文件不存在可视为删除成功
        } catch (IOException e) {
            log.error("删除临时文件失败: {}", file.getAbsolutePath(), e);
        }
    }


    /**
     * 校验批量上传参数
     */
    public void validateBatchUploadParams(PictureUploadByBatchRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        if (request.getCount() == null || request.getCount() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "数量必须大于0");
        }
        if (request.getCount() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多30条");
        }
        if (CharSequenceUtil.isBlank(request.getSearchText())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜索内容不能为空");
        }
    }

    /**
     * 获取并解析网页内容
     */
    public Document fetchAndParseWebPage(String searchText) {
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        try {
            return Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败, url: {}", fetchUrl, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
    }


    /**
     * 从网页中提取图片数据（URL和标题）
     * @param document 网页文档对象
     * @return 包含图片数据的列表
     */
    public List<ImageData> extractImageData(Document document) {
        if (document == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "网页内容为空");
        }

        Element div = document.getElementsByClass("dgControl").first();
        if (div == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }

        List<ImageData> result = new ArrayList<>();

        div.select("div.iuscp.isv").forEach(container -> {
            Element linkElement = container.selectFirst("a.iusc");
            if (linkElement != null) {
                processImageElement(linkElement, result);
            }
        });

        return result;
    }

    /**
     * 处理json字符串,提取图片数据
     * @param linkElement
     * @param result
     */
    private void processImageElement(Element linkElement, List<ImageData> result) {
        String jsonStr = linkElement.attr("m");
        if (jsonStr.isEmpty()) {
            return;
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            String imageUrl = jsonObj.has("murl") ? jsonObj.getString("murl") : "";
            if (imageUrl.isEmpty()) {
                return;
            }

            String title = jsonObj.has("t") ? jsonObj.getString("t") : "";
            if (title.isEmpty() && jsonObj.has("desc")) {
                title = jsonObj.getString("desc");
            }

            result.add(new ImageData(imageUrl, title));
        } catch (Exception e) {
            log.warn("解析图片JSON数据失败: {}", jsonStr, e);
        }
    }

    // 用于存储抓取到的图片数据的简单类
    @Data
    public static class ImageData {
        private String imageUrl;
        private String title;

        public ImageData(String imageUrl, String title) {
            this.imageUrl = imageUrl;
            this.title = title;
        }
    }


    /**
     * 用于通过本地上传的文件上传头像
     *
     * @param file     头像文件
     * @param basePath 上传的基础路径
     * @return 头像地址, 用于前端更新用户个人信息的头像字段
     */
    public String uploadAvatar(MultipartFile file, String basePath) {
        // 校验文件
        validPicture(file);
        // 生成唯一文件名
        String uniqueFileName = IdUtil.simpleUUID() + "." + FileUtil.getSuffix(file.getOriginalFilename());
        // 构建存储路径
        String filePath = String.format("%s/%s", basePath, uniqueFileName);
        // 构建临时文件用于上传
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload_", "." + FileUtil.getSuffix(file.getOriginalFilename()));
            // 调用cosManager,通过multipartFile和filePath进行上传
            file.transferTo(tempFile);
            cosManager.putObject(filePath, tempFile);
            // 返回头像地址
            return cosClientConfig.getHost() + "/" + filePath;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片上传失败");
        }finally {
            // 释放资源
            deleteTempFile(tempFile);
        }
    }

}
