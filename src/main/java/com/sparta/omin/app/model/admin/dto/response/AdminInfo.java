package com.sparta.omin.app.model.admin.dto.response;

import com.sparta.omin.app.model.admin.dto.AdminDto;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AdminInfo(
	String name,
	String email,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	public static AdminInfo from(AdminDto adminDto) {
		return AdminInfo.builder()
			.name(adminDto.name())
			.email(adminDto.email())
			.createdAt(adminDto.createdAt())
			.updatedAt(adminDto.updatedAt())
			.build();
	}
}
