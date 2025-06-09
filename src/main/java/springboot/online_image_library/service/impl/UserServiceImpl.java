package springboot.online_image_library.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.manager.CacheClientNoLocal;
import springboot.online_image_library.mapper.UserMapper;
import springboot.online_image_library.modle.dto.request.user.UserQueryRequest;
import springboot.online_image_library.modle.dto.vo.user.LoginState;
import springboot.online_image_library.modle.dto.vo.user.LoginUserInfo;
import springboot.online_image_library.modle.dto.vo.user.UserVO;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.modle.enums.UserRoleEnum;
import springboot.online_image_library.service.SpaceService;
import springboot.online_image_library.service.UserService;
import springboot.online_image_library.utils.picture.FileDeleteUtil;
import springboot.online_image_library.utils.picture.FileUploadUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
    // 登陆用户信息键
    private static final String LOGIN_USER_INFO = "user:login:info:";
    // 登陆用户状态键
    private static final String LOGIN_USER_STATE = "user:login:state:";

    private static final String SESSION_TOKEN = "session_token";
    // 登录状态过期时间，30分钟
    private static final long LOGIN_EXPIRE_TIME = 24 * 60 * (long) 60;
    private static final String USER_ACCOUNT_NICKNAME = "userAccount";

    // 创建一个CacheClient对象
    private final CacheClientNoLocal cacheClientNoLocal;
    // 创建一个SpaceService对象
    private final SpaceService spaceService;

    private final FileUploadUtil fileUploadUtil;
    private final FileDeleteUtil fileDeleteUtil;
    private final UserMapper userMapper;

    public UserServiceImpl(@Qualifier("cacheClientNoLocal") CacheClientNoLocal cacheClientNoLocal, SpaceService spaceService, FileUploadUtil fileUploadUtil, FileDeleteUtil fileDeleteUtil, UserMapper userMapper) {
        this.cacheClientNoLocal = cacheClientNoLocal;
        this.spaceService = spaceService;
        this.fileUploadUtil = fileUploadUtil;
        this.fileDeleteUtil = fileDeleteUtil;
        this.userMapper = userMapper;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        throwIf(userQueryRequest==null,ErrorCode.PARAMS_ERROR,"参数为空");
        // 拿去请求体中的字段信息
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        // 转换为请求体
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotNull(id), "id", id);
        queryWrapper.eq(CharSequenceUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(CharSequenceUtil.isNotBlank(userAccount), USER_ACCOUNT_NICKNAME, userAccount);
        queryWrapper.like(CharSequenceUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(CharSequenceUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(CharSequenceUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }



    @Override
    public UserVO getUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if(CollectionUtils.isEmpty(userList)){
            return new ArrayList<>();
        }
        // 使用stream流获取userVOList
        return userList.stream().map(this::getUserVO).toList();
    }


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        throwIf(CharSequenceUtil.hasBlank(userAccount, userPassword, checkPassword),ErrorCode.PARAMS_ERROR,"参数为空");
        throwIf(!userPassword.equals(checkPassword),ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        checkAccountAndPassword(userAccount, userPassword);
        // 2. 检查是否重复
        try {
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(getEncryptPassword(userPassword));
            user.setUserName("无名");
            user.setUserRole(UserRoleEnum.USER.getValue());
            // 直接尝试插入
            boolean success = save(user);
            // 处理其他插入失败情况
            throwIf(!success, ErrorCode.OPERATION_ERROR, "注册失败");
            // 异步创建空间
            LoginState loginState = new LoginState();
            loginState.setId(user.getId());
            loginState.setUserRole(UserRoleEnum.USER.getValue());
            spaceService.addSpaceForNewUser(loginState);
            return user.getId();
        } catch (DuplicateKeyException e) {
            // 捕获唯一键冲突异常
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "账号已存在");
        }
    }

    /**
     * 账号和密码校验
     * @param userAccount 账号
     * @param userPassword 密码
     */
    private static void checkAccountAndPassword(String userAccount, String userPassword) {
        throwIf(userAccount.length() > 10 || userAccount.length() < 6,ErrorCode.PARAMS_ERROR,"账号长度只能在8~10之间");
        throwIf(userPassword.length() > 16 || userPassword.length() < 8,ErrorCode.PARAMS_ERROR,"密码长度只能在8~16之间");
    }

    @Override
    public LoginUserInfo userLogin(String userAccount, String userPassword, HttpServletResponse response, int isAdmin) {
        if (CharSequenceUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        checkAccountAndPassword(userAccount, userPassword);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(USER_ACCOUNT_NICKNAME, userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 创建一个用户视图类用于在缓存当中存放用户信息
        UserVO userVO = this.getUserVO(user);

        if (user == null || !getEncryptPassword(userPassword).equals(user.getUserPassword())) {
            log.info("user login failed");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 判断用户是否是管理员,如果isAdmin为1,但是不是管理员,则返回无权限
        if (isAdmin == 1 && !user.getUserRole().equals(UserRoleEnum.ADMIN.getValue())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 创建一个登陆状态存储对象,用于存放用户的登陆状态和角色
        LoginState userLoginjState = new LoginState();
        // 赋值
        BeanUtils.copyProperties(user, userLoginjState);
        // 将用户登陆状态存入缓存
        // 生成一个唯一token
        String token = IdUtil.simpleUUID();
        String stateRedisKey = LOGIN_USER_STATE + token;
        cacheClientNoLocal.update(stateRedisKey, userLoginjState, Duration.ofSeconds(LOGIN_EXPIRE_TIME));
        // 回填用户信息到缓存
        String infoRedisKey = LOGIN_USER_INFO + user.getId();
        cacheClientNoLocal.update(infoRedisKey, userVO, Duration.ofSeconds(LOGIN_EXPIRE_TIME));
        // 写 Cookie
        Cookie cookie = new Cookie(SESSION_TOKEN, token);
        cookie.setPath("/");
        cookie.setMaxAge((int) LOGIN_EXPIRE_TIME);
        response.addCookie(cookie);
        return this.getLoginUserVO(user);
    }


    @Override
    public LoginUserInfo getLoginUser(HttpServletRequest request) {
        LoginState userLoginState = getLoginState(request);
        // 如果查询登陆状态的结果不为空,说明用户已经登陆,再从缓存当中获取用户信息
        if (userLoginState != null) {
            String redisInfoKey = LOGIN_USER_INFO + userLoginState.getId();
            return cacheClientNoLocal.query(
                    redisInfoKey,
                    new TypeReference<>() {
                    },
                    Duration.ofSeconds(LOGIN_EXPIRE_TIME),
                    () -> this.getLoginUserVO(userMapper.selectById(userLoginState.getId()))
            );
        }
        // 如果查询登陆状态的结果为空,说明用户没有登陆
        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }

    @Override
    public LoginState getLoginState(HttpServletRequest request) {
        String token = getSessionToken(request);
        if (token == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 先查询登陆状态,通过登陆状态键
        String stateRedisKey = LOGIN_USER_STATE + token;
        return cacheClientNoLocal.query(
                stateRedisKey,
                new TypeReference<>() {
                },
                Duration.ofSeconds(LOGIN_EXPIRE_TIME),
                () -> {
                    throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
                }
        );
    }


    @Override
    public boolean userLogout(HttpServletRequest request) {
        String token = getSessionToken(request);
        if (token != null) {
            String redisKey = LOGIN_USER_STATE + token;
            cacheClientNoLocal.invalidate(redisKey);
        }
        return true;
    }


    // 提取公共方法：从 Cookie 中获取会话令牌
    private String getSessionToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SESSION_TOKEN.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }


    @Override
    public LoginUserInfo getLoginUserVO(User user) {
        LoginUserInfo loginUserinfo = new LoginUserInfo();
        BeanUtil.copyProperties(user, loginUserinfo);
        return loginUserinfo;
    }

    @Override
    public boolean isAdmin(LoginState loginState) {
        return loginState != null && UserRoleEnum.ADMIN.getValue().equals(loginState.getUserRole());

    }

    @Override
    public String uploadAvatar(MultipartFile file, LoginUserInfo loginUserInfo) {
        // 生成基本路径
        String basePath = "public/" + loginUserInfo.getId() + "/avatar/";
        // 获取旧头像地址
        String oldAvatarUrl = loginUserInfo.getUserAvatar();
        log.debug("旧头像地址:{}", oldAvatarUrl);
        // 调用上传方法
        String newAvatarUrl;
        try {
            newAvatarUrl = fileUploadUtil.uploadAvatar(file, basePath);
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                // 设置新头像地址
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("id", loginUserInfo.getId());
                User user = new User();
                user.setUserAvatar(newAvatarUrl);
                boolean success = this.update(user, queryWrapper);
                throwIf(!success, ErrorCode.OPERATION_ERROR, "更新头像失败");
                log.info("更新头像成功,头像地址:{}", newAvatarUrl);
                // 异步删除旧头像
                log.debug("开始异步删除旧头像,url:{}", oldAvatarUrl);
                fileDeleteUtil.deleteFile(oldAvatarUrl);
                log.debug("异步删除旧头s像完成");
            } else {
                log.info("旧头像为空或新头像为空，无需删除");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "头像上传失败");
        }
        return newAvatarUrl;
    }
}




