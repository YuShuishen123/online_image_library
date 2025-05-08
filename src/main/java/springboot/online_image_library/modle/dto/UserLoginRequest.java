package springboot.online_image_library.modle.dto;

import lombok.Data;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/8
 * @description 登录请求体
 */
@Data
public class UserLoginRequest {
    /**
     * 用户名/账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;
}
