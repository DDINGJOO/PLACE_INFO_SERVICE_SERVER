package com.teambind.placeinfoserver.place.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 장소 운영 타입
 * 장소의 활성화/비활성화 작업을 나타내는 Enum
 */
@Getter
@RequiredArgsConstructor
public enum PlaceOperationType {
	ACTIVATE("activate", "장소 활성화"),
	DEACTIVATE("deactivate", "장소 비활성화");

	private final String value;
	private final String description;

	/**
	 * JSON 직렬화 시 사용할 값
	 */
	@JsonValue
	public String getValue() {
		return value;
	}

	/**
	 * JSON 역직렬화 시 문자열을 Enum으로 변환
	 * 대소문자 구분 없이 변환 지원
	 */
	@JsonCreator
	public static PlaceOperationType fromValue(String value) {
		if (value == null) {
			return null;
		}

		for (PlaceOperationType type : PlaceOperationType.values()) {
			if (type.value.equalsIgnoreCase(value)) {
				return type;
			}
		}

		throw new IllegalArgumentException("Unknown PlaceOperationType: " + value);
	}

	/**
	 * 문자열이 유효한 PlaceOperationType인지 확인
	 */
	public static boolean isValid(String value) {
		if (value == null) {
			return false;
		}

		for (PlaceOperationType type : PlaceOperationType.values()) {
			if (type.value.equalsIgnoreCase(value)) {
				return true;
			}
		}

		return false;
	}
}
