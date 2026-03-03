package com.sparta.omin.common.error;

import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApiException extends RuntimeException {

	private ErrorCode errorCode;

	public ApiException(ErrorCode errorCode) {
		super(errorCode.getDescription());
		this.errorCode = errorCode;
	}

	public ApiException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}