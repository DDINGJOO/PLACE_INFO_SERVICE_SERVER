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
 * 이메일 Value Object
 * 이메일 주소의 형식을 검증하고 관리
 * <p>
 * RFC 5322 표준을 따르는 간소화된 패턴 사용
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Email {
	
	/**
	 * 이메일 정규식 패턴 (RFC 5322 간소화 버전)
	 */
	private static final Pattern EMAIL_PATTERN = Pattern.compile(
			"^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
	);
	
	@Column(name = "email", length = 100)
	private String value;
	
	/**
	 * 정적 팩토리 메서드
	 *
	 * @param email 이메일 문자열
	 * @return Email 객체
	 * @throws CustomException 이메일 형식이 유효하지 않은 경우
	 */
	public static Email of(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		
		String normalized = normalize(email);
		validate(normalized);
		
		Email emailObj = new Email();
		emailObj.value = normalized;
		return emailObj;
	}
	
	/**
	 * 이메일 정규화
	 * - 소문자 변환
	 * - 앞뒤 공백 제거
	 */
	private static String normalize(String email) {
		return email.trim().toLowerCase();
	}
	
	/**
	 * 이메일 유효성 검증
	 */
	private static void validate(String email) {
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			throw new CustomException(ErrorCode.CONTACT_INVALID_EMAIL);
		}
		
		// 길이 검증
		if (email.length() > 100) {
			throw new CustomException(ErrorCode.INVALID_FORMAT);
		}
	}
	
	/**
	 * 도메인 추출
	 */
	public String getDomain() {
		int atIndex = value.indexOf('@');
		if (atIndex > 0) {
			return value.substring(atIndex + 1);
		}
		return "";
	}
	
	/**
	 * 로컬 파트 추출 (@ 앞부분)
	 */
	public String getLocalPart() {
		int atIndex = value.indexOf('@');
		if (atIndex > 0) {
			return value.substring(0, atIndex);
		}
		return value;
	}
	
	/**
	 * 마스킹된 이메일 반환 (개인정보 보호)
	 * 예: test@example.com -> te**@example.com
	 */
	public String getMasked() {
		String local = getLocalPart();
		String domain = getDomain();
		
		if (local.length() <= 2) {
			return local.charAt(0) + "**@" + domain;
		} else {
			return local.substring(0, 2) + "**@" + domain;
		}
	}
	
	@Override
	public String toString() {
		return value;
	}
}
