package com.teambind.placeinfoserver.place.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카카오 로컬 REST API 주소 검색 응답 DTO
 * GET https://dapi.kakao.com/v2/local/search/address 응답의 documents[n] 구조
 * 프론트엔드에서 사용자가 선택한 단일 document를 전달받음
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoLocalAddressRequest {

	@JsonProperty("address_name")
	private String addressName;

	@JsonProperty("address_type")
	private String addressType;

	@JsonProperty("x")
	private String x;

	@JsonProperty("y")
	private String y;

	@JsonProperty("address")
	private Address address;

	@JsonProperty("road_address")
	private RoadAddress roadAddress;

	/**
	 * 지번 주소 상세 정보
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Address {

		@JsonProperty("address_name")
		private String addressName;

		@JsonProperty("region_1depth_name")
		private String region1DepthName;

		@JsonProperty("region_2depth_name")
		private String region2DepthName;

		@JsonProperty("region_3depth_name")
		private String region3DepthName;

		@JsonProperty("region_3depth_h_name")
		private String region3DepthHName;

		@JsonProperty("h_code")
		private String hCode;

		@JsonProperty("b_code")
		private String bCode;

		@JsonProperty("mountain_yn")
		private String mountainYn;

		@JsonProperty("main_address_no")
		private String mainAddressNo;

		@JsonProperty("sub_address_no")
		private String subAddressNo;

		@JsonProperty("x")
		private String x;

		@JsonProperty("y")
		private String y;
	}

	/**
	 * 도로명 주소 상세 정보
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RoadAddress {

		@JsonProperty("address_name")
		private String addressName;

		@JsonProperty("region_1depth_name")
		private String region1DepthName;

		@JsonProperty("region_2depth_name")
		private String region2DepthName;

		@JsonProperty("region_3depth_name")
		private String region3DepthName;

		@JsonProperty("road_name")
		private String roadName;

		@JsonProperty("underground_yn")
		private String undergroundYn;

		@JsonProperty("main_building_no")
		private String mainBuildingNo;

		@JsonProperty("sub_building_no")
		private String subBuildingNo;

		@JsonProperty("building_name")
		private String buildingName;

		@JsonProperty("zone_no")
		private String zoneNo;

		@JsonProperty("x")
		private String x;

		@JsonProperty("y")
		private String y;
	}

	/**
	 * AddressRequest로 변환
	 * 도로명 주소를 우선으로 사용, 없으면 지번 주소 사용
	 */
	public AddressRequest toAddressRequest() {
		if (roadAddress != null) {
			return AddressRequest.builder()
					.province(roadAddress.getRegion1DepthName())
					.city(roadAddress.getRegion2DepthName())
					.district(roadAddress.getRegion3DepthName())
					.fullAddress(roadAddress.getAddressName())
					.addressDetail(roadAddress.getBuildingName())
					.postalCode(roadAddress.getZoneNo())
					.build();
		}

		if (address != null) {
			return AddressRequest.builder()
					.province(address.getRegion1DepthName())
					.city(address.getRegion2DepthName())
					.district(address.getRegion3DepthName())
					.fullAddress(address.getAddressName())
					.addressDetail(null)
					.postalCode(null)
					.build();
		}

		return AddressRequest.builder()
				.fullAddress(addressName)
				.build();
	}

	/**
	 * 도로명 주소가 있는지 확인
	 */
	public boolean hasRoadAddress() {
		return roadAddress != null && roadAddress.getAddressName() != null;
	}

	/**
	 * 지번 주소가 있는지 확인
	 */
	public boolean hasAddress() {
		return address != null && address.getAddressName() != null;
	}
}
