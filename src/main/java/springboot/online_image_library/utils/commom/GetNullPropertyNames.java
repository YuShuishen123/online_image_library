package springboot.online_image_library.utils.commom;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/27
 * @description 获取对象中值为 null 的属性
 */

public class GetNullPropertyNames {

    /**
     * 获取对象中值为 null 的属性名
     *
     * @param source 源对象
     * @return null 值的属性名数组
     */
    private static String[] getNullPropertyNames(Object source) {
        BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        List<String> nullPropertyNames = new ArrayList<>();
        for (PropertyDescriptor pd : pds) {
            if (src.getPropertyValue(pd.getName()) == null || "".equals(src.getPropertyValue(pd.getName()))) {
                nullPropertyNames.add(pd.getName());
            }
        }
        return nullPropertyNames.toArray(new String[0]);
    }

}
