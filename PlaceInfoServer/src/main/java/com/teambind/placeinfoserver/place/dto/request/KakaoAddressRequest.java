package com.teambind.placeinfoserver.place.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teambind.placeinfoserver.place.domain.vo.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카카오 주소 API 응답 DTO
 * 카카오 우편번호 서비스에서 반환하는 주소 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoAddressRequest {
	
	// 우편번호 관련
	@JsonProperty("zonecode")
	private String zonecode;
	
	@JsonProperty("postcode")
	private String postcode;
	
	@JsonProperty("postcode1")
	private String postcode1;
	
	@JsonProperty("postcode2")
	private String postcode2;
	
	@JsonProperty("postcodeSeq")
	private String postcodeSeq;
	
	// 주소 정보
	@JsonProperty("address")
	private String address;
	
	@JsonProperty("addressEnglish")
	private String addressEnglish;
	
	@JsonProperty("addressType")
	private String addressType;
	
	// 법정동 코드
	@JsonProperty("bcode")
	private String bcode;
	
	// 법정동 이름
	@JsonProperty("bname")
	private String bname;
	
	@JsonProperty("bnameEnglish")
	private String bnameEnglish;
	
	@JsonProperty("bname1")
	private String bname1;
	
	@JsonProperty("bname1English")
	private String bname1English;
	
	@JsonProperty("bname2")
	private String bname2;
	
	@JsonProperty("bname2English")
	private String bname2English;
	
	// 시도
	@JsonProperty("sido")
	private String sido;
	
	@JsonProperty("sidoEnglish")
	private String sidoEnglish;
	
	// 시군구
	@JsonProperty("sigungu")
	private String sigungu;
	
	@JsonProperty("sigunguEnglish")
	private String sigunguEnglish;
	
	@JsonProperty("sigunguCode")
	private String sigunguCode;
	
	// 사용자 입력 정보
	@JsonProperty("userLanguageType")
	private String userLanguageType;
	
	@JsonProperty("query")
	private String query;
	
	// 건물명
	@JsonProperty("buildingName")
	private String buildingName;
	
	@JsonProperty("buildingCode")
	private String buildingCode;
	
	@JsonProperty("apartment")
	private String apartment;
	
	// 지번 주소
	@JsonProperty("jibunAddress")
	private String jibunAddress;
	
	@JsonProperty("jibunAddressEnglish")
	private String jibunAddressEnglish;
	
	// 도로명 주소
	@JsonProperty("roadAddress")
	private String roadAddress;
	
	@JsonProperty("roadAddressEnglish")
	private String roadAddressEnglish;
	
	// 자동 입력 주소
	@JsonProperty("autoRoadAddress")
	private String autoRoadAddress;
	
	@JsonProperty("autoRoadAddressEnglish")
	private String autoRoadAddressEnglish;
	
	@JsonProperty("autoJibunAddress")
	private String autoJibunAddress;
	
	@JsonProperty("autoJibunAddressEnglish")
	private String autoJibunAddressEnglish;
	
	// 사용자 선택 타입
	@JsonProperty("userSelectedType")
	private String userSelectedType;
	
	@JsonProperty("noSelected")
	private String noSelected;
	
	@JsonProperty("hname")
	private String hname;
	
	// 도로명 코드
	@JsonProperty("roadnameCode")
	private String roadnameCode;
	
	@JsonProperty("roadname")
	private String roadname;
	
	@JsonProperty("roadnameEnglish")
	private String roadnameEnglish;
	
	/**
	 * AddressRequest로 변환
	 * 도로명 주소를 우선으로 사용
	 *
	 * @return AddressRequest 변환된 주소 요청 DTO
	 */
	public AddressRequest toAddressRequest() {
		// 도로명 주소가 있으면 도로명 주소 사용, 없으면 지번 주소 사용
		String selectedAddress = (roadAddress != null && !roadAddress.isEmpty())
				? roadAddress
				: jibunAddress;
		
		return AddressRequest.builder()
				.province(sido)
				.city(sigungu)
				.district(bname2 != null && !bname2.isEmpty() ? bname2 : bname)
				.fullAddress(selectedAddress)
				.addressDetail(buildingName)
				.postalCode(zonecode)
				.build();
	}
	
	/**
	 * Address VO로 직접 변환
	 * 테스트 편의를 위한 헬퍼 메서드
	 *
	 * @return Address Value Object
	 */
	public Address toAddress() {
		return toAddressRequest().toAddress();
	}
	
	/**
	 * 주소 타입이 도로명인지 확인
	 *
	 * @return 도로명 주소이면 true
	 */
	public boolean isRoadAddress() {
		return "R".equals(addressType) || "R".equals(userSelectedType);
	}
	
	/**
	 * 주소 타입이 지번인지 확인
	 *
	 * @return 지번 주소이면 true
	 */
	public boolean isJibunAddress() {
		return "J".equals(addressType) || "J".equals(userSelectedType);
	}
}
