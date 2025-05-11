package springboot.online_image_library.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.manager.CosManager;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Yu'S'hui'shen
 * 文件长传工具类
 */
@Slf4j
@Component
public class FileUploadUtil {
    
    @Resource
    private CosManager cosManager;

    /**
     * 通用文件上传方法
     * @param multipartFile 上传的文件
     * @param basePath 存储的基础路径（如 "/test"）
     * @return 文件存储路径(存储对象的key)
     */
    public String uploadFile(MultipartFile multipartFile, String basePath) {
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
            cosManager.putObject(filePath, tempFile);
            return filePath;
        } catch (Exception ex) {
            log.error("文件上传失败: {}", originalFilename, ex);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败");
        } finally {
            if (tempFile != null) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (IOException ex) {
                    log.error("删除临时文件失败: {}", filePath, ex);
                }
            }
        }
    }
}
