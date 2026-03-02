package com.sparta.omin.app.model.user.dto;

import com.sparta.omin.app.model.user.entity.User;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserDto(
	UUID id,
	String name,
	String nickname,
	String email,
	String password,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	public static UserDto from(User user) {
		return UserDto.builder()
			.id(user.getId())
			.name(user.getName())
			.nickname(user.getNickname())
			.email(user.getEmail())
			.password(user.getPassword())
			.createdAt(user.getCreatedAt())
			.updatedAt(user.getUpdatedAt())
			.build();
	}

}
