package com.teambind.placeinfoserver.place.domain.vo;

import com.teambind.placeinfoserver.place.common.exception.CustomException;
import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * 전화번호 Value Object
 * 전화번호의 형식을 검증하고 정규화하여 관리
 * <p>
 * 지원 형식:
 * - 일반 전화: 02-1234-5678, 031-123-4567
 * - 휴대폰: 010-1234-5678, 01012345678
 * - 1588/1577 등: 1588-1234
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class PhoneNumber {
	
	/**
	 * 전화번호 정규식 패턴
	 * - 일반: 0\d{1,2}-\d{3,4}-\d{4}
	 * - 휴대폰: 01[016789]-\d{3,4}-\d{4}
	 * - 특수번호: 15\d{2}-\d{4}
	 */
	private static final Pattern PHONE_PATTERN = Pattern.compile(
			"^(0\\d{1,2}-?\\d{3,4}-?\\d{4}|01[016789]-?\\d{3,4}-?\\d{4}|15\\d{2}-?\\d{4})$"
	);
	
	@Column(name = "phone_number", length = 20)
	private String value;
	
	/**
	 * 정적 팩토리 메서드
	 *
	 * @param phoneNumber 전화번호 문자열
	 * @return PhoneNumber 객체
	 * @throws CustomException 전화번호 형식이 유효하지 않은 경우
	 */
	public static PhoneNumber of(String phoneNumber) {
		if (phoneNumber == null || phoneNumber.isBlank()) {
			return null;
		}
		
		String normalized = normalize(phoneNumber);
		validate(normalized);
		
		PhoneNumber phone = new PhoneNumber();
		phone.value = normalized;
		return phone;
	}
	
	/**
	 * 전화번호 정규화
	 * - 공백, 괄호 제거
	 * - 하이픈 형식으로 통일
	 */
	private static String normalize(String phoneNumber) {
		// 공백, 괄호 제거
		String cleaned = phoneNumber.replaceAll("[\\s()\\-]", "");
		
		// 하이픈 추가
		if (cleaned.startsWith("02")) {
			// 서울 (02)
			if (cleaned.length() == 9) {
				return cleaned.substring(0, 2) + "-" + cleaned.substring(2, 5) + "-" + cleaned.substring(5);
			} else if (cleaned.length() == 10) {
				return cleaned.substring(0, 2) + "-" + cleaned.substring(2, 6) + "-" + cleaned.substring(6);
			}
		} else if (cleaned.startsWith("01")) {
			// 휴대폰
			if (cleaned.length() == 10) {
				return cleaned.substring(0, 3) + "-" + cleaned.substring(3, 6) + "-" + cleaned.substring(6);
			} else if (cleaned.length() == 11) {
				return cleaned.substring(0, 3) + "-" + cleaned.substring(3, 7) + "-" + cleaned.substring(7);
			}
		} else if (cleaned.startsWith("15") || cleaned.startsWith("16") || cleaned.startsWith("18")) {
			// 특수번호 (1588, 1577 등)
			if (cleaned.length() == 8) {
				return cleaned.substring(0, 4) + "-" + cleaned.substring(4);
			}
		} else if (cleaned.startsWith("0")) {
			// 일반 지역번호
			if (cleaned.length() == 9) {
				return cleaned.substring(0, 2) + "-" + cleaned.substring(2, 5) + "-" + cleaned.substring(5);
			} else if (cleaned.length() == 10) {
				return cleaned.substring(0, 3) + "-" + cleaned.substring(3, 6) + "-" + cleaned.substring(6);
			} else if (cleaned.length() == 11) {
				return cleaned.substring(0, 3) + "-" + cleaned.substring(3, 7) + "-" + cleaned.substring(7);
			}
		}
		
		return cleaned;
	}
	
	/**
	 * 전화번호 유효성 검증
	 */
	private static void validate(String phoneNumber) {
		if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
			throw new CustomException(ErrorCode.CONTACT_INVALID_PHONE);
		}
	}
	
	/**
	 * 숫자만 반환 (하이픈 제거)
	 */
	public String getDigitsOnly() {
		return value.replaceAll("-", "");
	}
	
	/**
	 * 국제 형식으로 변환 (+82)
	 */
	public String toInternationalFormat() {
		String digits = getDigitsOnly();
		if (digits.startsWith("0")) {
			return "+82" + digits.substring(1);
		}
		return "+82" + digits;
	}
	
	@Override
	public String toString() {
		return value;
	}
}
