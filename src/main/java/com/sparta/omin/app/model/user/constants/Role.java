package com.sparta.omin.app.model.user.constants;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
	CUSTOMER(List.of("ROLE_CUSTOMER")),
	OWNER(List.of("ROLE_OWNER", "ROLE_CUSTOMER")),
	MANAGER(List.of("ROLE_MANAGER", "ROLE_OWNER", "ROLE_CUSTOMER")),
	MASTER(List.of("ROLE_MASTER", "ROLE_MANAGER", "ROLE_OWNER", "ROLE_CUSTOMER"));

	private final List<String> roles;

	public List<String> getAuthorities() {
		return roles.stream()
			.map(role -> "ROLE_" + role.replace("ROLE_", ""))
			.collect(Collectors.toList());
	}
}
