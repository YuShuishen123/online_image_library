package springboot.online_image_library.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * 全局统一 Jackson 配置，处理 Long 精度 + 时间格式 + 忽略 null 等
 * @author Yu'S'hui'shen
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // Long -> String，防止精度丢失
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            builder.modules(module);

            // 统一时间格式
            builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 忽略 null 字段
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);

            // 设置时区
            builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));

            // 忽略未知字段
            builder.featuresToDisable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        };
    }
}
