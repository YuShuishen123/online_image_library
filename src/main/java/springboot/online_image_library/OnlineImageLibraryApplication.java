package springboot.online_image_library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Yu'S'hui'shen
 */
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class OnlineImageLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineImageLibraryApplication.class, args);
    }

}
