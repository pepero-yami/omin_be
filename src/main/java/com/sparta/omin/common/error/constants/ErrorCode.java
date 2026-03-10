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
	TOKEN_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 저장 실패"),

	//카트 에러
	CART_NOT_FOUND(HttpStatus.NOT_FOUND, "조회 가능한 카트가 없습니다."),
	STORE_MISMATCH(HttpStatus.CONFLICT, "다른 가게의 상품은 담을 수 없습니다."),
	CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 카트에 조회 가능한 상품이 없습니다."),
	CART_STORE_CONFLICT(HttpStatus.CONFLICT, "다른 가게의 상품이 담겨있습니다."),
	CART_CHANGE_FAIL(HttpStatus.CONFLICT, "장바구니를 추가할 수 없습니다."),

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

	// 카카오 API 관련 에러
	EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 연동 중 에러가 발생했습니다."),
	SEARCH_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "검색 결과가 존재하지 않습니다."),
	KAKAO_EMPTY_QUERY(HttpStatus.BAD_REQUEST, "주소(query)가 비어있습니다."),
	KAKAO_NO_RESULT(HttpStatus.NOT_FOUND, "카카오 주소 검색 결과가 없습니다."),
	KAKAO_API_CALL_FAILED(HttpStatus.BAD_GATEWAY, "카카오 주소 검색 API 호출에 실패했습니다."),
	KAKAO_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "카카오 주소 검색 API 응답이 올바르지 않습니다."),
	KAKAO_NO_LATITUDE(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 주소 검색 결과에 위도(y) 값이 없습니다."),
	KAKAO_NO_LONGITUDE(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 주소 검색 결과에 경도(x) 값이 없습니다."),
	KAKAO_INVALID_COORDINATE(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 주소 검색 결과의 좌표 값이 올바르지 않습니다."),

    //서버 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러"),
    // AI - 요청/검증 (400)
    INVALID_AI_PROMPT(HttpStatus.BAD_REQUEST, "Empty AI prompt"),
    // 결제 에러
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "조회 가능한 결제 정보가 없습니다."),
    PAYMENT_ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 결제입니다."),
    PAYMENT_UNAUTHORIZED(HttpStatus.FORBIDDEN, "해당 결제에 대한 권한이 없습니다."),
    PAYMENT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 완료된 결제입니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다."),
    PAYMENT_INVALID_STATUS(HttpStatus.BAD_REQUEST, "결제를 진행할 수 없는 상태입니다."),
    PAYMENT_ORDER_USER_MISMATCH(HttpStatus.BAD_REQUEST, "본인의 주문에 대해서만 처리가 가능합니다."),
    PAYMENT_ORDER_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 취소된 주문은 다시 결제할 수 없습니다. 새로 주문해 주세요."),
    PAYMENT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "현재 결제가 진행 중인 주문입니다. 잠시 후 다시 시도해 주세요."),

	// Product
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product Not Found"),
	PRODUCT_IS_NOT_AVAILABLE_FOR_SALE(HttpStatus.CONFLICT, "상품의 상태가 판매 가능하지 않습니다."),
	PRODUCT_INVALID_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "Invalid product image count."),
	PRODUCT_IMAGE_INVALID_ACCESS(HttpStatus.BAD_REQUEST, "Invalid product image access."),

	// AI - 외부 의존성(OpenAI) (502/503/504/429)
	AI_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "Failed to generate AI response"),
	AI_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI request timeout"),
	AI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "AI rate limited"),
	AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI service unavailable"),
	AI_EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "Empty AI response"),

	//리뷰 에러
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
	RATING_POLICY_VIOLATION(HttpStatus.BAD_REQUEST, "별점 정책 위반"),
	REVIEW_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "주문일로부터 2일이 초과되어 리뷰를 작성할 수 없습니다."),
	REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 주문 건으로 작성된 리뷰가 있습니다."),
	REVIEW_IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지는 최대 5개까지 업로드할 수 있습니다."),
	SELF_REVIEW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자신의 가게에 대한 리뷰를 남길 수 없습니다."),
    REVIEW_USER_MISMATCH(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정할 수 있습니다."),
    REVIEW_UPDATE_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "주문일로부터 2일이 초과되어 리뷰를 수정할 수 없습니다."),
	//주문 - 리뷰 에러,
	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."),
	ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "주문 완료 후에만 리뷰 작성이 가능합니다."),
	ORDER_USER_MISMATCH(HttpStatus.BAD_REQUEST, "본인의 주문에 대해서만 리뷰를 작성할 수 있습니다."),
    ORDER_NOT_OWNED(HttpStatus.FORBIDDEN, "해당 주문에 대한 권한이 없습니다."),
    ORDER_UPDATE_DENIED(HttpStatus.FORBIDDEN, "해당 주문은 수정할 수 없습니다."),
    ORDER_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "주문한지 5분 이상 된 주문은 취소할 수 없습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "처리할 수 없는 주문 요청입니다."),
    ORDER_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 주문입니다."),
    ORDER_OWNER_REJECTED(HttpStatus.CONFLICT, "사장님이 주문을 거절했습니다."),

	// store 에러
	STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 가게를 찾을 수 없습니다."),
	STORE_DUPLICATE_ADDRESS(HttpStatus.CONFLICT, "이미 동일한 주소로 등록된 가게가 존재합니다."),
	STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 가게에 대한 권한이 없습니다."),
	STORE_STATUS_NOT_PENDING(HttpStatus.CONFLICT, "가게가 승인 대기 상태가 아닙니다"),
	STORE_STATUS_PENDING_CANNOT_MODIFY(HttpStatus.CONFLICT, "승인 대기 중인 가게의 상태는 변경 불가합니다."),
	STORE_STATUS_INVALID_CHANGE(HttpStatus.BAD_REQUEST, "승인 대기 상태로는 변경할 수 없습니다."),
    STORE_HAS_ACTIVE_ORDERS(HttpStatus.CONFLICT, "진행 중인 주문이 있어 가게를 닫을 수 없습니다."),
    STORE_OPENED_CANNOT_DELETE(HttpStatus.CONFLICT, "영업중인 매장은 삭제할 수 없습니다. 먼저 영업 종료 처리를 해주세요."),

    // store image 에러
    STORE_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이미지를 찾을 수 없습니다."),
	STORE_IMAGE_COUNT_MISMATCH(HttpStatus.BAD_REQUEST, "추가한 이미지 수와 업로드된 파일 수가 일치하지 않습니다."),
	STORE_IMAGE_INVALID_ACTION(HttpStatus.BAD_REQUEST, "추가한 이미지는 id가 없어야 하고, 기존-삭제할 이미지는 id가 필수입니다."),
	STORE_IMAGE_MAX_EXCEEDED(HttpStatus.BAD_REQUEST, "최종 이미지 수는 최대 10개까지 허용됩니다."),
	STORE_IMAGE_DUPLICATE_ID(HttpStatus.BAD_REQUEST, "이미지 id가 중복되었습니다."),
	STORE_IMAGE_MIN_REQUIRED(HttpStatus.BAD_REQUEST, "매장 이미지는 최소 1개 이상이어야 합니다."),
	STORE_IMAGE_INVALID_TYPE(HttpStatus.BAD_REQUEST, "허용되지 않는 이미지 파일 형식입니다. (jpg, png, webp만 가능)"),
	STORE_IMAGE_EMPTY(HttpStatus.BAD_REQUEST, "빈 파일은 업로드할 수 없습니다."),

	BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request");

	private final HttpStatus status;
	private final String description;
}