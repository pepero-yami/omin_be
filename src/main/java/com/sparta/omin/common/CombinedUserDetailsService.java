package com.sparta.omin.common;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CombinedUserDetailsService implements UserDetailsService {

	private final List<UserDetailsService> userDetailsServices;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		for (UserDetailsService service : userDetailsServices) {
			try {
				UserDetails user = service.loadUserByUsername(username);
				if (user != null) return user;
			} catch (UsernameNotFoundException ignored) {}
		}
		throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
	}
}
