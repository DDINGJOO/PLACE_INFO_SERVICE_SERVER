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
 * 네이버 주소 파싱 전략 테스트
 */
class NaverAddressParsingStrategyTest {

	private NaverAddressParsingStrategy strategy;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		strategy = new NaverAddressParsingStrategy(objectMapper);
	}

	@Test
	@DisplayName("supports() - NAVER 반환")
	void supports() {
		// when
		AddressSource result = strategy.supports();

		// then
		assertThat(result).isEqualTo(AddressSource.NAVER);
	}

	@Test
	@DisplayName("네이버 주소 데이터 파싱 성공 - 도로명 주소")
	void parse_WithRoadAddress() {
		// given
		Map<String, Object> naverData = new HashMap<>();
		naverData.put("zipCode", "22006");
		naverData.put("sido", "인천");
		naverData.put("sigungu", "연수구");
		naverData.put("dong", "송도동");
		naverData.put("roadAddress", "인천 연수구 아트센터대로168번길 100");
		naverData.put("jibunAddress", "인천 연수구 송도동 29-1");

		// when
		AddressRequest result = strategy.parse(naverData);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getProvince()).isEqualTo("인천");
		assertThat(result.getCity()).isEqualTo("연수구");
		assertThat(result.getDistrict()).isEqualTo("송도동");
		assertThat(result.getFullAddress()).isEqualTo("인천 연수구 아트센터대로168번길 100");
		assertThat(result.getPostalCode()).isEqualTo("22006");
	}

	@Test
	@DisplayName("네이버 주소 데이터 파싱 성공 - 지번 주소")
	void parse_WithJibunAddress() {
		// given
		Map<String, Object> naverData = new HashMap<>();
		naverData.put("zipCode", "22006");
		naverData.put("sido", "인천");
		naverData.put("sigungu", "연수구");
		naverData.put("dong", "송도동");
		naverData.put("jibunAddress", "인천 연수구 송도동 29-1");
		naverData.put("roadAddress", null);

		// when
		AddressRequest result = strategy.parse(naverData);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getFullAddress()).isEqualTo("인천 연수구 송도동 29-1");
	}

	@Test
	@DisplayName("네이버 주소 데이터 파싱 성공 - 부분 데이터")
	void parse_WithPartialData() {
		// given
		Map<String, Object> naverData = new HashMap<>();
		naverData.put("sido", "서울");
		naverData.put("sigungu", "강남구");
		naverData.put("roadAddress", "서울 강남구 테헤란로 123");

		// when
		AddressRequest result = strategy.parse(naverData);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getProvince()).isEqualTo("서울");
		assertThat(result.getCity()).isEqualTo("강남구");
		assertThat(result.getFullAddress()).isEqualTo("서울 강남구 테헤란로 123");
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
				.hasMessageContaining("네이버 주소 데이터 파싱에 실패했습니다");
	}

	@Test
	@DisplayName("도로명 주소 우선순위 확인")
	void parse_RoadAddressPriority() {
		// given - 도로명과 지번 모두 있는 경우
		Map<String, Object> naverData = new HashMap<>();
		naverData.put("sido", "서울");
		naverData.put("sigungu", "강남구");
		naverData.put("dong", "역삼동");
		naverData.put("roadAddress", "서울 강남구 테헤란로 123");
		naverData.put("jibunAddress", "서울 강남구 역삼동 123-45");

		// when
		AddressRequest result = strategy.parse(naverData);

		// then - 도로명 주소가 우선 선택됨
		assertThat(result.getFullAddress()).isEqualTo("서울 강남구 테헤란로 123");
	}
}
