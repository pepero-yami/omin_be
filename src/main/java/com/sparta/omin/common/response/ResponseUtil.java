package com.sparta.omin.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ResponseUtil {

    private ResponseUtil() {}

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.of(200, "ok", data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(201, "ok", data));
    }

    //DELETE 204 No Content - 바디 없음
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }
}