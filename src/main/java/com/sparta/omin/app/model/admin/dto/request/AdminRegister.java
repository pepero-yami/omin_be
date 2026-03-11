package com.sparta.omin.app.model.admin.dto.request;


import com.sparta.omin.app.model.admin.constants.Department;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminRegister(
	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	String email,

	@NotBlank(message = "이름은 필수입니다.")
	@Size(min = 2, max = 20)
	String name,

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
		message = "비밀번호는 8~20자의 영문 대소문자, 숫자, 특수문자를 포함해야 합니다."
	)
	String password,

	@NotNull(message = "부서입력은 필수 입니다.")
	Department department
) {

}
