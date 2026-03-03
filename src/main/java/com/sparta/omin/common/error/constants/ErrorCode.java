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
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러")
	;
	private final HttpStatus status;
	private final String description;
}
