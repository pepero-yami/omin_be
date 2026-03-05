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

	// Region 에러
	REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 지역(regionId)입니다."),
	REGION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 지역(address)입니다."),
	REGION_INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "유효하지 않은 주소(address)입니다."),

	// Address 에러
	ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 배송지(addressId)입니다."),
	ADDRESS_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 배송지입니다."),
	ADDRESS_DEFAULT_CANNOT_DELETE(HttpStatus.BAD_REQUEST, "기본배송지는 삭제할 수 없습니다."),
	ADDRESS_REGION_NOT_FOUND(HttpStatus.BAD_REQUEST, "등록되지 않은 지역입니다. region 등록이 필요합니다."),
	ADDRESS_DEFAULT_MUST_EXIST(HttpStatus.BAD_REQUEST, "기본배송지는 항상 1개 이상 존재해야 합니다."),

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
	;
	private final HttpStatus status;
	private final String description;
}