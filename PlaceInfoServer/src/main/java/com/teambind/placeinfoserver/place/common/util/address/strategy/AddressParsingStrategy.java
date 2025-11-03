package com.teambind.placeinfoserver.place.common.util.address.strategy;

import com.teambind.placeinfoserver.place.domain.enums.AddressSource;
import com.teambind.placeinfoserver.place.dto.request.AddressRequest;

/**
 * 주소 파싱 전략 인터페이스
 * 각 주소 소스(카카오, 네이버, 수동)별로 구체적인 구현 제공
 */
public interface AddressParsingStrategy {

	/**
	 * 이 전략이 지원하는 주소 소스 반환
	 *
	 * @return 지원하는 주소 소스
	 */
	AddressSource supports();

	/**
	 * 주소 데이터를 파싱하여 AddressRequest로 변환
	 *
	 * @param addressData 외부 API 응답 또는 수동 입력 데이터
	 * @return 파싱된 AddressRequest
	 * @throws com.teambind.placeinfoserver.place.common.util.address.exception.AddressParsingException 파싱 실패 시
	 */
	AddressRequest parse(Object addressData);
}
