package com.teambind.placeinfoserver.place.service;

import com.teambind.placeinfoserver.place.dto.request.PlaceSearchRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceSearchResponse;
import com.teambind.placeinfoserver.place.repository.PlaceAdvancedSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공간 탐색 서비스
 * 커서 기반 페이징과 다양한 검색 옵션을 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceAdvancedSearchService {
	
	private final PlaceAdvancedSearchRepository searchRepository;
	
	/**
	 * 통합 검색
	 * 모든 검색 조건을 통합하여 처리
	 *
	 * @param request 검색 요청
	 * @return 검색 결과
	 */
	public PlaceSearchResponse search(PlaceSearchRequest request) {
		log.debug("검색 요청: keyword={}, location=({},{}), sort={}",
				request.getKeyword(),
				request.getLatitude(),
				request.getLongitude(),
				request.getSortBy());
		
		// 유효성 검증
		validateRequest(request);
		
		// 위치 기반 검색인 경우
		if (request.isLocationBasedSearch()) {
			return searchByLocation(request);
		}
		
		// 키워드 검색인 경우
		if (request.getKeywordIds() != null && !request.getKeywordIds().isEmpty()) {
			return searchByKeywords(request);
		}
		
		// 일반 검색
		return searchWithCursor(request);
	}
	
	/**
	 * 커서 기반 일반 검색
	 *
	 * @param request 검색 요청
	 * @return 검색 결과
	 */
	public PlaceSearchResponse searchWithCursor(PlaceSearchRequest request) {
		try {
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			log.info("검색 완료: {} 건 조회, hasNext={}", response.getCount(), response.getHasNext());
			return response;
		} catch (Exception e) {
			log.error("검색 중 오류 발생", e);
			return PlaceSearchResponse.empty();
		}
	}
	
	/**
	 * 위치 기반 검색
	 * PostGIS를 활용한 효율적인 지리 공간 검색
	 *
	 * @param request 위치 정보를 포함한 검색 요청
	 * @return 거리순으로 정렬된 검색 결과
	 */
	@Cacheable(
			value = "placeLocationSearch",
			key = "#request.latitude + ':' + #request.longitude + ':' + #request.radiusInMeters",
			condition = "#request.cursor == null", // 첫 페이지만 캐싱
			unless = "#result.items.isEmpty()"
	)
	public PlaceSearchResponse searchByLocation(PlaceSearchRequest request) {
		if (!request.isLocationBasedSearch()) {
			throw new IllegalArgumentException("위치 정보가 필요합니다");
		}
		
		log.info("위치 기반 검색: ({}, {}) 반경 {}m",
				request.getLatitude(),
				request.getLongitude(),
				request.getRadiusInMeters());
		
		return searchRepository.searchByLocation(request);
	}
	
	/**
	 * 키워드 기반 검색
	 *
	 * @param request 키워드를 포함한 검색 요청
	 * @return 키워드 매칭 검색 결과
	 */
	public PlaceSearchResponse searchByKeywords(PlaceSearchRequest request) {
		if (request.getKeywordIds() == null || request.getKeywordIds().isEmpty()) {
			return PlaceSearchResponse.empty();
		}
		
		log.info("키워드 검색: {} 개 키워드", request.getKeywordIds().size());
		
		return searchRepository.searchByKeywords(request);
	}
	
	/**
	 * 지역 기반 검색
	 * 특정 지역(시/구/동) 내의 장소 검색
	 *
	 * @param province 시/도
	 * @param city     시/군/구
	 * @param district 동/읍/면
	 * @param cursor   페이징 커서
	 * @param size     페이지 크기
	 * @return 검색 결과
	 */
	public PlaceSearchResponse searchByRegion(
			String province,
			String city,
			String district,
			String cursor,
			Integer size
	) {
		PlaceSearchRequest request = PlaceSearchRequest.builder()
				.province(province)
				.city(city)
				.district(district)
				.cursor(cursor)
				.size(size != null ? size : 20)
				.build();
		
		log.info("지역 검색: {}/{}/{}", province, city, district);
		
		return searchWithCursor(request);
	}
	
	/**
	 * 인기 장소 검색
	 * 평점과 리뷰 수를 기준으로 인기 있는 장소 반환
	 *
	 * @param size 결과 개수
	 * @return 인기 장소 목록
	 */
	@Cacheable(
			value = "popularPlaces",
			key = "#size",
			unless = "#result.items.isEmpty()"
	)
	public PlaceSearchResponse getPopularPlaces(Integer size) {
		PlaceSearchRequest request = PlaceSearchRequest.builder()
				.sortBy(PlaceSearchRequest.SortBy.RATING)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.size(size != null ? size : 10)
				.build();
		
		log.info("인기 장소 조회: {} 건", request.getSize());
		
		return searchWithCursor(request);
	}
	
	/**
	 * 최신 등록 장소 검색
	 *
	 * @param size 결과 개수
	 * @return 최신 장소 목록
	 */
	public PlaceSearchResponse getRecentPlaces(Integer size) {
		PlaceSearchRequest request = PlaceSearchRequest.builder()
				.sortBy(PlaceSearchRequest.SortBy.CREATED_AT)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.size(size != null ? size : 10)
				.build();
		
		log.info("최신 장소 조회: {} 건", request.getSize());
		
		return searchWithCursor(request);
	}
	
	/**
	 * 검색 요청 유효성 검증
	 */
	private void validateRequest(PlaceSearchRequest request) {
		// 기본 유효성 검증
		request.validate();
		
		// 위치 기반 검색 시 위도/경도 범위 검증
		if (request.isLocationBasedSearch()) {
			if (request.getLatitude() < -90 || request.getLatitude() > 90) {
				throw new IllegalArgumentException("유효하지 않은 위도입니다");
			}
			if (request.getLongitude() < -180 || request.getLongitude() > 180) {
				throw new IllegalArgumentException("유효하지 않은 경도입니다");
			}
		}
		
		// 페이지 크기 검증
		if (request.getSize() != null && request.getSize() > 100) {
			log.warn("페이지 크기가 100을 초과하여 100으로 조정됩니다");
			request.setSize(100);
		}
	}
	
	/**
	 * 검색 결과 개수 조회
	 * 페이징 UI를 위한 전체 결과 수 반환
	 *
	 * @param request 검색 요청
	 * @return 총 검색 결과 수
	 */
	public Long countSearchResults(PlaceSearchRequest request) {
		try {
			return searchRepository.countSearchResults(request);
		} catch (Exception e) {
			log.error("검색 결과 개수 조회 중 오류", e);
			return 0L;
		}
	}
}
