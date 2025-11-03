package com.teambind.placeinfoserver.place.service.usecase.query;

import com.teambind.placeinfoserver.place.dto.request.PlaceSearchRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자별 업체 조회 UseCase
 * SRP: 특정 사용자가 등록한 업체 목록 조회만을 담당
 *
 * Note: PlaceSearchRequest에 userId 필드 추가 필요
 * 현재는 SearchPlacesUseCase를 통한 통합 검색으로 처리 가능
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPlacesByUserUseCase {

	private final SearchPlacesUseCase searchPlacesUseCase;

	/**
	 * 사용자별 업체 조회
	 * TODO: PlaceSearchRequest에 userId 필드 추가 후 개선 필요
	 *
	 * @param userId 사용자 ID (현재 미사용 - 추후 구현 예정)
	 * @param cursor 페이징 커서
	 * @param size   페이지 크기
	 * @return 업체 목록
	 */
	public PlaceSearchResponse execute(String userId, String cursor, Integer size) {
		log.warn("GetPlacesByUserUseCase: userId 필터링 미구현 - PlaceSearchRequest 개선 필요");

		PlaceSearchRequest request = PlaceSearchRequest.builder()
				.cursor(cursor)
				.size(size != null ? size : 20)
				.sortBy(PlaceSearchRequest.SortBy.CREATED_AT)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.build();

		return searchPlacesUseCase.execute(request);
	}
}
