package springboot.online_image_library.modle.dto.request.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/8
 * @description 定义更新
 */
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;


    /**
     * 简介
     */
    private String userProfile;

    @Serial
    private static final long serialVersionUID = 1L;
}
