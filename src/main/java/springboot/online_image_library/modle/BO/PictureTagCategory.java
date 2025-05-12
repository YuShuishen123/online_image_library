package springboot.online_image_library.modle.BO;

import lombok.Data;

import java.util.List;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/12
 * @description  图片标签以及分类
 */
@Data
public class PictureTagCategory {
    /**
     * 图片标签
     */
    private List<String> tagList;
    /**
     * 图片分类
     */
    private List<String> categoryList;
}
