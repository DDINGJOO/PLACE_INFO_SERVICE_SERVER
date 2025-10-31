package com.teambind.placeinfoserver.place.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.placeinfoserver.place.domain.enums.AddressSource;
import com.teambind.placeinfoserver.place.dto.request.AddressRequest;
import com.teambind.placeinfoserver.place.dto.request.KakaoAddressRequest;
import com.teambind.placeinfoserver.place.dto.request.NaverAddressRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 주소 데이터 파싱 유틸리티
 * 외부 API(카카오, 네이버) 응답을 AddressRequest로 변환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddressParser {
	
	private final ObjectMapper objectMapper;
	
	/**
	 * 주소 데이터 출처에 따라 적절한 파싱 수행
	 *
	 * @param from        주소 데이터 출처
	 * @param addressData 외부 API 응답 원본 데이터
	 * @return 파싱된 AddressRequest
	 */
	public AddressRequest parse(AddressSource from, Object addressData) {
		if (addressData == null) {
			throw new IllegalArgumentException("주소 데이터가 null입니다.");
		}
		
		return switch (from) {
			case KAKAO -> parseKakaoAddress(addressData);
			case NAVER -> parseNaverAddress(addressData);
			case MANUAL -> parseManualAddress(addressData);
		};
	}
	
	/**
	 * 카카오맵 주소 데이터 파싱
	 */
	private AddressRequest parseKakaoAddress(Object addressData) {
		try {
			// Object를 KakaoAddressRequest로 변환
			KakaoAddressRequest kakaoAddress = objectMapper.convertValue(addressData, KakaoAddressRequest.class);
			return kakaoAddress.toAddressRequest();
		} catch (Exception e) {
			log.error("카카오 주소 파싱 실패: {}", e.getMessage(), e);
			throw new IllegalArgumentException("카카오 주소 데이터 파싱에 실패했습니다: " + e.getMessage());
		}
	}
	
	/**
	 * 네이버맵 주소 데이터 파싱
	 */
	private AddressRequest parseNaverAddress(Object addressData) {
		try {
			// Object를 NaverAddressRequest로 변환
			NaverAddressRequest naverAddress = objectMapper.convertValue(addressData, NaverAddressRequest.class);
			return naverAddress.toAddressRequest();
		} catch (Exception e) {
			log.error("네이버 주소 파싱 실패: {}", e.getMessage(), e);
			throw new IllegalArgumentException("네이버 주소 데이터 파싱에 실패했습니다: " + e.getMessage());
		}
	}
	
	/**
	 * 수동 입력 주소 데이터 파싱
	 * 프론트에서 이미 파싱된 데이터를 받음
	 */
	private AddressRequest parseManualAddress(Object addressData) {
		try {
			// Object를 AddressRequest로 직접 변환
			return objectMapper.convertValue(addressData, AddressRequest.class);
		} catch (Exception e) {
			log.error("수동 입력 주소 파싱 실패: {}", e.getMessage(), e);
			throw new IllegalArgumentException("수동 입력 주소 데이터 파싱에 실패했습니다: " + e.getMessage());
		}
	}
}
