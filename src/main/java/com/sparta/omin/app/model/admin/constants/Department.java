package com.sparta.omin.app.model.admin.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Department {
	CS("고객서비스팀"),
	MARKETING("마케팅팀"),
	DEVELOPMENT("개발팀"),
	MANAGEMENT("경영지원팀");

	private final String description;
}
