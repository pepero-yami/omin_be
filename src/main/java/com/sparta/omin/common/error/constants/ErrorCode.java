package com.sparta.omin.common.error.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    //사용자 에러
    ALREADY_EMAIL_EXIST(HttpStatus.CONFLICT, "이미 가입 된 이메일 주소"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "찾을 수 없는 유저"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "패스워드 불일치"),
    NICKNAME_POLICY_VIOLATION(HttpStatus.BAD_REQUEST, "닉네임 정책 위반"),
    PASSWORD_POLICY_VIOLATION(HttpStatus.BAD_REQUEST, "패스워드 정책 위반"),

    //카트 에러
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "조회 가능한 카트가 없습니다."),
    STORE_MISMATCH(HttpStatus.CONFLICT, "다른 가게의 상품은 담을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 카트에 조회 가능한 상품이 없습니다."),
    CART_STORE_CONFLICT(HttpStatus.CONFLICT, "다른 가게의 상품이 담겨있습니다."),

    //서버 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러"),

    // AI - 요청/검증 (400)
    INVALID_AI_PROMPT(HttpStatus.BAD_REQUEST, "Empty AI prompt"),

    // AI - 외부 의존성(OpenAI) (502/503/504/429)
    AI_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "Failed to generate AI response"),
    AI_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI request timeout"),
    AI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "AI rate limited"),
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI service unavailable"),
    AI_EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "Empty AI response"),

    //리뷰 에러
    RATING_POLICY_VIOLATION(HttpStatus.BAD_REQUEST, "별점 정책 위반"),
    REVIEW_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "주문일로부터 2일이 초과되어 리뷰를 작성할 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 주문 건으로 작성된 리뷰가 있습니다."),
    REVIEW_IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지는 최대 5개까지 업로드할 수 있습니다."),
    //주문 - 리뷰 에러
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."),
    ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "주문 완료 후에만 리뷰 작성이 가능합니다."),
    ORDER_USER_MISMATCH(HttpStatus.BAD_REQUEST, "본인의 주문에 대해서만 리뷰를 작성할 수 있습니다."); // 403?
    private final HttpStatus status;
    private final String description;
}
