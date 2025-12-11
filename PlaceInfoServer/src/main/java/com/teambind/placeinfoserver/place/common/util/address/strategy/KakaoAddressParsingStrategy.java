package com.teambind.placeinfoserver.place.common.util.address.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.placeinfoserver.place.common.util.address.exception.AddressParsingException;
import com.teambind.placeinfoserver.place.domain.enums.AddressSource;
import com.teambind.placeinfoserver.place.dto.request.AddressRequest;
import com.teambind.placeinfoserver.place.dto.request.KakaoAddressRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 카카오맵 주소 데이터 파싱 전략
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoAddressParsingStrategy implements AddressParsingStrategy {
	
	private final ObjectMapper objectMapper;
	
	@Override
	public AddressSource supports() {
		return AddressSource.KAKAO;
	}
	
	@Override
	public AddressRequest parse(Object addressData) {
		if (addressData == null) {
			throw AddressParsingException.nullData();
		}
		
		try {
			KakaoAddressRequest kakaoAddress = objectMapper.convertValue(
					addressData,
					KakaoAddressRequest.class
			);
			return kakaoAddress.toAddressRequest();
		} catch (Exception e) {
			log.error("카카오 주소 파싱 실패: {}", e.getMessage(), e);
			throw AddressParsingException.kakao(e);
		}
	}
}
