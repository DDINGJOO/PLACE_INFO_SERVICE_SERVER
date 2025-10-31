package com.teambind.placeinfoserver.place.dto.request;

import com.teambind.placeinfoserver.place.domain.vo.Address;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 주소 요청 DTO (표준 형태)
 * DB 저장을 위한 표준화된 주소 정보
 * 다양한 출처(카카오, 네이버, 수동 입력 등)의 주소 데이터를 이 형태로 변환하여 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
	
	@Size(max = 50, message = "시/도는 50자를 초과할 수 없습니다")
	private String province;
	
	@Size(max = 50, message = "시/군/구는 50자를 초과할 수 없습니다")
	private String city;
	
	@Size(max = 50, message = "동/읍/면은 50자를 초과할 수 없습니다")
	private String district;
	
	@Size(max = 500, message = "전체 주소는 500자를 초과할 수 없습니다")
	private String fullAddress;
	
	@Size(max = 200, message = "상세 주소는 200자를 초과할 수 없습니다")
	private String addressDetail;
	
	@Pattern(regexp = "^\\d{5}$|^\\d{6}$|^$", message = "우편번호는 5자리 또는 6자리 숫자여야 합니다")
	private String postalCode;
	
	/**
	 * Address VO로 변환
	 */
	public Address toAddress() {
		return Address.builder()
				.province(province)
				.city(city)
				.district(district)
				.fullAddress(fullAddress)
				.addressDetail(addressDetail)
				.postalCode(postalCode)
				.build();
	}
}
