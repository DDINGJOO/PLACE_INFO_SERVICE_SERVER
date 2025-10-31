package com.teambind.placeinfoserver.place.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teambind.placeinfoserver.place.domain.enums.AddressSource;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 장소 위치 등록/수정 요청 DTO
 * 프론트엔드에서 외부 API(카카오, 네이버) 응답을 그대로 전달받아 서버에서 파싱
 * JSR-303 Bean Validation을 통한 좌표 검증
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceLocationRequest {
	
	/**
	 * 주소 데이터 출처 (KAKAO, NAVER, MANUAL)
	 */
	@NotNull(message = "주소 출처(from)는 필수입니다")
	private AddressSource from;
	
	/**
	 * 외부 API 응답 원본 데이터 (JSON 객체)
	 * 카카오/네이버 API 응답을 그대로 담음
	 */
	@JsonProperty("addressData")
	private Object addressData;
	
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
