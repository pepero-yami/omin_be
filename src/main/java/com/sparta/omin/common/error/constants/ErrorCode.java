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

    //서버 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러"),

    //리뷰 에러
    RATING_POLICY_VIOLATION(HttpStatus.BAD_REQUEST, "별점 정책 위반"),
    REVIEW_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "주문일로부터 2일이 초과되어 리뷰를 작성할 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 주문 건으로 작성된 리뷰가 있습니다."),
    //주문 - 리뷰 에러
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."),
    ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "주문 완료 후에만 리뷰 작성이 가능합니다."),
    ;
    private final HttpStatus status;
    private final String description;
}
