package com.sparta.omin.app.model.admin.dto.request;

import com.sparta.omin.app.model.admin.constants.Department;

public record MasterRegister (
	String masterKey,
	String name,
	String email,
	String password,
	Department department
) {

}
