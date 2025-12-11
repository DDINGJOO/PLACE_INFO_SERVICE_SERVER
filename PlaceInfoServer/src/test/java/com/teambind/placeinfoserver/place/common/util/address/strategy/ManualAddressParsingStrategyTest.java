package com.teambind.placeinfoserver.place.common.util.address.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.placeinfoserver.place.common.util.address.exception.AddressParsingException;
import com.teambind.placeinfoserver.place.domain.enums.AddressSource;
import com.teambind.placeinfoserver.place.dto.request.AddressRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 수동 입력 주소 파싱 전략 테스트
 */
class ManualAddressParsingStrategyTest {
	
	private ManualAddressParsingStrategy strategy;
	private ObjectMapper objectMapper;
	
	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		strategy = new ManualAddressParsingStrategy(objectMapper);
	}
	
	@Test
	@DisplayName("supports() - MANUAL 반환")
	void supports() {
		// when
		AddressSource result = strategy.supports();
		
		// then
		assertThat(result).isEqualTo(AddressSource.MANUAL);
	}
	
	@Test
	@DisplayName("수동 입력 주소 데이터 파싱 성공 - 완전한 데이터")
	void parse_WithCompleteData() {
		// given
		Map<String, Object> manualData = new HashMap<>();
		manualData.put("province", "서울");
		manualData.put("city", "강남구");
		manualData.put("district", "역삼동");
		manualData.put("fullAddress", "서울 강남구 테헤란로 123");
		manualData.put("addressDetail", "스타빌딩 5층");
		manualData.put("postalCode", "06234");
		
		// when
		AddressRequest result = strategy.parse(manualData);
		
		// then
		assertThat(result).isNotNull();
		assertThat(result.getProvince()).isEqualTo("서울");
		assertThat(result.getCity()).isEqualTo("강남구");
		assertThat(result.getDistrict()).isEqualTo("역삼동");
		assertThat(result.getFullAddress()).isEqualTo("서울 강남구 테헤란로 123");
		assertThat(result.getAddressDetail()).isEqualTo("스타빌딩 5층");
		assertThat(result.getPostalCode()).isEqualTo("06234");
	}
	
	@Test
	@DisplayName("수동 입력 주소 데이터 파싱 성공 - 부분 데이터")
	void parse_WithPartialData() {
		// given
		Map<String, Object> manualData = new HashMap<>();
		manualData.put("province", "서울");
		manualData.put("fullAddress", "서울 강남구 테헤란로 123");
		
		// when
		AddressRequest result = strategy.parse(manualData);
		
		// then
		assertThat(result).isNotNull();
		assertThat(result.getProvince()).isEqualTo("서울");
		assertThat(result.getFullAddress()).isEqualTo("서울 강남구 테헤란로 123");
		assertThat(result.getCity()).isNull();
		assertThat(result.getAddressDetail()).isNull();
	}
	
	@Test
	@DisplayName("수동 입력 주소 데이터 파싱 성공 - AddressRequest 객체 직접 전달")
	void parse_WithAddressRequestObject() {
		// given
		AddressRequest addressRequest = AddressRequest.builder()
				.province("인천")
				.city("연수구")
				.district("송도동")
				.fullAddress("인천 연수구 아트센터대로168번길 100")
				.addressDetail("한라 웨스턴파크 송도")
				.postalCode("22006")
				.build();
		
		// when
		AddressRequest result = strategy.parse(addressRequest);
		
		// then
		assertThat(result).isNotNull();
		assertThat(result.getProvince()).isEqualTo("인천");
		assertThat(result.getCity()).isEqualTo("연수구");
		assertThat(result.getDistrict()).isEqualTo("송도동");
		assertThat(result.getFullAddress()).isEqualTo("인천 연수구 아트센터대로168번길 100");
		assertThat(result.getAddressDetail()).isEqualTo("한라 웨스턴파크 송도");
		assertThat(result.getPostalCode()).isEqualTo("22006");
	}
	
	@Test
	@DisplayName("null 데이터 파싱 시 예외 발생")
	void parse_NullData_ThrowsException() {
		// when & then
		assertThatThrownBy(() -> strategy.parse(null))
				.isInstanceOf(AddressParsingException.class)
				.hasMessageContaining("주소 데이터가 null입니다");
	}
	
	@Test
	@DisplayName("잘못된 형식 데이터 파싱 시 예외 발생")
	void parse_InvalidFormat_ThrowsException() {
		// given
		String invalidData = "invalid string data";
		
		// when & then
		assertThatThrownBy(() -> strategy.parse(invalidData))
				.isInstanceOf(AddressParsingException.class)
				.hasMessageContaining("수동 입력 주소 데이터 파싱에 실패했습니다");
	}
	
	@Test
	@DisplayName("빈 Map 파싱 - null 필드만 있는 AddressRequest 반환")
	void parse_EmptyMap() {
		// given
		Map<String, Object> emptyData = new HashMap<>();
		
		// when
		AddressRequest result = strategy.parse(emptyData);
		
		// then
		assertThat(result).isNotNull();
		assertThat(result.getProvince()).isNull();
		assertThat(result.getCity()).isNull();
		assertThat(result.getFullAddress()).isNull();
	}
}
