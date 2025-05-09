package springboot.online_image_library.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.ResultUtils;

/**
 * 系统基础接口
 * @author Yu'S'hui'shen
 */
@Api(tags = "System", value = "系统基础功能接口")
@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 服务健康检查接口
     */
    @ApiOperation(
            value = "健康检查",
            notes = "用于检测服务是否正常运行",
            httpMethod = "GET",
            response = BaseResponse.class
    )
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
