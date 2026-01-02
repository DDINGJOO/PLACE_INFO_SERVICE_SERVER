package com.teambind.placeinfoserver.place.domain.enums;

/**
 * 업체 등록 상태 Enum
 * 우리 서비스에 정식 등록된 업체인지 여부를 구분
 */
public enum RegistrationStatus {
	REGISTERED("등록 업체"),
	UNREGISTERED("미등록 업체");

	private final String description;

	RegistrationStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
