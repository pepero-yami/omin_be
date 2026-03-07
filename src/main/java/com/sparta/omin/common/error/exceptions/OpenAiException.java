package com.sparta.omin.common.error.exceptions;

import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.Getter;

@Getter
public class OpenAiException extends RuntimeException{

    private final ErrorCode errorCode;

    public OpenAiException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

    public OpenAiException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getDescription(), cause);
        this.errorCode = errorCode;
    }

}
