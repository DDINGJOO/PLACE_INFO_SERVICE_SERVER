package com.teambind.placeinfoserver.place.common.exception.domain;

import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import com.teambind.placeinfoserver.place.common.exception.PlaceException;

/**
 * 이미 승인된 장소를 다시 승인하려고 할 때 발생하는 예외
 * HTTP 400 Bad Request
 */
public class AlreadyApprovedException extends PlaceException {

	public AlreadyApprovedException() {
		super(ErrorCode.PLACE_ALREADY_APPROVED);
	}

	public AlreadyApprovedException(String placeId) {
		super(ErrorCode.PLACE_ALREADY_APPROVED, "이미 승인된 장소입니다. ID: " + placeId);
	}

	@Override
	public String getExceptionType() {
		return "DOMAIN";
	}
}
