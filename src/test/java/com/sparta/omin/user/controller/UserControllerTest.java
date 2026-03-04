package com.sparta.omin.user.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.omin.app.controller.user.UserController;
import com.sparta.omin.app.model.user.dto.UserDto;
import com.sparta.omin.app.model.user.dto.request.UserInfoEditRequest;
import com.sparta.omin.app.model.user.service.UserDetailsServiceImpl;
import com.sparta.omin.app.model.user.service.UserReadService;
import com.sparta.omin.app.model.user.service.UserWriteService;
import com.sparta.omin.app.security.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private JwtUtil jwtUtil;

	@MockBean
	private UserDetailsServiceImpl userDetailsService;

	@MockBean
	private UserReadService userReadService;

	@MockBean
	private UserWriteService userWriteService;


	@Test
	@WithMockUser(username = "user-uuid-123")
	@DisplayName("내 정보 조회 성공 - 200 OK")
	void getUserInfoTest() throws Exception {
		// given
		UserDto mockUserDto = UserDto.builder()
			.email("test@example.com")
			.nickname("tester")
			.build();
		given(userReadService.getUserInfo("user-uuid-123")).willReturn(mockUserDto);

		// when & then
		mockMvc.perform(get("/api/v1/users"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("test@example.com"))
			.andExpect(jsonPath("$.nickname").value("tester"));
	}

	@Test
	@WithMockUser(username = "user-uuid-123")
	@DisplayName("정보 수정 성공 - 200 OK")
	void editUserInfoTest() throws Exception {
		// given
		UserInfoEditRequest request = new UserInfoEditRequest("새닉네임", "NewPassword123!");
		UserDto updatedDto = UserDto.builder()
			.email("test@example.com")
			.nickname("새닉네임")
			.build();

		given(userWriteService.editInfo(eq("user-uuid-123"), eq("새닉네임"), eq("NewPassword123!")))
			.willReturn(updatedDto);

		// when & then
		mockMvc.perform(put("/api/v1/users")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nickname").value("새닉네임"));
	}

	@Test
	@WithMockUser(username = "user-uuid-123")
	@DisplayName("회원 탈퇴 성공 - 204 No Content")
	void deleteUserTest() throws Exception {
		// when & then
		mockMvc.perform(delete("/api/v1/users")
				.with(csrf()))
			.andExpect(status().isNoContent());

		// 서비스가 올바른 ID로 호출되었는지 확인
		verify(userWriteService).deleteUser("user-uuid-123");
	}
}