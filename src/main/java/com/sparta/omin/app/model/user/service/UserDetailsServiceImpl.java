package com.sparta.omin.app.model.user.service;

import com.sparta.omin.app.model.user.repository.UserRepository;
import com.sparta.omin.common.error.ApiException;
import com.sparta.omin.common.error.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByEmailAndIsDeletedFalse(username).orElseThrow(
			() -> new ApiException(ErrorCode.USER_NOT_FOUND)
		);
	}
}
