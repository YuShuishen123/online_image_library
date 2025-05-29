package springboot.online_image_library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Yu'S'hui'shen
 */
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableCaching
@EnableAsync
public class OnlineImageLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineImageLibraryApplication.class, args);
    }

}
