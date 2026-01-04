package com.teambind.placeinfoserver.place.common.util.address.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.placeinfoserver.place.common.util.address.exception.AddressParsingException;
import com.teambind.placeinfoserver.place.domain.enums.AddressSource;
import com.teambind.placeinfoserver.place.dto.request.AddressRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 카카오 로컬 API 주소 파싱 전략 테스트
 */
class KakaoLocalAddressParsingStrategyTest {
	
	private KakaoLocalAddressParsingStrategy strategy;
	private ObjectMapper objectMapper;
	
	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		strategy = new KakaoLocalAddressParsingStrategy(objectMapper);
	}
	
	@Test
	@DisplayName("supports() - KAKAO_LOCAL 반환")
	void supports() {
		// when
		AddressSource result = strategy.supports();
		
		// then
		assertThat(result).isEqualTo(AddressSource.KAKAO_LOCAL);
	}
	
	@Nested
	@DisplayName("도로명 주소가 있는 경우")
	class WithRoadAddress {
		
		@Test
		@DisplayName("도로명 주소 정보를 우선적으로 파싱")
		void parse_WithRoadAddress() {
			// given
			Map<String, Object> roadAddress = new HashMap<>();
			roadAddress.put("address_name", "서울 강남구 테헤란로 152");
			roadAddress.put("region_1depth_name", "서울");
			roadAddress.put("region_2depth_name", "강남구");
			roadAddress.put("region_3depth_name", "역삼동");
			roadAddress.put("road_name", "테헤란로");
			roadAddress.put("main_building_no", "152");
			roadAddress.put("building_name", "강남파이낸스센터");
			roadAddress.put("zone_no", "06236");
			
			Map<String, Object> address = new HashMap<>();
			address.put("address_name", "서울 강남구 역삼동 737");
			address.put("region_1depth_name", "서울");
			address.put("region_2depth_name", "강남구");
			address.put("region_3depth_name", "역삼동");
			
			Map<String, Object> kakaoLocalData = new HashMap<>();
			kakaoLocalData.put("address_name", "서울 강남구 역삼동 737");
			kakaoLocalData.put("address_type", "REGION_ADDR");
			kakaoLocalData.put("x", "127.0365645");
			kakaoLocalData.put("y", "37.5000354");
			kakaoLocalData.put("road_address", roadAddress);
			kakaoLocalData.put("address", address);
			
			// when
			AddressRequest result = strategy.parse(kakaoLocalData);
			
			// then
			assertThat(result).isNotNull();
			assertThat(result.getProvince()).isEqualTo("서울");
			assertThat(result.getCity()).isEqualTo("강남구");
			assertThat(result.getDistrict()).isEqualTo("역삼동");
			assertThat(result.getFullAddress()).isEqualTo("서울 강남구 테헤란로 152");
			assertThat(result.getAddressDetail()).isEqualTo("강남파이낸스센터");
			assertThat(result.getPostalCode()).isEqualTo("06236");
		}
		
		@Test
		@DisplayName("건물명이 없는 도로명 주소 파싱")
		void parse_WithRoadAddressWithoutBuildingName() {
			// given
			Map<String, Object> roadAddress = new HashMap<>();
			roadAddress.put("address_name", "서울 강남구 테헤란로 152");
			roadAddress.put("region_1depth_name", "서울");
			roadAddress.put("region_2depth_name", "강남구");
			roadAddress.put("region_3depth_name", "역삼동");
			roadAddress.put("zone_no", "06236");
			roadAddress.put("building_name", "");
			
			Map<String, Object> kakaoLocalData = new HashMap<>();
			kakaoLocalData.put("address_name", "서울 강남구 테헤란로 152");
			kakaoLocalData.put("road_address", roadAddress);
			
			// when
			AddressRequest result = strategy.parse(kakaoLocalData);
			
			// then
			assertThat(result.getFullAddress()).isEqualTo("서울 강남구 테헤란로 152");
			assertThat(result.getAddressDetail()).isEmpty();
		}
	}
	
	@Nested
	@DisplayName("지번 주소만 있는 경우")
	class WithAddressOnly {
		
		@Test
		@DisplayName("지번 주소 정보로 파싱")
		void parse_WithAddressOnly() {
			// given
			Map<String, Object> address = new HashMap<>();
			address.put("address_name", "강원 강릉시 주문진읍 장덕리 123");
			address.put("region_1depth_name", "강원");
			address.put("region_2depth_name", "강릉시");
			address.put("region_3depth_name", "주문진읍");
			address.put("b_code", "4215033000");
			
			Map<String, Object> kakaoLocalData = new HashMap<>();
			kakaoLocalData.put("address_name", "강원 강릉시 주문진읍 장덕리 123");
			kakaoLocalData.put("address_type", "REGION");
			kakaoLocalData.put("address", address);
			kakaoLocalData.put("road_address", null);
			
			// when
			AddressRequest result = strategy.parse(kakaoLocalData);
			
			// then
			assertThat(result).isNotNull();
			assertThat(result.getProvince()).isEqualTo("강원");
			assertThat(result.getCity()).isEqualTo("강릉시");
			assertThat(result.getDistrict()).isEqualTo("주문진읍");
			assertThat(result.getFullAddress()).isEqualTo("강원 강릉시 주문진읍 장덕리 123");
			assertThat(result.getAddressDetail()).isNull();
			assertThat(result.getPostalCode()).isNull();
		}
	}
	
	@Nested
	@DisplayName("최소 데이터만 있는 경우")
	class WithMinimalData {
		
		@Test
		@DisplayName("address_name만 있어도 파싱 성공")
		void parse_WithAddressNameOnly() {
			// given
			Map<String, Object> kakaoLocalData = new HashMap<>();
			kakaoLocalData.put("address_name", "서울 강남구 역삼동 737");
			kakaoLocalData.put("address_type", "REGION");
			
			// when
			AddressRequest result = strategy.parse(kakaoLocalData);
			
			// then
			assertThat(result).isNotNull();
			assertThat(result.getFullAddress()).isEqualTo("서울 강남구 역삼동 737");
		}
	}
	
	@Nested
	@DisplayName("예외 케이스")
	class ExceptionCases {
		
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
			String invalidData = "잘못된 문자열 데이터";
			
			// when & then
			assertThatThrownBy(() -> strategy.parse(invalidData))
					.isInstanceOf(AddressParsingException.class)
					.hasMessageContaining("카카오 로컬 API 주소 데이터 파싱에 실패했습니다");
		}
	}
	
	@Nested
	@DisplayName("실제 API 응답 구조 테스트")
	class RealApiResponseTest {
		
		@Test
		@DisplayName("카카오 로컬 API 실제 응답 구조 파싱")
		void parse_RealApiResponse() {
			// given - 실제 카카오 로컬 API 응답 구조
			Map<String, Object> roadAddress = new HashMap<>();
			roadAddress.put("address_name", "인천 연수구 아트센터대로168번길 100");
			roadAddress.put("region_1depth_name", "인천");
			roadAddress.put("region_2depth_name", "연수구");
			roadAddress.put("region_3depth_name", "송도동");
			roadAddress.put("road_name", "아트센터대로168번길");
			roadAddress.put("underground_yn", "N");
			roadAddress.put("main_building_no", "100");
			roadAddress.put("sub_building_no", "");
			roadAddress.put("building_name", "한라 웨스턴파크 송도");
			roadAddress.put("zone_no", "22006");
			roadAddress.put("x", "126.6396003");
			roadAddress.put("y", "37.3894592");
			
			Map<String, Object> address = new HashMap<>();
			address.put("address_name", "인천 연수구 송도동 29-1");
			address.put("region_1depth_name", "인천");
			address.put("region_2depth_name", "연수구");
			address.put("region_3depth_name", "송도동");
			address.put("region_3depth_h_name", "송도1동");
			address.put("h_code", "2818553500");
			address.put("b_code", "2818510300");
			address.put("mountain_yn", "N");
			address.put("main_address_no", "29");
			address.put("sub_address_no", "1");
			address.put("x", "126.6396003");
			address.put("y", "37.3894592");
			
			Map<String, Object> kakaoLocalData = new HashMap<>();
			kakaoLocalData.put("address_name", "인천 연수구 송도동 29-1");
			kakaoLocalData.put("address_type", "REGION_ADDR");
			kakaoLocalData.put("x", "126.6396003");
			kakaoLocalData.put("y", "37.3894592");
			kakaoLocalData.put("road_address", roadAddress);
			kakaoLocalData.put("address", address);
			
			// when
			AddressRequest result = strategy.parse(kakaoLocalData);
			
			// then
			assertThat(result.getProvince()).isEqualTo("인천");
			assertThat(result.getCity()).isEqualTo("연수구");
			assertThat(result.getDistrict()).isEqualTo("송도동");
			assertThat(result.getFullAddress()).isEqualTo("인천 연수구 아트센터대로168번길 100");
			assertThat(result.getAddressDetail()).isEqualTo("한라 웨스턴파크 송도");
			assertThat(result.getPostalCode()).isEqualTo("22006");
		}
	}
}
