package com.teambind.placeinfoserver.place.common.exception.domain;

import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import com.teambind.placeinfoserver.place.common.exception.PlaceException;

/**
 * 장소의 상태가 유효하지 않을 때 발생하는 예외
 * (예: 이미 삭제된 장소, 이미 활성화된 장소 등)
 * HTTP 400 Bad Request
 */
public class InvalidPlaceStateException extends PlaceException {

	public InvalidPlaceStateException(ErrorCode errorCode) {
		super(errorCode);
	}

	public InvalidPlaceStateException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	@Override
	public String getExceptionType() {
		return "DOMAIN";
	}

	public static InvalidPlaceStateException alreadyDeleted() {
		return new InvalidPlaceStateException(
				ErrorCode.PLACE_ALREADY_DELETED,
				"이미 삭제된 장소입니다."
		);
	}

	public static InvalidPlaceStateException notActive() {
		return new InvalidPlaceStateException(
				ErrorCode.PLACE_NOT_ACTIVE,
				"활성화되지 않은 장소입니다."
		);
	}

	public static InvalidPlaceStateException alreadyActive() {
		return new InvalidPlaceStateException(
				ErrorCode.PLACE_ALREADY_ACTIVE,
				"이미 활성화된 장소입니다."
		);
	}
}
