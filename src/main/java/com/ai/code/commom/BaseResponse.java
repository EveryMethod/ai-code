package com.ai.code.commom;

import com.ai.code.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @author alh
 * @description 统一返回结果类
 * @date 2026/04/27 11:09
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
