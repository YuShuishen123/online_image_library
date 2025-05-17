package springboot.online_image_library.utils.picture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import springboot.online_image_library.manager.CosManager;

import javax.annotation.Resource;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/16
 * @description 云端文件删除工具类
 */
@Component
@Slf4j
public class FileDeleteUtil {

    @Resource
    private CosManager cosManager;

    /**
     * 根据url删除
     * @param url 待删除的文件的url
     */
    public boolean deleteFileByUrl(String url) {
        // 示例:https://image-1329241986.cos.ap-chengdu.myqcloud.com/public/1920415149873795074/6f7b6909-1ead-4e12-9d65-4952ba20cf14.jpg
        // 获取桶String,示例中为:image-1329241986
        int startIndexBucket = url.indexOf("://") + 3;
        int endIndexBucket = url.indexOf(".cos");
        String buketName = url.substring(startIndexBucket, endIndexBucket);
        // 获取key,示例中为: public/1920415149873795074/6f7b6909-1ead-4e12-9d65-4952ba20cf14.jpg
        int startIndexKey = url.indexOf("/", url.indexOf(".cos") + 4);
        String key = url.substring(startIndexKey + 1);
        return cosManager.deleteObject(buketName, key);
    }

}
