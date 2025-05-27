package springboot.online_image_library.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import springboot.online_image_library.constant.SpaceConstants;
import springboot.online_image_library.constant.UserConstants;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.mapper.SpaceMapper;
import springboot.online_image_library.modle.dto.request.space.SpaceAddRequest;
import springboot.online_image_library.modle.entiry.Space;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.modle.enums.SpaceLevelEnum;
import springboot.online_image_library.service.SpaceService;
import springboot.online_image_library.utils.commom.LocalLockUtil;

import java.util.Objects;


/**
 * @author Yu'S'hui'shen
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-05-27 12:34:35
 */
@Service
@Slf4j
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

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

}




