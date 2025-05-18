package springboot.online_image_library.modle.dto.request.picture;

import lombok.Data;

/**
 * @author Yu'S'hui'shen
 * @description: 图片批量上传请求
 */
@Data
public class PictureUploadByBatchRequest {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 10;
}
