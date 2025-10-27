package com.teambind.placeinfoserver.place.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * 주소 Value Object
 * 주소 관련 필드를 하나의 개념으로 묶어 관리
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode
public class Address {

	/**
	 * 도/시 (예: 서울특별시)
	 */
	@Column(name = "province", length = 50)
	private String province;

	/**
	 * 시/구/군 (예: 강남구)
	 */
	@Column(name = "city", length = 50)
	private String city;

	/**
	 * 동/읍/면 (예: 역삼동)
	 */
	@Column(name = "district", length = 50)
	private String district;

	/**
	 * 전체 주소 (검색용)
	 */
	@Column(name = "full_address", nullable = false, length = 500)
	private String fullAddress;

	/**
	 * 상세 주소 (건물명, 호수 등)
	 */
	@Column(name = "address_detail", length = 200)
	private String addressDetail;

	/**
	 * 우편번호
	 */
	@Column(name = "postal_code", length = 10)
	private String postalCode;

	/**
	 * 짧은 주소 반환 (도/시 구/군 동/읍/면)
	 *
	 * @return 짧은 주소 문자열
	 */
	public String getShortAddress() {
		StringBuilder sb = new StringBuilder();
		if (province != null && !province.isBlank()) {
			sb.append(province);
		}
		if (city != null && !city.isBlank()) {
			if (sb.length() > 0) sb.append(" ");
			sb.append(city);
		}
		if (district != null && !district.isBlank()) {
			if (sb.length() > 0) sb.append(" ");
			sb.append(district);
		}
		return sb.toString();
	}

	/**
	 * 상세 주소 포함 전체 주소 반환
	 *
	 * @return 상세 주소까지 포함된 전체 주소
	 */
	public String getFullAddressWithDetail() {
		if (addressDetail != null && !addressDetail.isBlank()) {
			return fullAddress + " " + addressDetail;
		}
		return fullAddress;
	}

	/**
	 * 주소가 유효한지 확인
	 *
	 * @return 전체 주소가 있으면 true
	 */
	public boolean isValid() {
		return fullAddress != null && !fullAddress.isBlank();
	}
}
