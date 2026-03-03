package com.sparta.omin.app.model.user.dto.request;

public record UserLoginRequest(
	String email,
	String password
) {

}
