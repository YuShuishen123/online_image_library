package springboot.online_image_library.utils.picture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
     * 云存储删除文件接口
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


    /**
     * 调用云存储删除接口异步删除云端文件
     *
     * @param url 待删除的图片url
     */
    @Async("imageAsyncExecutor")
    public void asyncCheckAndDeleteFile(String url) {
        // 检查数据库中是否有其他图片也采用当前的这个url,如果有则不删除云端,防止其他记录的url失效
            long start = System.currentTimeMillis();
            try {
                boolean deleteResult = deleteFileByUrl(url);
                if (!deleteResult) {
                    // 删除失败，记录错误日志
                    log.error("异步删除云端文件失败，url: {}", url);
                }
            } catch (Exception e) {
                // 捕获异常，记录详细错误信息
                log.error("异步删除云端图片异常，url: {}, 异常: {}", url, e.getMessage(), e);
            } finally {
                long duration = System.currentTimeMillis() - start;
                // 记录耗时，超过 1000ms 记录警告
                log.info("异步云端删除任务耗时: {}ms", duration);
                if (duration > 1000) {
                    log.warn("异步云端删除任务耗时过长，url: {}, 耗时: {}ms", url, duration);
                }
            }
        }
}
