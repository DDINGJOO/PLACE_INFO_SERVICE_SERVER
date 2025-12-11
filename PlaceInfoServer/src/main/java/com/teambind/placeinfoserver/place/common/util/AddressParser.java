package com.teambind.placeinfoserver.place.common.util;

import com.teambind.placeinfoserver.place.common.util.address.exception.UnsupportedAddressSourceException;
import com.teambind.placeinfoserver.place.common.util.address.strategy.AddressParsingStrategy;
import com.teambind.placeinfoserver.place.domain.enums.AddressSource;
import com.teambind.placeinfoserver.place.dto.request.AddressRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 주소 데이터 파싱 유틸리티 (Context)
 * Strategy Pattern을 사용하여 주소 소스별 파싱 전략을 관리
 * <p>
 * 새로운 주소 소스 추가 시:
 * 1. AddressParsingStrategy 인터페이스 구현
 * 2. @Component로 등록
 * -> 자동으로 전략 목록에 추가됨 (OCP 준수)
 */
@Slf4j
@Component
public class AddressParser {
	
	private final Map<AddressSource, AddressParsingStrategy> strategies;
	
	/**
	 * 생성자 주입으로 모든 전략을 자동 등록
	 * Spring이 모든 AddressParsingStrategy 구현체를 주입
	 *
	 * @param strategyList Spring이 자동 주입하는 모든 전략 리스트
	 */
	public AddressParser(List<AddressParsingStrategy> strategyList) {
		this.strategies = strategyList.stream()
				.collect(Collectors.toMap(
						AddressParsingStrategy::supports,
						Function.identity()
				));
		log.info("AddressParser 초기화 완료. 등록된 전략: {}", strategies.keySet());
	}
	
	/**
	 * 주소 데이터 출처에 따라 적절한 파싱 전략 선택 및 실행
	 *
	 * @param from        주소 데이터 출처
	 * @param addressData 외부 API 응답 원본 데이터
	 * @return 파싱된 AddressRequest
	 * @throws UnsupportedAddressSourceException 지원하지 않는 주소 소스인 경우
	 */
	public AddressRequest parse(AddressSource from, Object addressData) {
		AddressParsingStrategy strategy = strategies.get(from);
		
		if (strategy == null) {
			throw new UnsupportedAddressSourceException(from);
		}
		
		return strategy.parse(addressData);
	}
}
