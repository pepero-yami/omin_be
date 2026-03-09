package com.sparta.omin.app.model.user.service;

import com.sparta.omin.app.model.user.dto.UserDto;
import com.sparta.omin.app.model.user.repository.UserRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReadService {

	private final UserRepository userRepository;

	public UserDto getUserInfo(String email) {
		return UserDto.from(
			userRepository.findByEmailAndIsDeletedFalse(email).orElseThrow(
				() -> new OminBusinessException(ErrorCode.USER_NOT_FOUND)
			));
	}

	// payment에서 유저 존재 확인할 때 사용
	@Transactional(readOnly = true)
	public void validateUserExists(UUID userId) {
		if (!userRepository.existsById(userId)) {
			throw new OminBusinessException(ErrorCode.USER_NOT_FOUND);
		}
	}
}
