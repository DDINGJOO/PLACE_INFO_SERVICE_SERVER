package com.teambind.placeinfoserver.place.domain.vo;

import com.teambind.placeinfoserver.place.common.exception.CustomException;
import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * URL Value Object
 * URL의 형식을 검증하고 정규화하여 관리
 * <p>
 * HTTP, HTTPS 프로토콜 지원
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Url {
	
	@Column(name = "url", length = 500)
	private String value;
	
	/**
	 * 정적 팩토리 메서드
	 *
	 * @param url URL 문자열
	 * @return Url 객체
	 * @throws CustomException URL 형식이 유효하지 않은 경우
	 */
	public static Url of(String url) {
		if (url == null || url.isBlank()) {
			return null;
		}
		
		String normalized = normalize(url);
		validate(normalized);
		
		Url urlObj = new Url();
		urlObj.value = normalized;
		return urlObj;
	}
	
	/**
	 * URL 정규화
	 * - 앞뒤 공백 제거
	 * - http:// 자동 추가 (프로토콜이 없는 경우)
	 */
	private static String normalize(String url) {
		String trimmed = url.trim();
		
		// 프로토콜이 없으면 https:// 추가
		if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
			trimmed = "https://" + trimmed;
		}
		
		return trimmed;
	}
	
	/**
	 * URL 유효성 검증
	 */
	private static void validate(String url) {
		try {
			URI uri = new URI(url);
			
			// 프로토콜 검증
			String scheme = uri.getScheme();
			if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
				throw new CustomException(ErrorCode.CONTACT_INVALID_URL);
			}
			
			// 호스트 검증
			if (uri.getHost() == null || uri.getHost().isEmpty()) {
				throw new CustomException(ErrorCode.CONTACT_INVALID_URL);
			}
			
			// 길이 검증
			if (url.length() > 500) {
				throw new CustomException(ErrorCode.INVALID_FORMAT);
			}
			
			// URL 파싱 가능 여부 검증
			uri.toURL();
			
		} catch (URISyntaxException | MalformedURLException e) {
			throw new CustomException(ErrorCode.CONTACT_INVALID_URL);
		}
	}
	
	/**
	 * 도메인 추출
	 */
	public String getDomain() {
		try {
			URI uri = new URI(value);
			return uri.getHost();
		} catch (URISyntaxException e) {
			return "";
		}
	}
	
	/**
	 * 프로토콜 추출
	 */
	public String getProtocol() {
		try {
			URI uri = new URI(value);
			return uri.getScheme();
		} catch (URISyntaxException e) {
			return "";
		}
	}
	
	/**
	 * HTTPS 여부 확인
	 */
	public boolean isSecure() {
		return value.startsWith("https://");
	}
	
	/**
	 * 경로 추출
	 */
	public String getPath() {
		try {
			URI uri = new URI(value);
			return uri.getPath();
		} catch (URISyntaxException e) {
			return "";
		}
	}
	
	@Override
	public String toString() {
		return value;
	}
}
