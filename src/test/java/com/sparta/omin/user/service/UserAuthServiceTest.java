package com.sparta.omin.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sparta.omin.app.model.user.client.JwtRedisClient;
import com.sparta.omin.app.model.user.dto.UserDto;
import com.sparta.omin.app.model.user.dto.UserRegister;
import com.sparta.omin.app.model.user.dto.request.UserLoginRequest;
import com.sparta.omin.app.model.user.dto.response.TokenResponse;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.repository.UserRepository;
import com.sparta.omin.app.model.user.service.UserAuthService;
import com.sparta.omin.app.security.jwt.JwtUtil;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

	@InjectMocks
	private UserAuthService userAuthService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private JwtRedisClient jwtRedisClient;

	@Nested
	@DisplayName("회원가입 테스트")
	class Register {
		@Test
		@DisplayName("회원가입 성공")
		void registerSuccess() {
			// given
			UserRegister.Request request = new UserRegister.Request("홍길동", "gildong", "test@test.com", "password123!");
			given(userRepository.existsByEmail(request.email())).willReturn(false);
			given(passwordEncoder.encode(request.password())).willReturn("encoded_password");

			// User 엔티티가 저장될 때 반환될 객체 모킹 (ID 포함)
			User user = User.builder()
				.name(request.name())
				.nickname(request.nickname())
				.email(request.email())
				.password("encoded_password")
				.build();
			given(userRepository.save(any(User.class))).willReturn(user);

			// when
			UserDto result = userAuthService.register(request);

			// then
			assertThat(result.email()).isEqualTo(request.email());
			verify(userRepository, times(1)).save(any(User.class));
		}

		@Test
		@DisplayName("이미 존재하는 이메일이면 예외 발생")
		void registerFailDuplicateEmail() {
			// given
			UserRegister.Request request = new UserRegister.Request("홍길동", "gildong", "test@test.com", "password123!");
			given(userRepository.existsByEmail(request.email())).willReturn(true);

			// when & then
			assertThatThrownBy(() -> userAuthService.register(request))
				.isInstanceOf(OminBusinessException.class)
				.hasMessageContaining(ErrorCode.ALREADY_EMAIL_EXIST.getDescription());
		}
	}

	@Nested
	@DisplayName("로그인 테스트")
	class Login {
		@Test
		@DisplayName("로그인 성공 - 토큰 반환")
		void loginSuccess() {
			// given
			String email = "test@sparta.com";
			String password = "password123!";
			UUID userId = UUID.randomUUID();
			UserLoginRequest request = new UserLoginRequest(email, password);

			User user = spy(User.builder()
				.email(email)
				.password("encoded_password")
				.build());

			given(user.getId()).willReturn(userId);
			given(userRepository.findByEmailAndIsDeletedFalse(email)).willReturn(Optional.of(user));
			given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);

			TokenResponse tokenResponse = new TokenResponse("access-token", "refresh-token");
			given(jwtUtil.generateToken(any(), anyString(), anyString())).willReturn(tokenResponse);

			// when
			TokenResponse result = userAuthService.login(request);

			// then
			assertThat(result.accessToken()).isEqualTo("access-token");
			assertThat(result.refreshToken()).isEqualTo("refresh-token");

			// Redis 저장 로직 검증
			verify(jwtRedisClient, times(1)).put(
				eq(userId), eq(tokenResponse)
			);
		}

		@Test
		@DisplayName("비밀번호 불일치 시 예외 발생")
		void loginFailInvalidPassword() {
			// given
			UserLoginRequest request = new UserLoginRequest("test@test.com", "wrong_pass");
			User user = User.builder().email("test@test.com").password("encoded_password").build();

			given(userRepository.findByEmailAndIsDeletedFalse(request.email())).willReturn(Optional.of(user));
			given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(false);

			// when & then
			assertThatExceptionOfType(OminBusinessException.class)
					.isThrownBy(() -> userAuthService.login(request))
					.extracting(OminBusinessException::getErrorCode)
					.isEqualTo(ErrorCode.INVALID_PASSWORD)
			;
		}
	}
}
