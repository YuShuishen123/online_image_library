package springboot.online_image_library.utils.picture;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.http.*;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.config.CosClientConfig;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.manager.CosManager;
import springboot.online_image_library.modle.BO.UploadPictureResult;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

        // 构建存储路径
        String filePath = String.format("%s/%s", basePath, uniqueFileName);

        // 处理临时文件上传
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

    // 上下文对象，封装上传所需参数
    private static class FileUploadContext {
        final String filePath;
        final File tempFile;
        final String originalFilename;
        final MultipartFile multipartFile;

        public FileUploadContext(String filePath, File tempFile, String originalFilename, MultipartFile multipartFile) {
            this.filePath = filePath;
            this.tempFile = tempFile;
            this.originalFilename = originalFilename;
            this.multipartFile = multipartFile;
        }
    }

    // 重构后的文件上传方法
    public String uploadFile(MultipartFile multipartFile, String basePath) {
        return handleFileUpload(multipartFile, basePath, context -> {
            cosManager.putObject(context.filePath, context.tempFile);
            return context.filePath;
        });
    }

    // 重构后的图片上传方法
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String basePath) {
        return handleFileUpload(multipartFile, basePath, context -> {
            PutObjectResult putObjectResult = cosManager.putPictureObject(context.filePath, context.tempFile);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            UploadPictureResult result = new UploadPictureResult();
            result.setPicName(context.originalFilename);
            result.setPicFormat(imageInfo.getFormat());
            result.setPicHeight(imageInfo.getHeight());
            result.setPicWidth(imageInfo.getWidth());
            result.setPicScale(NumberUtil.round(imageInfo.getWidth() * 1.0 / imageInfo.getHeight(), 2).doubleValue());
            result.setPicSize(context.multipartFile.getSize());
            result.setUrl(cosClientConfig.getHost() + "/" +context.filePath);
            return result;
        });
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
        String uniqueFileName = UUID.randomUUID() + "." + fileExtension;
        String filePath = String.format("%s/%s", basePath, uniqueFileName);

        // 下载图片到临时文件
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
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            // 构建返回结果
            UploadPictureResult result = new UploadPictureResult();
            result.setPicName(originalFilename);
            result.setPicFormat(imageInfo.getFormat());
            result.setPicHeight(imageInfo.getHeight());
            result.setPicWidth(imageInfo.getWidth());
            result.setPicScale(NumberUtil.round(imageInfo.getWidth() * 1.0 / imageInfo.getHeight(), 2).doubleValue());
            result.setPicSize(fileSize);
            result.setUrl(cosClientConfig.getHost() + "/" + filePath);

            return result;
        } catch (Exception ex) {
            log.error("URL图片上传失败: {}", fileurl, ex);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "URL图片上传失败");
        } finally {
            deleteTempFile(tempFile);
        }
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
        final List<String> allowFormatList = Arrays.asList("jpeg", "jpg", "png", "webp","gif");
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
        // 校验url后缀, 图片格式校验
        final List<String> allowFormatList = Arrays.asList("jepg", "jpg", "png", "webp", "gif");
        // 获取后缀
        String fileSuffix = FileUtil.getSuffix(fileurl);
        // 判断是否允许,忽略大小写
        ThrowUtils.throwIf(!allowFormatList.contains(fileSuffix.toLowerCase()),ErrorCode.PARAMS_ERROR,"文件类型错误");
        // 校验url是否包含点号
        ThrowUtils.throwIf(!fileurl.contains("."),ErrorCode.PARAMS_ERROR,"文件url协议不支持");

        // 发送head请求,通过返回的请求头判断url的具体信息,以免直接下载图片内容,消耗服务器流量
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


}
