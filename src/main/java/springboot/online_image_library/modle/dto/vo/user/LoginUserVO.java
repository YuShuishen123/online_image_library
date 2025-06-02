package springboot.online_image_library.modle.dto.vo.user;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/8
 * @description 用于返回给前端的已登录用户视图
 */
@Data
public class LoginUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -819574702651822509L;

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}

