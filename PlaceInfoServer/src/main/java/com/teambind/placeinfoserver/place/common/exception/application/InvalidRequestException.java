package com.teambind.placeinfoserver.place.common.exception.application;

import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import com.teambind.placeinfoserver.place.common.exception.PlaceException;

/**
 * 요청 데이터가 유효하지 않을 때 발생하는 예외
 * HTTP 400 Bad Request
 */
public class InvalidRequestException extends PlaceException {
	
	public InvalidRequestException() {
		super(ErrorCode.INVALID_INPUT);
	}
	
	public InvalidRequestException(ErrorCode errorCode) {
		super(errorCode);
	}
	
	public InvalidRequestException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
	
	public static InvalidRequestException invalidFormat(String fieldName) {
		return new InvalidRequestException(
				ErrorCode.INVALID_FORMAT,
				"잘못된 형식입니다: " + fieldName
		);
	}
	
	public static InvalidRequestException requiredFieldMissing(String fieldName) {
		return new InvalidRequestException(
				ErrorCode.REQUIRED_FIELD_MISSING,
				"필수 필드가 누락되었습니다: " + fieldName
		);
	}
	
	public static InvalidRequestException valueOutOfRange(String fieldName, String range) {
		return new InvalidRequestException(
				ErrorCode.VALUE_OUT_OF_RANGE,
				fieldName + "의 값이 허용 범위를 벗어났습니다: " + range
		);
	}
	
	public static InvalidRequestException headerMissing(String headerName) {
		return new InvalidRequestException(
				ErrorCode.HEADER_MISSING,
				"잘못된 접근입니다. 필수 헤더가 누락되었습니다: " + headerName
		);
	}
	
	@Override
	public String getExceptionType() {
		return "APPLICATION";
	}
}
