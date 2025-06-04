package springboot.online_image_library.api.aliyunAi.model.Request;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Yu'S'hui'shen
 * @date 2025/6/3
 * @description 前端发起文生图所需要携带的请求体
 */
@Data
public class Text2ImageRequest implements Serializable {

    /**
     * 指定使用的模型名称。
     * 可选值：
     * - stable-diffusion-3.5-large
     * - stable-diffusion-3.5-large-turbo
     * - flux-dev
     * - flux-merged
     * - wanx2.1-t2i-turbo 生成速度更快，通用生成模型。
     * - wanx2.1-t2i-plus 生成图像细节更丰富，速度稍慢，通用生成模型。
     * - wanx2.0-t2i-turbo 擅长质感人像与创意设计，速度中等，性价比高。
     */
    @Alias("model")
    private String model;

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
         * 可选，参考图像url
         */
        @Alias("init_image")
        private String initImage;

        /**
         * 正向提示词，用于描述期望生成图像的内容。
         * 支持中英文，不超过75个单词，超出部分会自动截断。
         */
        @Alias("prompt")
        private String prompt;

        /**
         * 负向提示词，用于描述期望生成图像的内容。
         */
        @Alias("negative_prompt")
        private String negativePrompt;
    }

    @Data
    public static class
    Parameters implements Serializable {

        /**
         * (可选)是否开启prompt智能改写。开启后会使用大模型对输入prompt进行智能改写，仅对正向提示词有效。对于较短的输入prompt生成效果提升明显，但会增加3-4秒耗时。
         */
        @Alias("prompt_extend")
        private Boolean promptExtend;

        /**
         * （可选）是否开启图片水印。开启后会在生成图像右下角添加水印。
         */
        @Alias("watermark")
        private Boolean watermark;

        /**
         * （可选）去噪推理步数，默认 40。
         * 一般步数越大，图像质量越高，但生成速度越慢。
         * 可选范围：1 ~ 500。
         */
        @Alias("steps")
        private Integer steps;

        /**
         * （可选）引导系数 cfg，表示生成图像与 prompt 的贴合程度。
         * 值越高，越倾向严格贴合 prompt。
         * 建议范围：4 ~ 5，默认值为 4.5。
         */
        @Alias("cfg")
        private Double cfg;

        /**
         * （可选）生成图像的尺寸。
         * 支持的尺寸范围是宽或高为 512 到 1024 的组合，且步长为 128。
         * 如："512*1024"、"1024*768"，默认值为 "1024*1024"。
         */
        @Alias("size")
        private String size;

        /**
         * （可选）期望生成图像的数量。
         * 默认值为 4，建议合理控制数量避免超时或过度资源消耗。
         */
        @Alias("n")
        private Integer n;

        /**
         * （可选）图像生成内容的偏移量，取值默认为3.0
         */
        @Alias("shift")
        private Double shift;

        /**
         * (可选)种子数,用于复现内容
         */
        @Alias("seed")
        private Integer seed;

    }
}
