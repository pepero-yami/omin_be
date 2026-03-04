package com.sparta.omin.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.omin.app.controller.user.UserAuthController;
import com.sparta.omin.app.model.user.dto.UserDto;
import com.sparta.omin.app.model.user.dto.UserRegister;
import com.sparta.omin.app.model.user.dto.request.UserLoginRequest;
import com.sparta.omin.app.model.user.dto.response.TokenResponse;
import com.sparta.omin.app.model.user.service.UserAuthService;
import com.sparta.omin.app.model.user.service.UserDetailsServiceImpl;
import com.sparta.omin.app.security.jwt.JwtUtil;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserAuthController.class)
class UserAuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserAuthService userAuthService;

	@MockBean
	private UserDetailsServiceImpl userDetailsService;

	@MockBean
	private JwtUtil jwtUtil;

	@Test
	@WithMockUser
	@DisplayName("회원가입 성공 - 201 Created 반환")
	void signUpTest() throws Exception {
		// given
		UserRegister.Request request = new UserRegister.Request(
			"홍길동", "gildong123", "test@test.com", "Password123!"
		);
		// 서비스가 반환할 mock 데이터 (예시 User 객체나 DTO)
		given(userAuthService.register(any())).willReturn(UserDto.builder()
			.id(UUID.randomUUID())
			.name("gildong123")
			.nickname("gd123")
			.email("test@test.com")
			.build());

		// when & then
		mockMvc.perform(post("/api/v1/users/auth")
				.with(csrf()) // CSRF 필터가 켜져있을 경우 대비
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.email").value("test@test.com"))
			.andExpect(jsonPath("$.nickname").value("gd123"));
	}

	@Test
	@WithMockUser
	@DisplayName("로그인 성공 - 200 OK와 토큰 반환")
	void signInTest() throws Exception {
		// given
		UserLoginRequest request = new UserLoginRequest("test@test.com", "Password123!");
		TokenResponse tokenResponse = new TokenResponse("access-token-example", "refresh-token-example");

		given(userAuthService.login(any())).willReturn(tokenResponse);

		// when & then
		mockMvc.perform(put("/api/v1/users/auth")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value("access-token-example"))
			.andExpect(jsonPath("$.refreshToken").value("refresh-token-example"));
	}
}
