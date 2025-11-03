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
 * 카카오 주소 파싱 전략 테스트
 */
class KakaoAddressParsingStrategyTest {

	private KakaoAddressParsingStrategy strategy;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		strategy = new KakaoAddressParsingStrategy(objectMapper);
	}

	@Test
	@DisplayName("supports() - KAKAO 반환")
	void supports() {
		// when
		AddressSource result = strategy.supports();

		// then
		assertThat(result).isEqualTo(AddressSource.KAKAO);
	}

	@Test
	@DisplayName("카카오 주소 데이터 파싱 성공 - 도로명 주소")
	void parse_WithRoadAddress() {
		// given
		Map<String, Object> kakaoData = new HashMap<>();
		kakaoData.put("zonecode", "22006");
		kakaoData.put("sido", "인천");
		kakaoData.put("sigungu", "연수구");
		kakaoData.put("bname", "송도동");
		kakaoData.put("bname2", "송도동");
		kakaoData.put("roadAddress", "인천 연수구 아트센터대로168번길 100");
		kakaoData.put("jibunAddress", "인천 연수구 송도동 29-1");
		kakaoData.put("buildingName", "한라 웨스턴파크 송도");
		kakaoData.put("addressType", "R");

		// when
		AddressRequest result = strategy.parse(kakaoData);

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
	@DisplayName("카카오 주소 데이터 파싱 성공 - 지번 주소")
	void parse_WithJibunAddress() {
		// given
		Map<String, Object> kakaoData = new HashMap<>();
		kakaoData.put("zonecode", "22006");
		kakaoData.put("sido", "인천");
		kakaoData.put("sigungu", "연수구");
		kakaoData.put("bname2", "송도동");
		kakaoData.put("jibunAddress", "인천 연수구 송도동 29-1");
		kakaoData.put("roadAddress", null);
		kakaoData.put("buildingName", "한라 웨스턴파크 송도");

		// when
		AddressRequest result = strategy.parse(kakaoData);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getFullAddress()).isEqualTo("인천 연수구 송도동 29-1");
	}

	@Test
	@DisplayName("카카오 주소 데이터 파싱 성공 - bname2 없을 때 bname 사용")
	void parse_UseBnameWhenBname2IsNull() {
		// given
		Map<String, Object> kakaoData = new HashMap<>();
		kakaoData.put("zonecode", "12345");
		kakaoData.put("sido", "서울");
		kakaoData.put("sigungu", "강남구");
		kakaoData.put("bname", "역삼동");
		kakaoData.put("bname2", null);
		kakaoData.put("roadAddress", "서울 강남구 테헤란로 123");

		// when
		AddressRequest result = strategy.parse(kakaoData);

		// then
		assertThat(result.getDistrict()).isEqualTo("역삼동");
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
		// given - 잘못된 타입의 데이터
		String invalidData = "invalid string data";

		// when & then
		assertThatThrownBy(() -> strategy.parse(invalidData))
				.isInstanceOf(AddressParsingException.class)
				.hasMessageContaining("카카오 주소 데이터 파싱에 실패했습니다");
	}

	@Test
	@DisplayName("필수 필드 누락된 데이터도 파싱 성공 - 부분 데이터")
	void parse_WithPartialData() {
		// given - 일부 필드만 있는 데이터
		Map<String, Object> kakaoData = new HashMap<>();
		kakaoData.put("sido", "서울");
		kakaoData.put("roadAddress", "서울 강남구 테헤란로 123");

		// when
		AddressRequest result = strategy.parse(kakaoData);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getProvince()).isEqualTo("서울");
		assertThat(result.getFullAddress()).isEqualTo("서울 강남구 테헤란로 123");
		assertThat(result.getCity()).isNull();
	}
}
