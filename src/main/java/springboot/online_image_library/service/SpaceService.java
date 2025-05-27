package springboot.online_image_library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.modle.dto.request.space.SpaceAddRequest;
import springboot.online_image_library.modle.entiry.Space;
import springboot.online_image_library.modle.entiry.User;

/**
 * @author Yu'S'hui'shen
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-05-27 12:34:35
 */
public interface SpaceService extends IService<Space> {


    /**
     * 根据空间等级自动填充空间容量
     *
     * @param space 根据空间等级自动填充空间容量
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 验证空间参数
     *
     * @param space 待验证空间
     * @param b     是否为更新
     */
    void validSpace(Space space, boolean b);

    /**
     * 验证用户权限
     *
     * @param user    当前用户
     * @param request 创建空间请求
     * @throws BusinessException 如果用户无权创建指定级别的空间,抛出错误
     */
    void validateUserPrivilege(User user, SpaceAddRequest request);

    /**
     * 将请求DTO转换为实体对象
     *
     * @param request 空间添加请求DTO
     * @param user    当前用户
     * @return 填充好的空间实体对象
     */
    Space requestToSpace(SpaceAddRequest request, User user);

    /**
     * 添加空间
     *
     * @param request 添加空间请求DTO
     * @param user    当前用户
     * @return 添加成功的空间ID
     */
    Space addSpace(SpaceAddRequest request, User user);

    /**
     * 为新用户添加空间
     *
     * @param user 新用户
     * @return 新用户添加的空间ID
     */
    void addSpaceForNewUser(User user);

}
