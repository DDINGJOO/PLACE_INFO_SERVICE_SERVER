package com.teambind.placeinfoserver.place.common.util.address.exception;

import com.teambind.placeinfoserver.place.domain.enums.AddressSource;

/**
 * 지원하지 않는 주소 소스일 때 발생하는 예외
 */
public class UnsupportedAddressSourceException extends RuntimeException {
	
	public UnsupportedAddressSourceException(AddressSource source) {
		super("지원하지 않는 주소 소스입니다: " + source);
	}
	
	public UnsupportedAddressSourceException(String message) {
		super(message);
	}
}
