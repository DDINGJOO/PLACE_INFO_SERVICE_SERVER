package com.teambind.placeinfoserver.place.domain.enums;

/**
 * 앱 타입 Enum
 * API 접근 제어를 위한 앱 구분
 */
public enum AppType {
	GENERAL("일반 앱"),
	PLACE_MANAGER("공간관리자 앱");

	private final String description;

	AppType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
