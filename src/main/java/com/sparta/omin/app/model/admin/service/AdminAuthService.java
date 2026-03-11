package com.sparta.omin.app.model.admin.service;

import com.sparta.omin.app.model.admin.dto.AdminDto;
import com.sparta.omin.app.model.admin.dto.request.AdminLoginRequest;
import com.sparta.omin.app.model.admin.dto.request.AdminRegister;
import com.sparta.omin.app.model.admin.entity.Admin;
import com.sparta.omin.app.model.admin.repository.AdminRepository;
import com.sparta.omin.app.model.user.client.JwtRedisClient;
import com.sparta.omin.app.model.user.dto.response.TokenResponse;
import com.sparta.omin.app.security.jwt.JwtUtil;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {

	private final AdminRepository adminRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final JwtRedisClient jwtRedisClient;

	public AdminDto register(AdminRegister request) {
		if (adminRepository.existsByEmail(request.email())) {
			throw new OminBusinessException(ErrorCode.ALREADY_EMAIL_EXIST);
		}
		return AdminDto.from(adminRepository.save(
			Admin.builder()
				.name(request.name())
				.department(request.department())
				.email(request.email())
				.password(passwordEncoder.encode(request.password()))
				.build()
		));
	}

	public TokenResponse login(AdminLoginRequest request) {
		Admin admin = adminRepository.findByEmailAndIsDeletedFalse(request.email()).orElseThrow(
			() -> new OminBusinessException(ErrorCode.USER_NOT_FOUND)
		);
		if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
			throw new OminBusinessException(ErrorCode.INVALID_PASSWORD);
		}
		TokenResponse tokenResponse = jwtUtil.generateToken(admin.getId(), admin.getEmail(),
			admin.getRole().name());
		jwtRedisClient.put(admin.getId(), tokenResponse);
		return tokenResponse;
	}
}
