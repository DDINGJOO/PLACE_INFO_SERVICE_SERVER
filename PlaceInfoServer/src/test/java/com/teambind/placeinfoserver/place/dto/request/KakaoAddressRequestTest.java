package com.teambind.placeinfoserver.place.dto.request;

import com.teambind.placeinfoserver.place.domain.vo.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 카카오 주소 요청 DTO 테스트
 */
class KakaoAddressRequestTest {
	
	@Test
	@DisplayName("카카오 주소 응답을 AddressRequest로 변환 - 도로명 주소")
	void toAddressRequest_WithRoadAddress() {
		// given
		KakaoAddressRequest kakaoAddress = KakaoAddressRequest.builder()
				.zonecode("22006")
				.address("인천 연수구 아트센터대로168번길 100")
				.addressType("R")
				.bcode("2818510600")
				.bname("송도동")
				.bname2("송도동")
				.sido("인천")
				.sigungu("연수구")
				.buildingName("한라 웨스턴파크 송도")
				.jibunAddress("인천 연수구 송도동 29-1")
				.roadAddress("인천 연수구 아트센터대로168번길 100")
				.userSelectedType("R")
				.build();
		
		// when
		AddressRequest result = kakaoAddress.toAddressRequest();
		
		// then
		assertThat(result.getProvince()).isEqualTo("인천");
		assertThat(result.getCity()).isEqualTo("연수구");
		assertThat(result.getDistrict()).isEqualTo("송도동");
		assertThat(result.getFullAddress()).isEqualTo("인천 연수구 아트센터대로168번길 100");
		assertThat(result.getAddressDetail()).isEqualTo("한라 웨스턴파크 송도");
		assertThat(result.getPostalCode()).isEqualTo("22006");
	}
	
	@Test
	@DisplayName("카카오 주소 응답을 AddressRequest로 변환 - 지번 주소 (도로명 주소 없음)")
	void toAddressRequest_WithJibunAddressOnly() {
		// given
		KakaoAddressRequest kakaoAddress = KakaoAddressRequest.builder()
				.zonecode("22006")
				.addressType("J")
				.sido("인천")
				.sigungu("연수구")
				.bname2("송도동")
				.buildingName("한라 웨스턴파크 송도")
				.jibunAddress("인천 연수구 송도동 29-1")
				.roadAddress(null)
				.userSelectedType("J")
				.build();
		
		// when
		AddressRequest result = kakaoAddress.toAddressRequest();
		
		// then
		assertThat(result.getFullAddress()).isEqualTo("인천 연수구 송도동 29-1");
	}
	
	@Test
	@DisplayName("카카오 주소 응답을 Address VO로 변환")
	void toAddress() {
		// given
		KakaoAddressRequest kakaoAddress = KakaoAddressRequest.builder()
				.zonecode("22006")
				.address("인천 연수구 아트센터대로168번길 100")
				.addressType("R")
				.sido("인천")
				.sigungu("연수구")
				.bname2("송도동")
				.buildingName("한라 웨스턴파크 송도")
				.jibunAddress("인천 연수구 송도동 29-1")
				.roadAddress("인천 연수구 아트센터대로168번길 100")
				.build();
		
		// when
		Address result = kakaoAddress.toAddress();
		
		// then
		assertThat(result.getProvince()).isEqualTo("인천");
		assertThat(result.getCity()).isEqualTo("연수구");
		assertThat(result.getDistrict()).isEqualTo("송도동");
		assertThat(result.getFullAddress()).isEqualTo("인천 연수구 아트센터대로168번길 100");
		assertThat(result.getAddressDetail()).isEqualTo("한라 웨스턴파크 송도");
		assertThat(result.getPostalCode()).isEqualTo("22006");
		assertThat(result.isValid()).isTrue();
	}
	
	@Test
	@DisplayName("주소 타입 확인 - 도로명")
	void isRoadAddress() {
		// given
		KakaoAddressRequest kakaoAddress1 = KakaoAddressRequest.builder()
				.addressType("R")
				.build();
		
		KakaoAddressRequest kakaoAddress2 = KakaoAddressRequest.builder()
				.userSelectedType("R")
				.build();
		
		// when & then
		assertThat(kakaoAddress1.isRoadAddress()).isTrue();
		assertThat(kakaoAddress2.isRoadAddress()).isTrue();
	}
	
