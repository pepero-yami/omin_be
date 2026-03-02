package com.sparta.omin.common.error.constants;

import com.sparta.omin.common.error.ApiException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
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

	//jwt 에러
	INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED, "유효하지 않는 JWT 서명"),
	EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰"),
	UNSUPPORTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "지원되지 않는 JWT 토큰"),
	JWT_CLAIMS_IS_EMPTY(HttpStatus.UNAUTHORIZED, "잘못된 JWT 토큰"),

	//서버 에러
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러")
	;
	private final HttpStatus status;
	private final String description;
}
