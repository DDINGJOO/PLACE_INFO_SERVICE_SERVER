package com.teambind.placeinfoserver.place.common.util.address.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.placeinfoserver.place.common.util.address.exception.AddressParsingException;
import com.teambind.placeinfoserver.place.domain.enums.AddressSource;
import com.teambind.placeinfoserver.place.dto.request.AddressRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 수동 입력 주소 데이터 파싱 전략
 * 프론트엔드에서 이미 파싱된 형태로 전달받음
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ManualAddressParsingStrategy implements AddressParsingStrategy {
	
	private final ObjectMapper objectMapper;
	
	@Override
	public AddressSource supports() {
		return AddressSource.MANUAL;
	}
	
	@Override
	public AddressRequest parse(Object addressData) {
		if (addressData == null) {
			throw AddressParsingException.nullData();
		}
		
		try {
			return objectMapper.convertValue(addressData, AddressRequest.class);
		} catch (Exception e) {
			log.error("수동 입력 주소 파싱 실패: {}", e.getMessage(), e);
			throw AddressParsingException.manual(e);
		}
	}
}
