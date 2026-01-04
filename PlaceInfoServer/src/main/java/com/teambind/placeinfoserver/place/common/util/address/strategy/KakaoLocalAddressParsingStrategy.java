package com.teambind.placeinfoserver.place.common.util.address.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.placeinfoserver.place.common.util.address.exception.AddressParsingException;
import com.teambind.placeinfoserver.place.domain.enums.AddressSource;
import com.teambind.placeinfoserver.place.dto.request.AddressRequest;
import com.teambind.placeinfoserver.place.dto.request.KakaoLocalAddressRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 카카오 로컬 REST API 주소 데이터 파싱 전략
 * GET https://dapi.kakao.com/v2/local/search/address 응답 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoLocalAddressParsingStrategy implements AddressParsingStrategy {
	
	private final ObjectMapper objectMapper;
	
	@Override
	public AddressSource supports() {
		return AddressSource.KAKAO_LOCAL;
	}
	
	@Override
	public AddressRequest parse(Object addressData) {
		if (addressData == null) {
			throw AddressParsingException.nullData();
		}
		
		try {
			KakaoLocalAddressRequest kakaoLocalAddress = objectMapper.convertValue(
					addressData,
					KakaoLocalAddressRequest.class
			);
			return kakaoLocalAddress.toAddressRequest();
		} catch (Exception e) {
			log.error("카카오 로컬 API 주소 파싱 실패: {}", e.getMessage(), e);
			throw AddressParsingException.kakaoLocal(e);
		}
	}
}
