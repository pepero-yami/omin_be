package com.sparta.omin.common.error;

import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OminBusinessException extends RuntimeException {

	private ErrorCode errorCode;
	private String message;

	public OminBusinessException(ErrorCode errorCode) {
		super(errorCode.getDescription());
		this.errorCode = errorCode;
		this.message = errorCode.getDescription();
	}
}