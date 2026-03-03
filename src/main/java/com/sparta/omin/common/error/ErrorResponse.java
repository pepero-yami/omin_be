package com.sparta.omin.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String error;
    private final Object details;

    public ErrorResponse(String error, Object details) {
        this.error = error;
        this.details = details;
    }

    public static ErrorResponse of(String error) {
        return new ErrorResponse(error, null);
    }

    public static ErrorResponse of(String error, Object details) {
        return new ErrorResponse(error, details);
    }

}