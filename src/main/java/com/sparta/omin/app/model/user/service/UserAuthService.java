package com.sparta.omin.app.model.user.service;

import com.sparta.omin.app.model.user.dto.UserDto;
import com.sparta.omin.app.model.user.dto.UserRegister;
import com.sparta.omin.app.model.user.dto.request.UserLoginRequest;
import com.sparta.omin.app.model.user.dto.response.TokenResponse;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.repository.UserRepository;
import com.sparta.omin.app.security.jwt.JwtUtil;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.concurrent.TimeUnit;
import com.sparta.omin.common.util.AuditUserProvider;
import com.sparta.omin.common.util.AuditUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserAuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final RedisTemplate<String, Object> redisTemplate;

	@Transactional
	public UserDto register(UserRegister.Request request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new ApiException(ErrorCode.ALREADY_EMAIL_EXIST);
		}
		return UserDto.from(userRepository.save(User.builder()
			.name(request.name())
			.nickname(request.nickname())
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.build()));
//		// 회원가입 시 created_by/updated_by NOT NULL 충족하도록 2단계로 나눔
//		User user = User.builder()
//				.name(request.name())
//				.nickname(request.nickname())
//				.email(request.email())
//				.password(passwordEncoder.encode(request.password()))
//				.build();
//
//		user.initAuditFieldsForSignUp(AuditUserProvider.currentUserId());
//
//		return UserDto.from(userRepository.save(user));

//		user.initAuditFieldsForSignUp(AuditUserProvider.currentUserId());

//		return UserDto.from(userRepository.save(user));
	}

	public TokenResponse login(UserLoginRequest request) {
		User user = userRepository.findByEmailAndIsDeletedFalse(request.email()).orElseThrow(
			() -> new ApiException(ErrorCode.USER_NOT_FOUND)
		);
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new ApiException(ErrorCode.INVALID_PASSWORD);
		}
		TokenResponse tokenResponse = jwtUtil.generateToken(user.getId(), user.getEmail(),
			user.getRole().name());
		redisTemplate.opsForValue().set(
			"RT:"+user.getEmail(),
			tokenResponse.refreshToken(),
			1000 * 60 * 60 * 12,
			TimeUnit.MILLISECONDS);
		return tokenResponse;
	}
}