package com.teambind.placeinfoserver.place.exceptions;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
	private final ErrorCode errorcode;
	
	public CustomException(ErrorCode errorcode) {
		
		super(errorcode.toString());
		this.errorcode = errorcode;
	}
	
	public HttpStatus getStatus() {
		return errorcode.getStatus();
	}
	
	public ErrorCode getErrorCode() {
		return errorcode;
	}
}
