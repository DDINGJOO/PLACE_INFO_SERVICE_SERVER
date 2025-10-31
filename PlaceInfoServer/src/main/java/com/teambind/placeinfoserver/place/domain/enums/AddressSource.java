package com.teambind.placeinfoserver.place.domain.enums;

/**
 * 주소 출처 Enum
 * 프론트엔드에서 전송된 주소 데이터의 출처를 구분
 */
public enum AddressSource {
	KAKAO("카카오맵"),
	NAVER("네이버맵"),
	MANUAL("수동 입력");
	
	private final String description;
	
	AddressSource(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
