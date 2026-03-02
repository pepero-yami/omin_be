package com.sparta.omin.app.model.user.service;

import com.sparta.omin.app.model.user.dto.UserDto;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.repository.UserRepository;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserWriteService {

	private final UserRepository userRepository;

	@Transactional
	public UserDto editInfo(String userId, String nickname, String password) {
		User user = userRepository.findByIdAndIsDeletedFalse(UUID.fromString(userId)).orElseThrow(
			() -> new ApiException(ErrorCode.USER_NOT_FOUND)
		);
		user.edit(nickname, password);
		return UserDto.from(user);
	}

	@Transactional
	public void deleteUser(String userId) {
		UUID id = UUID.fromString(userId);
		User user = userRepository.findByIdAndIsDeletedFalse(id).orElseThrow(
			() -> new ApiException(ErrorCode.USER_NOT_FOUND)
		);
		user.softDelete(id);
	}
}
