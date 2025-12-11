package com.teambind.placeinfoserver.place.common.exception.application;

import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import com.teambind.placeinfoserver.place.common.exception.PlaceException;

/**
 * 권한이 없는 요청일 때 발생하는 예외
 * HTTP 403 Forbidden
 */
public class ForbiddenException extends PlaceException {
	
	public ForbiddenException() {
		super(ErrorCode.FORBIDDEN);
	}
	
	public ForbiddenException(String message) {
		super(ErrorCode.FORBIDDEN, message);
	}
	
	public ForbiddenException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
	
	public static ForbiddenException insufficientPermission() {
		return new ForbiddenException(
				ErrorCode.INSUFFICIENT_PERMISSION,
				"이 작업을 수행할 권한이 없습니다."
		);
	}
	
	public static ForbiddenException ownerOnly() {
		return new ForbiddenException("소유자만 접근할 수 있습니다.");
	}
	
	public static ForbiddenException adminOnly() {
		return new ForbiddenException("관리자만 접근할 수 있습니다.");
	}
	
	@Override
	public String getExceptionType() {
		return "APPLICATION";
	}
}
