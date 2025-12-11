package com.teambind.placeinfoserver.place.common.util.address.exception;

/**
 * 주소 데이터 파싱 실패 시 발생하는 예외
 */
public class AddressParsingException extends RuntimeException {
	
	public AddressParsingException(String message) {
		super(message);
	}
	
	public AddressParsingException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public static AddressParsingException kakao(Throwable cause) {
		return new AddressParsingException("카카오 주소 데이터 파싱에 실패했습니다.", cause);
	}
	
	public static AddressParsingException naver(Throwable cause) {
		return new AddressParsingException("네이버 주소 데이터 파싱에 실패했습니다.", cause);
	}
	
	public static AddressParsingException manual(Throwable cause) {
		return new AddressParsingException("수동 입력 주소 데이터 파싱에 실패했습니다.", cause);
	}
	
	public static AddressParsingException nullData() {
		return new AddressParsingException("주소 데이터가 null입니다.");
	}
}
