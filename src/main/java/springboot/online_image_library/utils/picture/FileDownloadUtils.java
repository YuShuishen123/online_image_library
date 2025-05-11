package springboot.online_image_library.utils.picture;

import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.manager.CosManager;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * @author Yu'S'hui'shen
 * 文件下载工具类
 */
@Slf4j
@Component
public class FileDownloadUtils {

    @Resource
    private CosManager cosManager;

    /**
     * 通用文件下载方法
     * @param filepath 文件存储路径（如 "/test/abc.jpg"）
     * @param response HttpServletResponse对象
     * @param originalFilename 前端显示的文件名（可选）
     */
    public void download(String filepath, HttpServletResponse response, String... originalFilename) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            // 1. 从COS获取文件流
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            
            // 2. 设置响应头（支持中文文件名编码）
            String displayName = originalFilename.length > 0 ? originalFilename[0] : 
                                filepath.substring(filepath.lastIndexOf("/") + 1);
            String encodedFileName = URLEncoder.encode(displayName, "UTF-8").replace("+", "%20");
            
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", 
                "attachment; filename*=UTF-8''" + encodedFileName);
            
            // 3. 流式传输（避免内存溢出）
            try (OutputStream out = response.getOutputStream()) {
                IOUtils.copy(cosObjectInput, out);
            }
        } catch (CosClientException e) {
            log.error("COS服务异常 | 文件路径: {}", filepath, e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "对象参数错误");
        } catch (Exception e) {
            log.error("文件下载失败 | 路径: {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }
}
