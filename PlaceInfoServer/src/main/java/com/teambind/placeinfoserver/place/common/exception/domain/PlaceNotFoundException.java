package com.teambind.placeinfoserver.place.common.exception.domain;

import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import com.teambind.placeinfoserver.place.common.exception.PlaceException;

/**
 * 장소를 찾을 수 없을 때 발생하는 예외
 * HTTP 404 Not Found
 */
public class PlaceNotFoundException extends PlaceException {

	public PlaceNotFoundException() {
		super(ErrorCode.PLACE_NOT_FOUND);
	}

	public PlaceNotFoundException(String placeId) {
		super(ErrorCode.PLACE_NOT_FOUND, "장소를 찾을 수 없습니다. ID: " + placeId);
	}

	@Override
	public String getExceptionType() {
		return "DOMAIN";
	}
}
