package com.sparta.omin.app.model.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public class UserRegister {

	private UserRegister() {
	}

	public record Request(
		@NotBlank(message = "이메일은 필수입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		String email,

		@NotBlank(message = "이름은 필수입니다.")
		@Size(min = 2, max = 20)
		String name,

		@NotBlank(message = "닉네임은 필수입니다.")
		@Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자여야 합니다.")
		String nickname,

		@NotBlank(message = "비밀번호는 필수입니다.")
		@Pattern(
			regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
			message = "비밀번호는 8~20자의 영문 대소문자, 숫자, 특수문자를 포함해야 합니다."
		)
		String password
	) {

	}

	@Builder
	public record Response(
		String email,
		String name,
		String nickname
	) {

		public static Response from(UserDto userDto) {
			return Response.builder()
				.email(userDto.email())
				.name(userDto.name())
				.nickname(userDto.nickname())
				.build();
		}
	}
}
