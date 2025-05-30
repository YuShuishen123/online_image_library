package springboot.online_image_library.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springboot.online_image_library.constant.SpaceConstants;
import springboot.online_image_library.constant.UserConstants;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.mapper.SpaceMapper;
import springboot.online_image_library.modle.BO.SpaceLevel;
import springboot.online_image_library.modle.dto.request.space.SpaceAddRequest;
import springboot.online_image_library.modle.entiry.Space;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.modle.enums.SpaceLevelEnum;
import springboot.online_image_library.service.SpaceService;
import springboot.online_image_library.utils.commom.LocalLockUtil;
import springboot.online_image_library.utils.commom.Object2JsonUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author Yu'S'hui'shen
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-05-27 12:34:35
 */
@Service
@Slf4j
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private Object2JsonUtils object2JsonUtils;

    @Override
    public void validSpace(Space space, boolean isAdd) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        // 创建
        if (isAdd) {
            if (CharSequenceUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
        }
        // 修改数据时，如果要改空间级别和名称
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (CharSequenceUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public void validateUserPrivilege(User user, SpaceAddRequest request) {
        // 请求中的空间等级不为空时候,判断是否为管理员,非管理员则抛出权限错误
        if (request.getSpaceLevel() != null) {
            ThrowUtils.throwIf(request.getSpaceLevel() != SpaceLevelEnum.COMMON.getValue()
                    && !Objects.equals(user.getUserRole(), UserConstants.ADMIN_ROLE), ErrorCode.NO_AUTH_ERROR, "非管理员无权限");
        }
    }

    @Override
    public Space requestToSpace(SpaceAddRequest request, User user) {
        Space space = new Space();
        // 使用Spring工具类复制同名属性
        BeanUtils.copyProperties(request, space);

        // 设置默认空间名称（如果请求中未提供）
        if (CharSequenceUtil.isBlank(request.getSpaceName())) {
            space.setSpaceName(SpaceConstants.DEFAULT_SPACE_NAME);
        }
        // 设置默认空间级别（如果请求中未提供）
        if (request.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }

        // 根据空间级别填充额外信息
        this.fillSpaceBySpaceLevel(space);
        // 设置所属用户ID
        space.setUserId(user.getId());

        return space;
    }

    @Override
    public Space addSpace(SpaceAddRequest request, User user) {
        // 生成唯一的锁键（建议：业务前缀_用户ID_空间类型）
        String lockKey = "space_service:create:" + user.getId();
        // 转换并且填充数据
        Space space = requestToSpace(request, user);
        // 权限校验,如果是非普通空间，则需要管理员权限
        validateUserPrivilege(user, request);
        // 加本地锁创建空间,防止用户连续点击导致重复创建
        return LocalLockUtil.executeWithLockAndReturn(lockKey, () -> {
            // 先判断该用户是否已经拥有空间
            Space oldSpace = this.getOne(new QueryWrapper<Space>().eq("userid", user.getId()));
            ThrowUtils.throwIf(oldSpace != null, ErrorCode.OPERATION_ERROR, "用户已拥有空间");
            // 创建空间
            boolean result = this.save(space);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建空间失败");
            return this.getById(space.getId());
        });
    }

    @Override
    @Async
    public void addSpaceForNewUser(User user) {
        SpaceAddRequest request = new SpaceAddRequest();
        // 转换并且填充数据
        Space space = requestToSpace(request, user);
        if (this.save(space)) {
            log.info("初始用户空间创建成功,空间id:{}", space.getId());
        } else {
            log.error("初始用户空间创建失败,userId:{}", user.getId());
        }
    }

    @Override
    public boolean isSpaceOwner(User user, Long spaceId) {
        return this.getOne(new QueryWrapper<Space>().eq("id", spaceId).eq("userid", user.getId())) != null;
    }

    @Async("imageAsyncExecutor")
    @Transactional
    @Override
    public void asyncUpdateSpacePictureInfo(long spaceId, Long picSize, boolean isAdd) {
        // 1. 参数校验
        if (spaceId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间ID非法");
        }
        if (picSize == null || picSize < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小非法");
        }

        // 2. 创建并配置更新包装器
        LambdaUpdateWrapper<Space> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Space::getId, spaceId);

        // 3. 根据 isAdd 动态设置更新值
        if (isAdd) {
            updateWrapper.setSql("totalCount = totalCount + 1")
                    .setSql("totalSize = totalSize + " + picSize);
        } else {
            updateWrapper.setSql("totalCount = totalCount - 1")
                    .setSql("totalSize = totalSize - " + picSize);
        }

        // 4. 执行更新并处理异常
        boolean success = this.update(updateWrapper);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "异步更新空间图片信息失败");
        }
    }


    /**
     * 根据登陆者获取用户空间
     */
    @Override
    public Space getUserSpaceFromLogUser(User logUser) {
        Space space = this.getOne(new QueryWrapper<Space>().eq("userid", logUser.getId()));
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "用户空间不存在");
        return space;
    }

    @Override
    public Optional<List<SpaceLevel>> getSpaceLeveListJsonFromCache(String cacheKey) {
        try {
            List<String> jsonList = redisTemplate.opsForList().range(cacheKey, 0, -1);
            if (jsonList == null || jsonList.isEmpty()) {
                return Optional.empty();
            }
            List<SpaceLevel> levels = jsonList.stream()
                    .map(json -> object2JsonUtils.parseJson(json, SpaceLevel.class))
                    .collect(Collectors.toList());
            log.info("从 Redis 获取 spaceLevelList 成功");
            return Optional.of(levels);
        } catch (Exception e) {
            log.error("从 Redis 解析 spaceLevelList 失败", e);
            return Optional.empty();
        }
    }

    @Override
    public List<SpaceLevel> buildSpaceLevelsFromEnum() {
        return Arrays.stream(SpaceLevelEnum.values())
                .map(enumValue -> new SpaceLevel(
                        enumValue.getValue(),
                        enumValue.getText(),
                        enumValue.getMaxCount(),
                        enumValue.getMaxSize()))
                .collect(Collectors.toList());
    }

    @Override
    public void cacheSpaceLevels(String cacheKey, List<SpaceLevel> levels) {
        try {
            List<String> jsonList = levels.stream()
                    .map(level -> object2JsonUtils.toJson(level))
                    .collect(Collectors.toList());
            redisTemplate.opsForList().rightPushAll(cacheKey, jsonList);
        } catch (Exception e) {
            log.error("缓存空间级别数据失败", e);
        }
    }

}





