package com.teambind.placeinfoserver.place.common.util.address.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.placeinfoserver.place.common.util.address.exception.AddressParsingException;
import com.teambind.placeinfoserver.place.domain.enums.AddressSource;
import com.teambind.placeinfoserver.place.dto.request.AddressRequest;
import com.teambind.placeinfoserver.place.dto.request.NaverAddressRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 네이버맵 주소 데이터 파싱 전략
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverAddressParsingStrategy implements AddressParsingStrategy {

	private final ObjectMapper objectMapper;

	@Override
	public AddressSource supports() {
		return AddressSource.NAVER;
	}

	@Override
	public AddressRequest parse(Object addressData) {
		if (addressData == null) {
			throw AddressParsingException.nullData();
		}

		try {
			NaverAddressRequest naverAddress = objectMapper.convertValue(
					addressData,
					NaverAddressRequest.class
			);
			return naverAddress.toAddressRequest();
		} catch (Exception e) {
			log.error("네이버 주소 파싱 실패: {}", e.getMessage(), e);
			throw AddressParsingException.naver(e);
		}
	}
}
