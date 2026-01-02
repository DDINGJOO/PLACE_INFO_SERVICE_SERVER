package com.teambind.placeinfoserver.place.dto.request;

import lombok.*;

import java.util.List;

/**
 * 공간 탐색 요청 DTO
 * 커서 기반 페이징과 다양한 검색 조건을 지원
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSearchRequest {
	
	// ======== 검색 조건 ========
	/**
	 * 키워드 검색 (장소명, 설명, 카테고리 등)
	 */
	private String keyword;
	
	/**
	 * 장소명 직접 검색
	 */
	private String placeName;
	
	/**
	 * 키워드 ID 목록 (태그 검색)
	 */
	private List<Long> keywordIds;
	
	/**
	 * 주차 가능 여부 필터
	 */
	private Boolean parkingAvailable;
	
	/**
	 * 카테고리 필터
	 */
	private String category;
	
	/**
	 * 장소 타입 필터
	 */
	private String placeType;
	
	// ======== 위치 기반 검색 ========
	/**
	 * 중심점 위도 (위치 기반 검색 시)
	 */
	private Double latitude;
	
	/**
	 * 중심점 경도 (위치 기반 검색 시)
	 */
	private Double longitude;
	
	/**
	 * 검색 반경 (미터 단위, 기본값: 5000m)
	 */
	@Builder.Default
	private Integer radiusInMeters = 5000;
	
	/**
	 * 지역 필터 (시/도)
	 */
	private String province;
	
	/**
	 * 지역 필터 (시/군/구)
	 */
	private String city;
	
	/**
	 * 지역 필터 (동/읍/면)
	 */
	private String district;
	
	// ======== 정렬 조건 ========
	/**
	 * 정렬 기준
	 */
	@Builder.Default
	private SortBy sortBy = SortBy.DISTANCE;
	
	/**
	 * 정렬 방향
	 */
	@Builder.Default
	private SortDirection sortDirection = SortDirection.ASC;
	
	// ======== 커서 기반 페이징 ========
	/**
	 * 커서 값 (다음 페이지 조회 시 사용)
	 */
	private String cursor;
	
	/**
	 * 페이지 크기 (기본값: 20, 최대: 100)
	 */
	@Builder.Default
	private Integer size = 20;
	
	// ======== 기본 필터 (항상 적용) ========
	/**
	 * 활성화 상태 필터 (기본값: true)
	 */
	@Builder.Default
	private Boolean isActive = true;
	
	/**
	 * 승인 상태 필터 (기본값: APPROVED)
	 */
	@Builder.Default
	private String approvalStatus = "APPROVED";

	/**
	 * 등록 상태 필터 (null이면 전체 조회)
	 * REGISTERED: 등록 업체만, UNREGISTERED: 미등록 업체만
	 */
	private String registrationStatus;

	/**
	 * 위치 기반 검색 여부 확인
	 */
	public boolean isLocationBasedSearch() {
		return latitude != null && longitude != null;
	}
	
	/**
	 * 지역 필터 검색 여부 확인
	 */
	public boolean hasRegionFilter() {
		return province != null || city != null || district != null;
	}
	
	/**
	 * 유효성 검증
	 */
	public void validate() {
		if (size != null) {
			if (size < 1) size = 1;
			if (size > 100) size = 100;
		}
		
		if (radiusInMeters != null) {
			if (radiusInMeters < 100) radiusInMeters = 100;
			if (radiusInMeters > 50000) radiusInMeters = 50000; // 최대 50km
		}
		
		// 위치 기반 검색 시 거리순 정렬이 아니면 경고
		if (isLocationBasedSearch() && sortBy != SortBy.DISTANCE) {
			// 로그 경고만 남기고 진행
		}
	}
	
	/**
	 * 정렬 기준 열거형
	 */
	public enum SortBy {
		DISTANCE("distance"),          // 거리순 (위치 검색 시)
		RATING("rating_average"),       // 평점순
		REVIEW_COUNT("review_count"),   // 리뷰 수순
		CREATED_AT("created_at"),       // 최신순
		PLACE_NAME("place_name");       // 이름순
		
		private final String field;
		
		SortBy(String field) {
			this.field = field;
		}
		
		public String getField() {
			return field;
		}
	}
	
	/**
	 * 정렬 방향 열거형
	 */
	public enum SortDirection {
		ASC, DESC
	}
}
