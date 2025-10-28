package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.dto.cursor.PlaceSearchCursor;
import com.teambind.placeinfoserver.place.dto.request.PlaceSearchRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceSearchResponse;

/**
 * 고급 공간 검색 리포지토리 인터페이스
 * 커서 기반 페이징과 복잡한 검색 조건을 지원
 */
public interface PlaceAdvancedSearchRepository {

	/**
	 * 커서 기반 검색
	 *
	 * @param request 검색 요청 (조건, 정렬, 커서 포함)
	 * @return 검색 결과와 다음 페이지 커서
	 */
	PlaceSearchResponse searchWithCursor(PlaceSearchRequest request);

	/**
	 * 위치 기반 검색 (PostGIS 활용)
	 *
	 * @param request 위치 정보를 포함한 검색 요청
	 * @return 거리순으로 정렬된 검색 결과
	 */
	PlaceSearchResponse searchByLocation(PlaceSearchRequest request);

	/**
	 * 키워드 기반 검색
	 *
	 * @param request 키워드를 포함한 검색 요청
	 * @return 키워드 매칭 검색 결과
	 */
	PlaceSearchResponse searchByKeywords(PlaceSearchRequest request);

	/**
	 * 검색 결과 개수 조회 (선택적)
	 *
	 * @param request 검색 요청
	 * @return 총 결과 수
	 */
	Long countSearchResults(PlaceSearchRequest request);
}