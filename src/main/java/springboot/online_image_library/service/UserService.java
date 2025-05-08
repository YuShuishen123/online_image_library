package springboot.online_image_library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.modle.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author Yu'S'hui'shen
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-05-08 11:16:28
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     *  用户登陆
     * @param userAccount 账号
     * @param userPassword 密码
     * @param request 请求
     * @return 登陆用户视图
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 密码加密
     * @param userPassword
     * @return 加密后的密文
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户注销
     *
     * @param request
     * @return 注销结果
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 将User对象转换为LoginUserVO对象
     * @param user
     * @return LoginUserVO对象
     */
    LoginUserVO getLoginUserVO(User user);
}
