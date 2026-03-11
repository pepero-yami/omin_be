package com.sparta.omin.app.model.admin.dto;

import com.sparta.omin.app.model.admin.entity.Admin;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AdminDto (
	String email,
	String name,
	String department,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	public static AdminDto from(Admin admin) {
		return AdminDto.builder()
			.email(admin.getEmail())
			.name(admin.getName())
			.department(admin.getDepartment().getDescription())
			.createdAt(admin.getCreatedAt())
			.updatedAt(admin.getUpdatedAt())
			.build();
	}
}
