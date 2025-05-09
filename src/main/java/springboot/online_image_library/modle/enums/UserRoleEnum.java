package springboot.online_image_library.modle.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yu'S'hui'shen
 * @description 用户角色枚举类
 */
@Getter
public enum UserRoleEnum {
    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;
    private final String value;

    private static final Map<String, UserRoleEnum> VALUE_TO_ENUM_MAP;

    static {
        Map<String, UserRoleEnum> map = new HashMap<>();
        for (UserRoleEnum role : values()) {
            map.put(role.value, role);
        }
        // 将普通的 HashMap 转换为不可变Map，确保枚举初始化后，外部代码无法修改其内容。
        VALUE_TO_ENUM_MAP = Collections.unmodifiableMap(map);
    }

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static UserRoleEnum fromValue(String value) {
        if (value == null || ObjectUtil.isEmpty(value)) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        UserRoleEnum role = VALUE_TO_ENUM_MAP.get(value);
        if (role == null) {
            throw new IllegalArgumentException("Invalid role value: " + value);
        }
        return role;
    }

    @Override
    public String toString() {
        return value;
    }
}

