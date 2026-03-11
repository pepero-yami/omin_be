package com.sparta.omin.app.controller.admin;

import com.sparta.omin.app.model.admin.dto.request.AdminLoginRequest;
import com.sparta.omin.app.model.admin.dto.request.AdminRegister;
import com.sparta.omin.app.model.admin.dto.response.AdminInfo;
import com.sparta.omin.app.model.admin.service.AdminAuthService;
import com.sparta.omin.app.model.user.dto.response.TokenResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admins/auth")
@RequiredArgsConstructor
public class AdminAuthController {

	private final AdminAuthService adminAuthService;

	@PostMapping
	public ResponseEntity<AdminInfo> signUp(
		@RequestBody AdminRegister request
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(AdminInfo.from(adminAuthService.register(request)));
	}

	@PutMapping
	public ResponseEntity<TokenResponse> signIn(
		@RequestBody AdminLoginRequest request) {
		return ResponseEntity.ok()
			.body(adminAuthService.login(request));
	}
}
