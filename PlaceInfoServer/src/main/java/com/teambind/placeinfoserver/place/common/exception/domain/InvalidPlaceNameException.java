package com.teambind.placeinfoserver.place.common.exception.domain;

import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import com.teambind.placeinfoserver.place.common.exception.PlaceException;

/**
 * 장소 이름이 유효하지 않을 때 발생하는 예외
 * HTTP 400 Bad Request
 */
public class InvalidPlaceNameException extends PlaceException {

	public InvalidPlaceNameException() {
		super(ErrorCode.PLACE_NAME_REQUIRED);
	}

	public InvalidPlaceNameException(String message) {
		super(ErrorCode.PLACE_NAME_REQUIRED, message);
	}

	@Override
	public String getExceptionType() {
		return "DOMAIN";
	}

	public static InvalidPlaceNameException required() {
		return new InvalidPlaceNameException("장소 이름은 필수입니다.");
	}

	public static InvalidPlaceNameException tooLong(int maxLength) {
		return new InvalidPlaceNameException("장소 이름은 " + maxLength + "자를 초과할 수 없습니다.");
	}

	public static InvalidPlaceNameException empty() {
		return new InvalidPlaceNameException("장소 이름은 공백일 수 없습니다.");
	}
}
