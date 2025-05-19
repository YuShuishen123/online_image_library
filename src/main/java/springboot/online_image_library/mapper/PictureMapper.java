package springboot.online_image_library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import springboot.online_image_library.modle.entiry.Picture;

/**
* @author Yu'S'hui'shen
* @description 针对表【picture(图片)】的数据库操作Mapper
* @createDate 2025-05-11 21:29:53
* @Entity springboot.online_image_library.modle.entiry.Picture
*/
@Mapper
@Repository
public interface PictureMapper extends BaseMapper<Picture> {

}




