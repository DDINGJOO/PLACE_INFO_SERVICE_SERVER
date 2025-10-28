package com.teambind.placeinfoserver.place.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 장소 위치 요청 DTO
 * JSR-303 Bean Validation을 통한 좌표 및 주소 검증
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceLocationRequest {

	@NotNull(message = "주소 정보는 필수입니다")
	@Valid
	private AddressRequest address;

	@NotNull(message = "위도는 필수입니다")
	@DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
	@DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
	private Double latitude;

	@NotNull(message = "경도는 필수입니다")
	@DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
	@DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
	private Double longitude;

	@Size(max = 500, message = "위치 안내는 500자를 초과할 수 없습니다")
	private String locationGuide;
}
