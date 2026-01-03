package com.teambind.placeinfoserver.place.service.usecase.query;

import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.service.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자별 업체 조회 UseCase
 * SRP: 특정 사용자가 등록한 업체 목록 조회만을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPlacesByUserUseCase {

	private final PlaceInfoRepository placeInfoRepository;
	private final PlaceMapper placeMapper;

	/**
	 * 사용자별 업체 조회
	 * 삭제되지 않은 모든 공간 조회 (활성/비활성 모두 포함)
	 *
	 * @param userId 사용자 ID
	 * @return 해당 사용자가 등록한 업체 목록
	 */
	public List<PlaceInfoResponse> execute(String userId) {
		log.info("내 공간 조회: userId={}", userId);

		List<PlaceInfo> places = placeInfoRepository.findAllByUserIdWithDetails(userId);

		log.info("내 공간 조회 완료: userId={}, count={}", userId, places.size());

		return placeMapper.toResponseList(places);
	}
}
