package com.sparta.omin.common.error;

import com.sparta.omin.common.response.ApiResponse;
import jakarta.validation.Valid;
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
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.of(404, e.getMessage(), ErrorResponse.of("NOT_FOUND")));
    }

    //409: 중복/상태 충돌
    //TODO(error): DuplicateException 같은 커스텀 예외로 분리하는 게 좋을지도
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.of(409, e.getMessage(), ErrorResponse.of("CONFLICT")));
    }

    //400: Validation 실패 (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            // 같은 필드가 여러 번 걸리면 첫 번째 메시지만 유지(원하면 리스트로 바꿀 수 있음)
            fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }

        ErrorResponse data = ErrorResponse.of("VALIDATION_ERROR", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(400, "validation failed", data));
    }

    //그 외: 500. 운영에서 내부 메시지 노출을 피하기 위해 message는 고정.
    //TODO(error): 로깅/트래킹(Sentry 등) 도입 시 여기에서 correlationId 추가하는 게 좋을 것 같긴 함
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(500, "internal server error", ErrorResponse.of("INTERNAL_ERROR")));
    }
}