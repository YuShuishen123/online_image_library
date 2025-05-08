package springboot.online_image_library.modle.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/8
 * @description 定义用户注册请求体
 */

@Data
public class UserRegistRequest implements Serializable {

    private static final long serialVersionUID = 5882578652312455104L;

    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户密码
     */
    private String userPassword;
    /**
     * 确认用户密码
     */
    private String checkPassword;

}
