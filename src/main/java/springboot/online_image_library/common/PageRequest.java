package springboot.online_image_library.common;

import lombok.Data;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/7
 * @description 分页请求封装
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}

