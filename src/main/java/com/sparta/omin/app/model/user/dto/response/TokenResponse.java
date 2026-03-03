package com.sparta.omin.app.model.user.dto.response;

public record TokenResponse(
	String accessToken,
	String refreshToken
) {
}
