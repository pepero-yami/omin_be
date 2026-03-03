package com.sparta.omin.app.controller.user;

import com.sparta.omin.app.model.user.dto.UserRegister;
import com.sparta.omin.app.model.user.dto.request.UserLoginRequest;
import com.sparta.omin.app.model.user.dto.response.TokenResponse;
import com.sparta.omin.app.model.user.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserAuthController {

	private final UserAuthService userAuthService;

	@PostMapping("/auth")
	public ResponseEntity<UserRegister.Response> signUp(@RequestBody UserRegister.Request request) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(UserRegister.Response.from(userAuthService.register(request)));
	}

	@PutMapping("/auth")
	public ResponseEntity<TokenResponse> signIn(@RequestBody UserLoginRequest request) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(userAuthService.login(request));
	}
}
