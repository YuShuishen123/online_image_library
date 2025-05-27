package springboot.online_image_library.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import springboot.online_image_library.service.PictureService;

/**
 * @author Yu'S'hui'shen
 * @description: 缓存定时器服务
 */
@Service
@Slf4j
public class CacheScheduleService {
    /**
     * 异步延迟双删
     *
     * @param pictureId      图片id
     * @param delay          延迟时间
     * @param pictureService 图片服务
     */
    @Async("imageAsyncExecutor")
    public void scheduleInvalidateForSingePicture(Long pictureId, long delay, PictureService pictureService) {
        try {
            Thread.sleep(delay);
            pictureService.invalidateSingerPicture(pictureId);
            log.info("[scheduleInvalidate] 缓存失效，pictureId={}", pictureId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
