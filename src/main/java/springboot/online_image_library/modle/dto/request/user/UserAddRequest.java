package springboot.online_image_library.modle.dto.request.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/9
 * @description 管理员创建用户请求体
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}

