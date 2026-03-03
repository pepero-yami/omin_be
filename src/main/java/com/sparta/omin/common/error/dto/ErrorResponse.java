package com.sparta.omin.common.error.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String error;
    private final Object details;

    public static ErrorResponse of(String error) {
        return new ErrorResponse(error, null);
    }

    public static ErrorResponse of(String error, Object details) {
        return new ErrorResponse(error, details);
    }

}