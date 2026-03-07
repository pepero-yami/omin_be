package com.sparta.omin.app.model.user.service;

import com.sparta.omin.app.model.user.dto.UserDto;
import com.sparta.omin.app.model.user.entity.User;
import com.sparta.omin.app.model.user.repository.UserRepository;
import com.sparta.omin.common.error.OminBusinessException;
import com.sparta.omin.common.error.constants.ErrorCode;
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
			() -> new OminBusinessException(ErrorCode.USER_NOT_FOUND)
		);
		user.edit(nickname, password, passwordEncoder);
		return UserDto.from(user);
	}

	@Transactional
	public void deleteUser(String email) {
		//FIXME @순식님 userId가 없어서 누가 삭제(최종 수정)했는지 반영이 어려워요
		User user = userRepository.findByEmailAndIsDeletedFalse(email).orElseThrow(
			() -> new OminBusinessException(ErrorCode.USER_NOT_FOUND)
		);
		user.softDelete(user.getId()); //FIXME 여기서 관리자 삭제라도 해당유저 아이디가 최종수정자로 들어감
	}
}
