package springboot.online_image_library.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.mapper.UserMapper;
import springboot.online_image_library.modle.dto.request.user.UserQueryRequest;
import springboot.online_image_library.modle.dto.vo.user.LoginUserVO;
import springboot.online_image_library.modle.dto.vo.user.UserVO;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.modle.enums.UserRoleEnum;
import springboot.online_image_library.service.UserService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private static final String USER_LOGIN_STATE = "user:login:state:";
    private static final String SESSION_TOKEN = "session_token";
    // 登录状态过期时间，30分钟
    private static final long LOGIN_EXPIRE_TIME = 7 * 24 * 60 * (long) 60;
    private static final String USER_ACCOUNT_NICKNAME = "userAccount";

    // 创建一个RedisTemplate对象
    private final StringRedisTemplate stringRedisTemplate;

    public UserServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
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
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
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
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletResponse response) {
        // 1. 参数校验
        if (CharSequenceUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        checkAccountAndPassword(userAccount, userPassword);

        // 2. 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(USER_ACCOUNT_NICKNAME, userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);

        // 3. 验证用户名和密码
        if (user == null || !getEncryptPassword(userPassword).equals(user.getUserPassword())) {
            log.info("user login failed");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 4. 存储登录状态到 Redis
        String token = IdUtil.simpleUUID();
        // 生成唯一会话令牌
        String redisKey = USER_LOGIN_STATE + token;
        String userJson = JSONUtil.toJsonStr(user);
        // 序列化用户对象
        stringRedisTemplate.opsForValue().set(redisKey, userJson, LOGIN_EXPIRE_TIME, TimeUnit.SECONDS);

        // 5. 将令牌写入 Cookie
        Cookie cookie = new Cookie(SESSION_TOKEN, token);
        cookie.setPath("/");
        cookie.setMaxAge((int) LOGIN_EXPIRE_TIME);
        response.addCookie(cookie);

        // 6. 返回登录用户信息
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 1. 从 Cookie 获取会话令牌
        String token = getSessionToken(request);

        // 2. 令牌为空，直接返回未登录
        if (token == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 3. 从 Redis 获取用户信息
        String redisKey = USER_LOGIN_STATE + token;
        String userJson = stringRedisTemplate.opsForValue().get(redisKey);
        if (userJson == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 4. 反序列化用户信息
        User currentUser = JSONUtil.toBean(userJson, User.class);

        // 5. 从数据库查询最新用户信息（可选，视性能需求）
        currentUser = this.getById(currentUser.getId());
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 6. 更新 Redis 中的用户信息和过期时间
        userJson = JSONUtil.toJsonStr(currentUser);
        stringRedisTemplate.opsForValue().set(redisKey, userJson, LOGIN_EXPIRE_TIME, TimeUnit.SECONDS);

        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 1. 从 Cookie 获取会话令牌
        String token = getSessionToken(request);

        // 2. 删除 Redis 中的登录状态
        if (token != null) {
            String redisKey = USER_LOGIN_STATE + token;
            stringRedisTemplate.delete(redisKey);
        }

        // 3. 清理 Cookie（可选，客户端会自动处理过期）
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
    public LoginUserVO getLoginUserVO(User user) {
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user,loginUserVO);
        return loginUserVO;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }


}




