package com.sparta.omin.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;

import com.sparta.omin.app.model.user.dto.UserDto;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.repository.UserRepository;
import com.sparta.omin.app.model.user.service.UserWriteService;
import com.sparta.omin.common.error.ApiException;
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
class UserWriteServiceTest {

	@InjectMocks
	private UserWriteService userWriteService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Nested
	@DisplayName("사용자 정보 수정")
	class EditInfo {
		@Test
		@DisplayName("닉네임과 비밀번호 수정 성공")
		void editInfo_Success() {
			// given
			UUID userId = UUID.randomUUID();
			String newNickname = "새닉네임";
			String newRawPassword = "NewPassword123!";
			String encodedPassword = "encoded_new_password";

			// User 엔티티를 spy로 생성하여 실제 로직(edit)은 실행되되 특정 메서드만 모킹 가능하게 함
			User user = spy(User.builder()
				.nickname("기존닉네임")
				.password("old_password")
				.build());

			given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));
			given(passwordEncoder.encode(newRawPassword)).willReturn(encodedPassword);

			// when
			UserDto result = userWriteService.editInfo(userId.toString(), newNickname, newRawPassword);

			// then
			assertThat(result.nickname()).isEqualTo(newNickname);
			// 실제 엔티티의 password가 암호화된 값으로 변경되었는지 확인
			assertThat(user.getPassword()).isEqualTo(encodedPassword);
		}

		@Test
		@DisplayName("수정 시 비밀번호 정책 위반하면 예외 발생")
		void editInfo_PasswordPolicyViolation() {
			// given
			UUID userId = UUID.randomUUID();
			String invalidPassword = "123"; // 정책 위반 (짧음)
			User user = User.builder().nickname("기존").password("old").build();

			given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));

			// when & then
			assertThatThrownBy(() -> userWriteService.editInfo(userId.toString(), "새닉네임", invalidPassword))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining(ErrorCode.PASSWORD_POLICY_VIOLATION.getDescription());
		}
	}

	@Nested
	@DisplayName("사용자 삭제 (Soft Delete)")
	class DeleteUser {
		@Test
		@DisplayName("사용자 삭제 성공")
		void deleteUser_Success() {
			// given
			UUID userId = UUID.randomUUID();
			User user = spy(User.builder().build());
			given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));

			// when
			userWriteService.deleteUser(userId.toString());

			// then
			assertThat(user.isDeleted()).isTrue();
			assertThat(user.getDeletedBy()).isEqualTo(userId);
		}

		@Test
		@DisplayName("삭제하려는 사용자가 없으면 예외 발생")
		void deleteUser_UserNotFound() {
			// given
			UUID userId = UUID.randomUUID();
			given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> userWriteService.deleteUser(userId.toString()))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining(ErrorCode.USER_NOT_FOUND.getDescription());
		}
	}
}
