package com.sparta.omin.app.model.user.dto.request;

public record EditUserInfoRequest(
	String nickname,
	String password
) {
}
