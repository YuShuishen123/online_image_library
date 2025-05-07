package springboot.online_image_library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Yu'S'hui'shen
 */
@SpringBootApplication
@MapperScan("springboot.online_image_library.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class OnlineImageLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineImageLibraryApplication.class, args);
    }

}
