package com.teambind.placeinfoserver.place.dto.response;

import lombok.*;

import java.util.List;

/**
 * 공간 탐색 응답 DTO
 * 커서 기반 페이징 정보와 검색 결과를 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSearchResponse {
	
	/**
	 * 검색 결과 목록
	 */
	private List<PlaceSearchItem> items;
	
	/**
	 * 다음 페이지 조회를 위한 커서
	 */
	private String nextCursor;
	
	/**
	 * 다음 페이지 존재 여부
	 */
	private Boolean hasNext;
	
	/**
	 * 현재 페이지 항목 수
	 */
	private Integer count;
	
	/**
	 * 검색된 전체 항목 수 (선택적)
	 */
	private Long totalCount;
	
	/**
	 * 검색 메타데이터
	 */
	private SearchMetadata metadata;
	
	/**
	 * 빈 응답 생성
	 */
	public static PlaceSearchResponse empty() {
		return PlaceSearchResponse.builder()
				.items(List.of())
				.hasNext(false)
				.count(0)
				.build();
	}
	
	/**
	 * 개별 검색 결과 항목
	 */
	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PlaceSearchItem {
		private String id;
		private String placeName;
		private String description;
		private String category;
		private String placeType;
		
		// 위치 정보
		private String fullAddress;
		private Double latitude;
		private Double longitude;
		private Double distance; // 위치 기반 검색 시 거리 (미터)
		
		// 평가 정보
		private Double ratingAverage;
		private Integer reviewCount;
		
		// 편의 정보
		private Boolean parkingAvailable;
		private String parkingType;
		
		// 이미지
		private String thumbnailUrl;
		
		// 키워드
		private List<String> keywords;
		
		// 연락처
		private String contact;
		
		// 상태
		private Boolean isActive;
		private String approvalStatus;
		private String registrationStatus;
		
		// Room 정보
		private Integer roomCount; // 룸 개수
		private List<Long> roomIds; // 룸 ID 목록
		
		/**
		 * 거리를 킬로미터로 변환하여 반환
		 */
		public Double getDistanceInKm() {
			return distance != null ? distance / 1000.0 : null;
		}
		
		/**
		 * 거리를 포맷팅하여 반환
		 */
		public String getFormattedDistance() {
			if (distance == null) return null;
			
			if (distance < 1000) {
				return String.format("%.0fm", distance);
			} else {
				return String.format("%.1fkm", distance / 1000.0);
			}
		}
	}
	
	/**
	 * 검색 메타데이터
	 */
	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SearchMetadata {
		private Long searchTime; // 검색 소요 시간 (밀리초)
		private String sortBy;   // 정렬 기준
		private String sortDirection; // 정렬 방향
		
		// 위치 기반 검색 정보
		private Double centerLat;
		private Double centerLng;
		private Integer radiusInMeters;
		
		// 적용된 필터
		private String appliedFilters;
	}
}
