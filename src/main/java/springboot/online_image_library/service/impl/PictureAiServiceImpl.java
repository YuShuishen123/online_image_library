package springboot.online_image_library.service.impl;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import springboot.online_image_library.api.aliyunAi.AliYunApi;
import springboot.online_image_library.api.aliyunAi.model.Request.ExpansionTaskRequestFromTheFrontend;
import springboot.online_image_library.api.aliyunAi.model.Request.ExpansionTaskRequestSentToAPI;
import springboot.online_image_library.api.aliyunAi.model.Request.Text2ImageRequest;
import springboot.online_image_library.api.aliyunAi.model.Request.UniversalImageEditingRequestBody;
import springboot.online_image_library.api.aliyunAi.model.Response.CreateTaskResponse;
import springboot.online_image_library.exception.BusinessException;
import springboot.online_image_library.exception.ErrorCode;
import springboot.online_image_library.exception.ThrowUtils;
import springboot.online_image_library.modle.dto.vo.user.LoginState;
import springboot.online_image_library.modle.entiry.Picture;
import springboot.online_image_library.service.PictureAiService;
import springboot.online_image_library.service.PictureService;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Yu'S'hui'shen
 * @date 2025/6/1
 * @description 图片ai相关服务实现类
 */
@Service
@Slf4j
public class PictureAiServiceImpl implements PictureAiService {

    @Resource
    PictureService pictureServic;
    @Resource
    AliYunApi aliYunApi;

    @Override
    public CreateTaskResponse createOutPaintingTask(ExpansionTaskRequestFromTheFrontend expansionTaskRequestFromTheFrontend, LoginState loginState) {
        // 获取需要扩图的图片信息
        Long pictureId = expansionTaskRequestFromTheFrontend.getPictureId();
        Picture picture = Optional.ofNullable(pictureServic.getPictureById(pictureId)).orElseThrow(() -> new RuntimeException("图片不存在"));
        // 权限校验,用户必须是图片的拥有者
        ThrowUtils.throwIf(!Objects.equals(picture.getUserId(), loginState.getId()), ErrorCode.NO_AUTH_ERROR);
        // 构造请求参数
        ExpansionTaskRequestSentToAPI expansionTaskRequestSentToApi = new ExpansionTaskRequestSentToAPI();
        // 创建图片信息输入体
        ExpansionTaskRequestSentToAPI.Input input = new ExpansionTaskRequestSentToAPI.Input();
        // 图片url
        input.setImageUrl(picture.getOriginalImageurl());
        // 把图片信息放入请求体
        expansionTaskRequestSentToApi.setInput(input);
        // 把前端请求参数放入请求体
        BeanUtils.copyProperties(expansionTaskRequestFromTheFrontend, expansionTaskRequestSentToApi);
        // 创建任务
        try {
            log.info("创建任务参数：{}", JSONUtil.toJsonStr(expansionTaskRequestSentToApi));
            return aliYunApi.createOutPaintingTask(expansionTaskRequestSentToApi);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    @Override
    public CreateTaskResponse createTextToImageTask(Text2ImageRequest text2ImageRequest) {
        try {
            return aliYunApi.createTextToImageTask(text2ImageRequest);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    @Override
    public CreateTaskResponse createUniversalImageTask(UniversalImageEditingRequestBody universalImageEditingRequestBody) {
        try {
            return aliYunApi.createUniversalImageTask(universalImageEditingRequestBody);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }
}
