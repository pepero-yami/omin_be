package com.sparta.omin.app.model.user.dto.response;

import com.sparta.omin.app.model.user.dto.UserDto;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserInfo(
	UUID id,
	String name,
	String nickname,
	String email,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	public static UserInfo from(UserDto user) {
			return UserInfo.builder()
				.id(user.id())
				.name(user.name())
				.nickname(user.nickname())
				.email(user.email())
				.createdAt(user.createdAt())
				.updatedAt(user.updatedAt())
				.build();
		}
}
