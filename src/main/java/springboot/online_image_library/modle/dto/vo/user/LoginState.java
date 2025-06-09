package springboot.online_image_library.modle.dto.vo.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Yu'S'hui'shen
 * @date 2025/6/9
 * @description 存储用户登陆状态, 仅包含id和角色
 */

@Data
public class LoginState implements Serializable {
    @Serial
    private static final long serialVersionUID = -819574702651822509L;

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

}
