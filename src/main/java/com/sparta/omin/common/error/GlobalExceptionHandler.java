package com.sparta.omin.common.error;

import com.sparta.omin.common.response.ApiResponse;
import com.sparta.omin.common.response.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    //TODO(error): 도메인 확장되면 NotFoundException 같은 커스텀 예외로 분리하는 게 좋을지도...
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseUtil.error(HttpStatus.NOT_FOUND, e.getMessage(), "NOT_FOUND");
    }

    //409: 중복/상태 충돌
    //TODO(error): DuplicateException 같은 커스텀 예외로 분리하는 게 좋을지도
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalState(IllegalStateException e) {
        return ResponseUtil.error(HttpStatus.CONFLICT, e.getMessage(), "CONFLICT");
    }

    //502: 외부 API 장애/연동 실패
    @ExceptionHandler(KakaoApiException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleKakaoApiException(KakaoApiException e) {
        return ResponseUtil.error(HttpStatus.BAD_GATEWAY, e.getMessage(), "KAKAO_API_ERROR");
    }

    //400: Validation 실패 (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }

        ErrorResponse data = ErrorResponse.of("VALIDATION_ERROR", fieldErrors);
        return ResponseUtil.error(HttpStatus.BAD_REQUEST, "validation failed", data);
    }

    //그 외: 500. 운영에서 내부 메시지 노출을 피하기 위해 message는 고정.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleException(Exception e) {
        return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, "internal server error", "INTERNAL_ERROR");
    }
}