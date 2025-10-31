package com.teambind.placeinfoserver.place.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 위치 기반 검색 요청 DTO
 * 특정 좌표를 중심으로 반경 내 장소를 검색하기 위한 요청 객체
 * JSR-303 Bean Validation을 통한 검색 파라미터 검증
 */
@Getter
@Setter
public class LocationSearchRequest {
	
	@NotNull(message = "위도는 필수입니다")
	@DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
	@DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
	private Double latitude;
	
	@NotNull(message = "경도는 필수입니다")
	@DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
	@DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
	private Double longitude;
	
	@Min(value = 100, message = "검색 반경은 최소 100m 이상이어야 합니다")
	@Max(value = 50000, message = "검색 반경은 최대 50km를 초과할 수 없습니다")
	private Integer radius = 5000; // 기본값 5km
	
	@Size(max = 100, message = "키워드는 100자를 초과할 수 없습니다")
	private String keyword;
	
	@Size(max = 20, message = "키워드 ID는 최대 20개까지 선택할 수 있습니다")
	private List<Long> keywordIds;
	
	private Boolean parkingAvailable;
	
	private String cursor;
	
	@Min(value = 1, message = "페이지 크기는 최소 1 이상이어야 합니다")
	@Max(value = 100, message = "페이지 크기는 최대 100을 초과할 수 없습니다")
	private Integer size = 20; // 기본값 20개
	
	/**
	 * 반경 값 반환 (null인 경우 기본값 5000m)
	 */
	public Integer getRadius() {
		return radius != null ? radius : 5000;
	}
	
	/**
	 * 페이지 크기 반환 (null인 경우 기본값 20)
	 */
	public Integer getSize() {
		return size != null ? size : 20;
	}
}
