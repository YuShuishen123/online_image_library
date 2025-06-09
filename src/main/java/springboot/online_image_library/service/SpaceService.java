package springboot.online_image_library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.modle.BO.SpaceLevel;
import springboot.online_image_library.modle.dto.request.space.SpaceAddRequest;
import springboot.online_image_library.modle.dto.vo.user.LoginState;
import springboot.online_image_library.modle.entiry.Space;

import java.util.List;
import java.util.Optional;

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
     * @param loginState    当前用户
     * @param request 创建空间请求
     * @throws BusinessException 如果用户无权创建指定级别的空间,抛出错误
     */
    void validateUserPrivilege(LoginState loginState, SpaceAddRequest request);

    /**
     * 将请求DTO转换为实体对象
     *
     * @param request 空间添加请求DTO
     * @param loginState    当前用户
     * @return 填充好的空间实体对象
     */
    Space requestToSpace(SpaceAddRequest request, LoginState loginState);

    /**
     * 添加空间
     *
     * @param request 添加空间请求DTO
     * @param loginState    当前用户
     * @return 添加成功的空间ID
     */
    Space addSpace(SpaceAddRequest request, LoginState loginState);

    /**
     * 为新用户添加空间
     *
     * @param loginState 新用户
     * @return 新用户添加的空间ID
     */
    void addSpaceForNewUser(LoginState loginState);


    /**
     * 检查用户是否为空间拥有者
     *
     * @param loginState 待检查的用户
     * @param spaceId 空间ID
     * @return 如果是空间拥有者返回true，否则返回false
     */
    boolean isSpaceOwner(LoginState loginState, Long spaceId);

    /**
     * 异步更新空间图片信息
     *
     * @param spaceId 空间ID
     * @param picSize 图片大小(单位:字节)
     * @param isAdd   true为添加图片,false为删除图片
     */
    void asyncUpdateSpacePictureInfo(long spaceId, Long picSize, boolean isAdd);

    /**
     * 根据登陆者获取用户空间
     *
     * @param loginState 当前登陆者
     * @return 用户空间
     */
    Space getUserSpaceFromLogUser(LoginState loginState);

    /**
     * 从缓存中获取空间等级列表
     *
     * @param cacheKey 空间级别缓存键
     * @return 包含空间等级列表的Optional对象
     */
    Optional<List<SpaceLevel>> getSpaceLeveListJsonFromCache(String cacheKey);

    /**
     * 构建空间等级列表
     *
     * @return 空间等级列表
     */
    List<SpaceLevel> buildSpaceLevelsFromEnum();

    /**
     * 缓存空间等级列表
     *
     * @param cacheKey 缓存键
     * @param levels   空间等级列表
     */
    void cacheSpaceLevels(String cacheKey, List<SpaceLevel> levels);
}
