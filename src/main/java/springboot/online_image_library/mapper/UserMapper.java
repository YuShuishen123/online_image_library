package springboot.online_image_library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import springboot.online_image_library.modle.entiry.User;

/**
* @author Yu'S'hui'shen
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2025-05-08 11:16:28
* @Entity springboot.online_image_library.modle.entiry.User
*/
@Mapper
@Repository
public interface UserMapper extends BaseMapper<User> {

}




