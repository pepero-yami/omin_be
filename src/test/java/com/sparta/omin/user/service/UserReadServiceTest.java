package com.sparta.omin.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sparta.omin.app.model.user.dto.UserDto;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.repository.UserRepository;
import com.sparta.omin.app.model.user.service.UserReadService;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserReadServiceTest {

	@InjectMocks
	private UserReadService userReadService;

	@Mock
	private UserRepository userRepository;

	@Test
	@DisplayName("사용자 정보 조회 성공")
	void getUserInfo_Success() {
		// given
		String email = "ss@ss";
		User user = mock(User.class); // 엔티티 모킹

		given(user.getEmail()).willReturn("test@sparta.com");
		given(user.getNickname()).willReturn("오민테스터");
		given(userRepository.findByEmailAndIsDeletedFalse(email)).willReturn(Optional.of(user));

		// when
		UserDto result = userReadService.getUserInfo(email);

		// then
		assertThat(result.email()).isEqualTo("test@sparta.com");
		assertThat(result.nickname()).isEqualTo("오민테스터");
	}

	@Test
	@DisplayName("존재하지 않는 사용자 조회 시 USER_NOT_FOUND 예외 발생")
	void getUserInfo_UserNotFound() {
		// given
		String email = "ss@ss";
		given(userRepository.findByEmailAndIsDeletedFalse(email)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userReadService.getUserInfo(email))
			.isInstanceOf(OminBusinessException.class)
			.hasMessageContaining(ErrorCode.USER_NOT_FOUND.getDescription());
	}
}
