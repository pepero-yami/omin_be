package com.sparta.omin.app.model.user.service;

import com.sparta.omin.app.model.user.dto.UserDto;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.repository.UserRepository;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserWriteService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public UserDto editInfo(String email, String nickname, String password) {
		User user = userRepository.findByEmailAndIsDeletedFalse(email).orElseThrow(
			() -> new ApiException(ErrorCode.USER_NOT_FOUND)
		);
		user.edit(nickname, password, passwordEncoder);
		return UserDto.from(user);
	}

	@Transactional
	public void deleteUser(String email) {
		User user = userRepository.findByEmailAndIsDeletedFalse(email).orElseThrow(
			() -> new ApiException(ErrorCode.USER_NOT_FOUND)
		);
		user.softDelete(user.getId());
	}
}
