package com.teambind.placeinfoserver.place.service.usecase.common;

import com.teambind.placeinfoserver.place.common.exception.application.InvalidRequestException;

/**
 * ID 변환 유틸리티
 * UseCase 간 공통으로 사용되는 ID 파싱 로직
 */
public final class IdParser {
	
	private IdParser() {
		// 유틸리티 클래스이므로 인스턴스 생성 방지
	}
	
	/**
	 * String ID를 Long으로 안전하게 변환
	 *
	 * @param placeId String 형태의 업체 ID
	 * @return Long 형태의 업체 ID
	 * @throws InvalidRequestException ID 형식이 잘못된 경우
	 */
	public static Long parsePlaceId(String placeId) {
		try {
			return Long.parseLong(placeId);
		} catch (NumberFormatException e) {
			throw InvalidRequestException.invalidFormat("placeId");
		}
	}
}
