package springboot.online_image_library.common;

import lombok.Data;
import springboot.online_image_library.exception.ErrorCode;

import java.io.Serializable;

/**
 * @author Yu'S'hui'shen
 * @date 2025/5/7
 * @description 封账响应类
 */
@Data
public class BaseResponse<T> implements Serializable {
    private int code;
    private String message;
    // 即使T不可序列化也不会报错
    private transient T data;

    public BaseResponse(int code, T data, String message){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code,T data){
        this(code,data,"");
    }

    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(),null, errorCode.getMessage());
    }

}
