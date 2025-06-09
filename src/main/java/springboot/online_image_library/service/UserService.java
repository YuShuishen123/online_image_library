package springboot.online_image_library.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.modle.dto.request.user.UserQueryRequest;
import springboot.online_image_library.modle.dto.vo.user.LoginState;
import springboot.online_image_library.modle.dto.vo.user.LoginUserInfo;
import springboot.online_image_library.modle.dto.vo.user.UserVO;
import springboot.online_image_library.modle.entiry.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
* @author Yu'S'hui'shen
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-05-08 11:16:28
*/
public interface UserService extends IService<User> {


    /**
     * 将用户查询请求转换为QueryWrapper对象
     * @param userQueryRequest
     * @return QueryWrapper对象
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 用户脱敏
     * @param user
     * @return 脱敏后的单个用户
     */
    UserVO getUserVO(User user);

    /**
     * 用户列表脱敏
     * @param userList
     * @return 脱敏后的用户列表
     */
    List<UserVO> getUserVOList(List<User> userList);

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
     * @param response 响应
     * @param isAdmin 是否为管理员
     * @return 登陆用户视图
     */
    LoginUserInfo userLogin(String userAccount, String userPassword, HttpServletResponse response, int isAdmin);

    /**
     * 获取当前登录用户的个人信息
     *
     * @param request
     * @return 当前登录用户信息
     */
    LoginUserInfo getLoginUser(HttpServletRequest request);


    /**
     * 获取当前登录状态以及角色
     *
     * @param request 请求,提供cookie
     * @return 登录状态(id和角色)
     */
    LoginState getLoginState(HttpServletRequest request);

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
    LoginUserInfo getLoginUserVO(User user);

    /**
     * 是否为管理员
     *
     * @param loginState 登录状态
     * @return 是否为管理员
     */
    boolean isAdmin(LoginState loginState);


    /**
     * 上传头像
     *
     * @param file          头像文件
     * @param loginUserInfo 登陆用户
     * @return 头像url
     */
    String uploadAvatar(MultipartFile file, LoginUserInfo loginUserInfo);
}
