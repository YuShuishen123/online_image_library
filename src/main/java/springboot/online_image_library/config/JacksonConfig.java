package springboot.online_image_library.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 用于配置 Jackson
 *
 * @author Yu'S'hui'shen
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 设置可选的配置，增强兼容性
        // 忽略 null 字段
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 忽略未知字段
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 可选：统一日期格式
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 设置时区
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        return mapper;
    }
}
