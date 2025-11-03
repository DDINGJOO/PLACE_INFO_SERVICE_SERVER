package com.teambind.placeinfoserver.place.service.usecase.query;

import com.teambind.placeinfoserver.place.dto.request.PlaceSearchRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceSearchResponse;
import com.teambind.placeinfoserver.place.repository.PlaceAdvancedSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 업체 검색 UseCase
 * SRP: 업체 검색만을 담당
 * - 위치 기반 검색
 * - 키워드 검색
 * - 커서 기반 페이징
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchPlacesUseCase {

	private final PlaceAdvancedSearchRepository searchRepository;

	/**
	 * 통합 검색
	 * 모든 검색 조건을 통합하여 처리
	 *
	 * @param request 검색 요청
	 * @return 검색 결과
	 */
	public PlaceSearchResponse execute(PlaceSearchRequest request) {
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
	 */
	private PlaceSearchResponse searchWithCursor(PlaceSearchRequest request) {
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
	 */
	private PlaceSearchResponse searchByLocation(PlaceSearchRequest request) {
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
	 */
	private PlaceSearchResponse searchByKeywords(PlaceSearchRequest request) {
		if (request.getKeywordIds() == null || request.getKeywordIds().isEmpty()) {
			return PlaceSearchResponse.empty();
		}

		log.info("키워드 검색: {} 개 키워드", request.getKeywordIds().size());

		return searchRepository.searchByKeywords(request);
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
}
