package com.teambind.placeinfoserver.place.common.exception.domain;

import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import com.teambind.placeinfoserver.place.common.exception.PlaceException;

/**
 * 장소를 승인할 수 없는 상태일 때 발생하는 예외
 * HTTP 403 Forbidden
 */
public class CannotApprovePlaceException extends PlaceException {

	public CannotApprovePlaceException() {
		super(ErrorCode.PLACE_NOT_APPROVED, "장소를 승인할 수 없는 상태입니다.");
	}

	public CannotApprovePlaceException(String reason) {
		super(ErrorCode.PLACE_NOT_APPROVED, reason);
	}

	@Override
	public String getExceptionType() {
		return "DOMAIN";
	}
}
