package springboot.online_image_library.modle.BO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 保存各级空间的容量配置信息
 *
 * @author Yu'S'hui'shen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceLevel {

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;
}
