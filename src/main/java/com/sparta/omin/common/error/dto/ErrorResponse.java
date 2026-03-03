package com.sparta.omin.common.error.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String error, Object details) {

    public static ErrorResponse of(String error) {
        return new ErrorResponse(error, null);
    }

    public static ErrorResponse of(String error, Object details) {
        return new ErrorResponse(error, details);
    }

}