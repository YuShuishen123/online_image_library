package springboot.online_image_library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import springboot.online_image_library.modle.entiry.Space;

/**
 * @author Yu'S'hui'shen
 * @description 针对表【space(空间)】的数据库操作Mapper
 * @createDate 2025-05-27 12:34:35
 * @Entity generator.domain.Space
 */
@Mapper
@Repository
public interface SpaceMapper extends BaseMapper<Space> {

}