	@Test
	@DisplayName("주소 타입 확인 - 지번")
	void isJibunAddress() {
		// given
		KakaoAddressRequest kakaoAddress1 = KakaoAddressRequest.builder()
				.addressType("J")
				.build();
		
		KakaoAddressRequest kakaoAddress2 = KakaoAddressRequest.builder()
				.userSelectedType("J")
				.build();
		
		// when & then
		assertThat(kakaoAddress1.isJibunAddress()).isTrue();
		assertThat(kakaoAddress2.isJibunAddress()).isTrue();
	}
	
	@Test
	@DisplayName("bname2가 없으면 bname 사용")
	void useBnameWhenBname2IsEmpty() {
		// given
		KakaoAddressRequest kakaoAddress = KakaoAddressRequest.builder()
				.sido("서울")
				.sigungu("강남구")
				.bname("역삼동")
				.bname2("")
				.roadAddress("서울 강남구 테헤란로 123")
				.zonecode("12345")
				.build();
		
		// when
		AddressRequest result = kakaoAddress.toAddressRequest();
		
		// then
		assertThat(result.getDistrict()).isEqualTo("역삼동");
	}
	
	@Test
	@DisplayName("전체 주소 정보 변환 통합 테스트")
	void fullKakaoAddressConversion() {
		// given - 사용자가 제공한 실제 카카오 API 응답 예시
		KakaoAddressRequest kakaoAddress = KakaoAddressRequest.builder()
				.postcode("")
				.postcode1("")
				.postcode2("")
				.postcodeSeq("")
				.zonecode("22006")
				.address("인천 연수구 아트센터대로168번길 100")
				.addressEnglish("100 Art center-daero 168beon-gil, Yeonsu-gu, Incheon, Republic of Korea")
				.addressType("R")
				.bcode("2818510600")
				.bname("송도동")
				.bnameEnglish("Songdo-dong")
				.bname1("")
				.bname1English("")
				.bname2("송도동")
				.bname2English("Songdo-dong")
				.sido("인천")
				.sidoEnglish("Incheon")
				.sigungu("연수구")
				.sigunguEnglish("Yeonsu-gu")
				.sigunguCode("28185")
				.userLanguageType("K")
				.query("아트센터대로 168번길 100")
				.buildingName("한라 웨스턴파크 송도")
				.buildingCode("2818510600100290001000001")
				.apartment("N")
				.jibunAddress("인천 연수구 송도동 29-1")
				.jibunAddressEnglish("29-1 Songdo-dong, Yeonsu-gu, Incheon, Republic of Korea")
				.roadAddress("인천 연수구 아트센터대로168번길 100")
				.roadAddressEnglish("100 Art center-daero 168beon-gil, Yeonsu-gu, Incheon, Republic of Korea")
				.autoRoadAddress("")
				.autoRoadAddressEnglish("")
				.autoJibunAddress("")
				.autoJibunAddressEnglish("")
				.userSelectedType("R")
				.noSelected("N")
				.hname("")
				.roadnameCode("4856813")
				.roadname("아트센터대로168번길")
				.roadnameEnglish("Art center-daero 168beon-gil")
				.build();
		
		// when
		AddressRequest addressRequest = kakaoAddress.toAddressRequest();
		Address address = kakaoAddress.toAddress();
		
		// then - AddressRequest 검증
		assertThat(addressRequest.getProvince()).isEqualTo("인천");
		assertThat(addressRequest.getCity()).isEqualTo("연수구");
		assertThat(addressRequest.getDistrict()).isEqualTo("송도동");
		assertThat(addressRequest.getFullAddress()).isEqualTo("인천 연수구 아트센터대로168번길 100");
		assertThat(addressRequest.getAddressDetail()).isEqualTo("한라 웨스턴파크 송도");
		assertThat(addressRequest.getPostalCode()).isEqualTo("22006");
		
		// then - Address VO 검증
		assertThat(address.getProvince()).isEqualTo("인천");
		assertThat(address.getCity()).isEqualTo("연수구");
		assertThat(address.getDistrict()).isEqualTo("송도동");
		assertThat(address.getFullAddress()).isEqualTo("인천 연수구 아트센터대로168번길 100");
		assertThat(address.getAddressDetail()).isEqualTo("한라 웨스턴파크 송도");
		assertThat(address.getPostalCode()).isEqualTo("22006");
		assertThat(address.isValid()).isTrue();
		assertThat(address.getShortAddress()).isEqualTo("인천 연수구 송도동");
		
		// then - 주소 타입 검증
		assertThat(kakaoAddress.isRoadAddress()).isTrue();
		assertThat(kakaoAddress.isJibunAddress()).isFalse();
	}
}
