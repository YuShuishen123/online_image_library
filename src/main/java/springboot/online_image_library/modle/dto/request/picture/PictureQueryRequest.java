package springboot.online_image_library.modle.dto.request.picture;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.DigestUtils;
import springboot.online_image_library.common.PageRequest;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 图片查询请求，支持分页查询
 * @author Yu'S'hui'shen
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {
  
    /**  
     * id  
     */  
    private Long id;  
  
    /**  
     * 图片名称  
     */  
    private String name;  
  
    /**  
     * 简介  
     */  
    private String introduction;  
  
    /**  
     * 分类  
     */  
    private String category;  
  
    /**  
     * 标签  
     */  
    private List<String> tags;
  
    /**  
     * 文件体积  
     */  
    private Long picSize;  
  
    /**  
     * 图片宽度  
     */  
    private Integer picWidth;  
  
    /**  
     * 图片高度  
     */  
    private Integer picHeight;  
  
    /**  
     * 图片比例  
     */  
    private Double picScale;  
  
    /**  
     * 图片格式  
     */  
    private String picFormat;  
  
    /**  
     * 搜索词（同时搜名称、简介等）  
     */  
    private String searchText;  
  
    /**  
     * 用户 id  
     */  
    private Long userId;

    /**
     * 状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 id
     */
    private Long reviewerId;


    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 生成用于分页查询的缓存唯一key
     */
    public String generateCacheKey() {
        // 1. 构建有序参数字符串（按字母顺序排列字段）
        StringJoiner joiner = new StringJoiner(":");
        joiner.add(String.valueOf(this.getCurrent()));
        joiner.add(String.valueOf(this.getPageSize()));

        // 2. 添加所有可能影响查询结果的字段（排除分页已包含的字段）
        if (this.id != null) {
            joiner.add("id:" + this.id);
        }
        if (CharSequenceUtil.isNotBlank(this.name)) {
            joiner.add("name:" + this.name);
        }
        if (CharSequenceUtil.isNotBlank(this.category)) {
            joiner.add("cat:" + this.category);
        }
        if (this.tags != null && !this.tags.isEmpty()) {
            joiner.add("tags:" + this.tags.stream().sorted().collect(Collectors.joining(",")));
        }
        if (this.userId != null) {
            joiner.add("uid:" + this.userId);
        }
        if (this.reviewStatus != null) {
            joiner.add("status:" + this.reviewStatus);
        }

        // 3. 添加固定前缀和MD5摘要
        String baseKey = "PIC_QUERY:" + joiner.toString();
        return DigestUtils.md5DigestAsHex(baseKey.getBytes(StandardCharsets.UTF_8));
    }

}
