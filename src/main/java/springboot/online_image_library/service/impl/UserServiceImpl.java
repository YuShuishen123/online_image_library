package springboot.online_image_library.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import springboot.online_image_library.mapper.UserMapper;
import springboot.online_image_library.modle.entiry.User;
import springboot.online_image_library.service.UserService;

/**
* @author Yu'S'hui'shen
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-05-08 11:16:28
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

}




