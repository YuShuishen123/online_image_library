package springboot.online_image_library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springboot.online_image_library.annotation.AuthCheck;
import springboot.online_image_library.common.BaseResponse;
import springboot.online_image_library.common.ResultUtils;
import springboot.online_image_library.constant.UserConstants;
import springboot.online_image_library.utils.picture.FileDownloadUtils;
import springboot.online_image_library.utils.picture.FileUploadUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * @author Yu'S'hui'shen
 * @date 2025/5/11
 * @description 文件管理接口
 */
@Tag(name = "FileController", description = "文件相关控制器")
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    private final FileUploadUtil fileUploadUtil;
    private final FileDownloadUtils fileDownloadUtils;

    public FileController(FileUploadUtil fileUploadUtil, FileDownloadUtils fileDownloadUtils) {
        this.fileUploadUtil = fileUploadUtil;
        this.fileDownloadUtils = fileDownloadUtils;
    }


    @Operation(
            summary = "测试文件上传",
            description = "用于测试文件上传",
            method = "POST")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    @PostMapping("/test/Upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile file) {
        // 一行调用工具类
        return ResultUtils.success(fileUploadUtil.uploadFile(file, "/test"));
    }

    /**
     * 测试文件下载
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @Operation(
            summary = "测试文件下载",
            description = "用于测试文件下载",
            method = "GET")
    @AuthCheck(mustRole = UserConstants.ADMIN_ROLE)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        fileDownloadUtils.download(filepath, response);
    }


}
