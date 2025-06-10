package springboot.online_image_library.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import springboot.online_image_library.config.CosClientConfig;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yu'S'hui'shen
 * Cos云存储通用工具类
 */
@Slf4j
@Component
public class CosManager {
  
    @Resource
    private CosClientConfig cosClientConfig;
  
    @Resource  
    private COSClient cosClient;

    /**
     * 上传对象,可以用于上传头像等无依赖文件
     *
     * @param key  唯一键
     * @param file 文件
     */
    public void putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传图片(进行图片压缩处理)
     * @param key 文件唯一键
     * @param file 要上传的文件
     * @return 上传结果为附带缩略图处理的PutObjectResult
     */
    public PutObjectResult putPictureObject(String key, File file) {

        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        List<PicOperations.Rule> rules = new ArrayList<>();
        // 图片压缩（转成 webp 格式）
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setRule("imageMogr2/format/webp");
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setFileId(webpKey);
        rules.add(compressRule);
        // 缩略图处理
        PicOperations.Rule thumbnailRule = new PicOperations.Rule();
        thumbnailRule.setBucket(cosClientConfig.getBucket());
        String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
        thumbnailRule.setFileId(thumbnailKey);
        // 缩放规则 /thumbnail/<Width>x<Height>>（如果大于原图宽高，则不处理）
        thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 128, 128));
        rules.add(thumbnailRule);
        // 构造处理参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * @param bucketName 存储桶
     * @param key        对象唯一区分路径
     * @return
     * @throws CosClientException  对象存储客户端异常
     * @throws CosServiceException 删除服务异常
     */
    public boolean deleteObject(String bucketName, String key)
            throws CosClientException, CosServiceException{
        try {
            cosClient.deleteObject(bucketName, key);
        } catch (CosServiceException cosServiceException){
            log.error("发生CosServiceException，异常信息：{}", cosServiceException.getMessage(), cosServiceException);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,cosServiceException.getMessage());
        } catch (CosClientException cosClientException) {
            log.error("发生CosClientException，异常信息：{}", cosClientException.getMessage(), cosClientException);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,cosClientException.getMessage());
        }
        return true;
    }


}
