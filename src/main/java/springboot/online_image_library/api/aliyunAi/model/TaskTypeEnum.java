package springboot.online_image_library.api.aliyunAi.model;


import lombok.Getter;
import springboot.online_image_library.api.aliyunAi.model.Response.CreatText2ImageTaskResponse;
import springboot.online_image_library.api.aliyunAi.model.Response.GetTaskResponse;

import java.util.Objects;

import static com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty;

/**
 * @author Yu'S'hui'shen
 * @description 任务类型枚举类
 */
@Getter
public enum TaskTypeEnum {


    OUTIMAGE(0, GetTaskResponse.class),
    TEXT2IMAGE(1, CreatText2ImageTaskResponse.class),
    UNIVERSALIMAGE(2, CreatText2ImageTaskResponse.class);

    //定义枚举变量
    private final Integer value;
    private final Class<?> clazz;

    TaskTypeEnum(Integer value, Class<?> clazz) {
        this.value = value;
        this.clazz = clazz;
    }

    // 根据value获取枚举
    public static TaskTypeEnum getEnumByValue(Integer value) {
        if (isEmpty(value)) {
            return null;
        }
        for (TaskTypeEnum taskTypeEnum : TaskTypeEnum.values()) {
            if (Objects.equals(taskTypeEnum.value, value)) {
                return taskTypeEnum;
            }
        }
        return null;
    }
}
