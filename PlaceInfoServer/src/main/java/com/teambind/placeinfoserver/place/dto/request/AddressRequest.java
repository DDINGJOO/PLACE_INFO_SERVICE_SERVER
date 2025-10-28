package com.teambind.placeinfoserver.place.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 주소 요청 DTO
 * JSR-303 Bean Validation을 통한 주소 필드 검증
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

	@NotBlank(message = "시/도는 필수입니다")
	@Size(max = 50, message = "시/도는 50자를 초과할 수 없습니다")
	private String province;

	@Size(max = 50, message = "시/군/구는 50자를 초과할 수 없습니다")
	private String city;

	@Size(max = 50, message = "동/읍/면은 50자를 초과할 수 없습니다")
	private String district;

	@NotBlank(message = "전체 주소는 필수입니다")
	@Size(max = 200, message = "전체 주소는 200자를 초과할 수 없습니다")
	private String fullAddress;

	@Size(max = 200, message = "상세 주소는 200자를 초과할 수 없습니다")
	private String addressDetail;

	@Pattern(regexp = "^\\d{5}$|^\\d{6}$|^$", message = "우편번호는 5자리 또는 6자리 숫자여야 합니다")
	private String postalCode;
}
