package com.teambind.placeinfoserver.place.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 네이버 주소 API 응답 DTO
 * TODO: 네이버 지도 API 스펙에 맞게 구현 필요
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaverAddressRequest {
	
	// TODO: 네이버 API 스펙에 맞는 필드 추가
	private String roadAddress;
	private String jibunAddress;
	private String sido;
	private String sigungu;
	private String dong;
	private String zipCode;
	
	public AddressRequest toAddressRequest() {
		// TODO: 네이버 주소 파싱 로직 구현
		return AddressRequest.builder()
				.province(sido)
				.city(sigungu)
				.district(dong)
				.fullAddress(roadAddress != null ? roadAddress : jibunAddress)
				.postalCode(zipCode)
				.build();
	}
}
