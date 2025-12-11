package com.teambind.placeinfoserver.place.common.exception.application;

import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import com.teambind.placeinfoserver.place.common.exception.PlaceException;

/**
 * 인증되지 않은 요청일 때 발생하는 예외
 * HTTP 401 Unauthorized
 */
public class UnauthorizedException extends PlaceException {
	
	public UnauthorizedException() {
		super(ErrorCode.UNAUTHORIZED);
	}
	
	public UnauthorizedException(String message) {
		super(ErrorCode.UNAUTHORIZED, message);
	}
	
	public static UnauthorizedException tokenExpired() {
		return new UnauthorizedException("인증 토큰이 만료되었습니다.");
	}
	
	public static UnauthorizedException tokenInvalid() {
		return new UnauthorizedException("유효하지 않은 인증 토큰입니다.");
	}
	
	public static UnauthorizedException tokenMissing() {
		return new UnauthorizedException("인증 토큰이 필요합니다.");
	}
	
	@Override
	public String getExceptionType() {
		return "APPLICATION";
	}
}
