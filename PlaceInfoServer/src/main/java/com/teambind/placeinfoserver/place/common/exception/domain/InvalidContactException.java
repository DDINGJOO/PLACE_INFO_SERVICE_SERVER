package com.teambind.placeinfoserver.place.common.exception.domain;

import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import com.teambind.placeinfoserver.place.common.exception.PlaceException;

/**
 * 연락처 정보가 유효하지 않을 때 발생하는 예외
 * HTTP 400 Bad Request
 */
public class InvalidContactException extends PlaceException {

	public InvalidContactException(ErrorCode errorCode) {
		super(errorCode);
	}

	public InvalidContactException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	@Override
	public String getExceptionType() {
		return "DOMAIN";
	}

	public static InvalidContactException invalidPhone(String phoneNumber) {
		return new InvalidContactException(
				ErrorCode.CONTACT_INVALID_PHONE,
				"유효하지 않은 전화번호 형식입니다: " + phoneNumber
		);
	}

	public static InvalidContactException invalidEmail(String email) {
		return new InvalidContactException(
				ErrorCode.CONTACT_INVALID_EMAIL,
				"유효하지 않은 이메일 형식입니다: " + email
		);
	}

	public static InvalidContactException invalidUrl(String url) {
		return new InvalidContactException(
				ErrorCode.CONTACT_INVALID_URL,
				"유효하지 않은 URL 형식입니다: " + url
		);
	}
}
