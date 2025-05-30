package springboot.online_image_library.utils.commom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/30
 * @description 该工具用于将对象序列化为JSON字符串, 或者将json转换为对象序列化
 */
@Component
@Slf4j
public class Object2JsonUtils {

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 将JSON字符串反序列化为指定类型的对象
     *
     * @param json  JSON字符串
     * @param clazz 目标对象类型
     * @param <T>   泛型类型参数
     * @return 反序列化后的对象，如果失败则返回null
     */
    public <T> T parseJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败", e);
            throw new RuntimeException("反序列化失败", e);
        }
    }

    /**
     * 将对象序列化为JSON字符串
     *
     * @param obj 需要序列化的对象
     * @return 序列化后的JSON字符串，如果失败则返回null
     */
    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            throw new RuntimeException("序列化失败", e);
        }
    }

}
