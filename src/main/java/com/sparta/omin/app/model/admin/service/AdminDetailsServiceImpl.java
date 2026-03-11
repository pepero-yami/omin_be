package com.sparta.omin.app.model.admin.service;

import com.sparta.omin.app.model.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDetailsServiceImpl implements UserDetailsService {

	private final AdminRepository adminRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return adminRepository.findByEmailAndIsDeletedFalse(username)
			.orElseThrow(() -> new UsernameNotFoundException(username));
	}
}
