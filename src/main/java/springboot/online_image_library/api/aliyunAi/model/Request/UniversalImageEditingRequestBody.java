package springboot.online_image_library.api.aliyunAi.model.Request;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Yu'S'hui'shen
 * @date 2025/6/4
 * @description 通用图像编辑:通用请求体
 */
@Data
public class UniversalImageEditingRequestBody {

    /**
     * 指定使用的模型名称。
     */
    @Alias("model")
    private String model = "wanx2.1-imageedit";

    /**
     * 输入图像信息
     */
    private Input input;

    /**
     * 图像处理参数
     */
    private Parameters parameters;

    @Data
    public static class Input implements Serializable {

        /**
         * 正向提示词，用于描述期望生成图像的内容。
         * 支持中英文，不超过75个单词，超出部分会自动截断。
         */
        @Alias("prompt")
        private String prompt;

        /**
         * function string （必选）
         * 图像编辑功能。目前支持的功能有：
         * stylization_all：全局风格化，当前支持2种风格。风格和提示词技巧
         * stylization_local：局部风格化，当前支持8种风格。风格和提示词技巧
         * description_edit：指令编辑。通过指令即可编辑图像，简单编辑任务优先推荐这种方式。提示词技巧
         * description_edit_with_mask：局部重绘。需要指定编辑区域，适合对编辑范围有精确控制的场景。提示词技巧
         * remove_watermark：去文字水印。提示词技巧
         * expand：扩图。提示词技巧
         * super_resolution：图像超分。提示词技巧
         * colorization：图像上色。提示词技巧
         * doodle：线稿生图。提示词技巧
         * control_cartoon_feature：垫图，当前仅支持卡通形象。提示词技巧
         */
        @Alias("function")
        private String function;

        /**
         * 输入图像的URL地址。
         * 图像限制：
         * 图像格式：JPG、JPEG、PNG、BMP、TIFF、WEBP。
         * 图像分辨率：图像的宽度和高度范围为[512, 4096]像素。
         * 图像大小：不超过10MB。
         * URL地址中不能包含中文字符。
         */
        @Alias("base_image_url")
        private String baseImageUrl;

        /**
         * 仅当function设置为description_edit_with_mask（局部重绘）时必填，其余情况无需填写。
         * 涂抹区域图像要求：
         * 数据格式 ：仅支持图像URL地址，不支持Base64数据。
         * 图像分辨率 ：必须与base_image_url的图像分辨率保持一致。图像宽度和高度需在[512, 4096]像素之间。
         * 图像格式 ：支持JPG、JPEG、PNG、BMP、TIFF、WEBP。
         * 图像大小 ：不超过10MB。
         * URL地址中不能包含中文字符。
         * 涂抹区域颜色要求：
         * 白色区域 ：表示需要编辑的部分，必须使用纯白色（RGB值为[255,255,255]），否则可能无法正确识别。
         * 黑色区域：表示无需改变的部分，必须使用纯黑色（RGB值为[0,0,0]），否则可能无法正确识别。
         * 关于如何获取涂抹区域图像：使用PS抠图或其他工具生成黑白涂抹图像。
         */
        @Alias("mask_image_url")
        private String maskImageUrl;
    }

    @Data
    public static class Parameters implements Serializable {

        /**
         * （可选）是否开启图片水印。开启后会在生成图像右下角添加水印。
         */
        @Alias("watermark")
        private Boolean watermark;

        /**
         * 图片数量,默认1
         */
        @Alias("n")
        private Integer n;

        /**
         * (可选)种子数,用于复现内容
         */
        @Alias("seed")
        private Integer seed;

        /**
         * (可选)风格强度,默认0.5
         * 用法1:
         * 当function设置为 stylization_all（全局风格化）时填写。
         * 图像修改幅度。取值范围[0.0 1.0]，默认值为0.5。
         * 值越接近0，则越接近原图效果；值越接近1，对原图的修改幅度越大。
         * 用法2:
         * 当function设置为description_edit（指令编辑）时填写。
         * 图像修改幅度。取值范围[0.0, 1.0]，默认值为0.5。
         * 值越接近0，则越接近原图效果；值越接近1，对原图的修改幅度越大。
         */
        @Alias("strength")
        private Double strength;

        /**
         * top_scale float （可选）
         * 当function设置为expand（扩图）时才需填写。
         * 图像居中，向上按比例扩展图像。默认值为1.0，取值范围[1.0, 2.0]。
         * bottom_scale float （可选）
         * 当function设置为expand（扩图）时才需填写。
         * 图像居中，向下按比例扩展图像。默认值为1.0，取值范围[1.0, 2.0]。
         * left_scale float （可选）
         * 当function设置为expand（扩图）时才需填写。
         * 图像居中，向左按比例扩展图像。默认值为1.0，取值范围[1.0, 2.0]。
         * right_scale float （可选）
         * 当function设置为expand（扩图）时才需填写。
         * 图像居中，向右按比例扩展图像。默认值为1.0，取值范围[1.0, 2.0]。
         */
        @Alias("top_scale")
        private Double topScale;

        @Alias("bottom_scale")
        private Double bottomScale;

        @Alias("left_scale")
        private Double leftScale;

        @Alias("right_scale")
        private Double rightScale;


        /**
         * 当function设置为doodle（线稿生图）时才需填写。
         * 输入图像是否为线稿图像。
         * false：默认值，输入图像不为线稿图像。模型会先从输入图像中提取线稿，再参考提取的线稿生成新的图像。
         * true：输入图像为线稿图像。模型将直接基于输入图像生成图像，适用于涂鸦作画场景。
         */
        @Alias("is_sketch")
        private Boolean isSketch;

        /**
         * 当function设置为super_resolution（图像超分）时才需填写。
         * 图像超分的放大倍数。在放大图像的同时增强细节，提升图像分辨率，实现高清处理。
         * 取值范围为1~4，默认值为1。当upscale_factor设置为1时，仅对图像进行高清处理，不进行放大。
         */
        @Alias("upscale_factor")
        private Integer upscaleFactor;

    }
}
