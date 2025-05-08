package springboot.online_image_library.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.mapper.UserMapper;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.modle.enums.UserRoleEnum;
import springboot.online_image_library.modle.vo.LoginUserVO;
import springboot.online_image_library.service.UserService;

import javax.servlet.http.HttpServletRequest;

import static springboot.online_image_library.constant.UserConstants.USER_LOGIN_STATE;
import static springboot.online_image_library.exception.ThrowUtils.throwIf;

/**
* @author Yu'S'hui'shen
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-05-08 11:16:28
*/
@Service
@Slf4j

public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "SALT";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        throwIf(CharSequenceUtil.hasBlank(userAccount, userPassword, checkPassword),ErrorCode.PARAMS_ERROR,"参数为空");
        throwIf(!userPassword.equals(checkPassword),ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        checkAccountAndPassword(userAccount, userPassword);
        // 2. 检查是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        throwIf(count > 0,ErrorCode.OPERATION_ERROR,"账号已存在");
        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    private static void checkAccountAndPassword(String userAccount, String userPassword) {
        throwIf(userAccount.length() > 10 || userAccount.length() < 6,ErrorCode.PARAMS_ERROR,"账号长度只能在8~10之间");
        throwIf(userPassword.length() > 16 || userPassword.length() < 8,ErrorCode.PARAMS_ERROR,"密码长度只能在8~16之间");
    }

    // 1.参数有效性校验
        // 2.检查用户是否存在
        // 3.核对用户密码是否正确
        // 4.封装响应数据
        // 5.记录用户登陆状态
        // 6.返回用户数据视图

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 参数校验
        throwIf(CharSequenceUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        checkAccountAndPassword(userAccount, userPassword);
        // 2. 先仅用账号查用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);

        // 3. 查询用户名和比对密码
        if (user == null || !getEncryptPassword(userPassword).equals(user.getUserPassword())) {
            log.info("user login failed");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 4. 记录登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 更新登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, currentUser);
        return currentUser;
    }



    @Override
    public String getEncryptPassword(String userPassword) {
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }


    @Override
    public LoginUserVO getLoginUserVO(User user) {
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user,loginUserVO);
        return loginUserVO;
    }
}




