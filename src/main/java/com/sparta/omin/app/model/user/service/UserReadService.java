package com.sparta.omin.app.model.user.service;

import com.sparta.omin.app.model.user.dto.UserDto;
import com.sparta.omin.app.model.user.repository.UserRepository;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReadService {

	private final UserRepository userRepository;

	public UserDto getUserInfo(String email) {
		return UserDto.from(
			userRepository.findByEmailAndIsDeletedFalse(email).orElseThrow(
				() -> new ApiException(ErrorCode.USER_NOT_FOUND)
			));
	}


}
