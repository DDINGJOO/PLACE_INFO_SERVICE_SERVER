package com.teambind.placeinfoserver.place.service.usecase.command;

import com.teambind.placeinfoserver.place.common.exception.application.ForbiddenException;
import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.domain.entity.Keyword;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.dto.request.PlaceUpdateRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.repository.KeywordRepository;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.service.mapper.PlaceMapper;
import com.teambind.placeinfoserver.place.service.usecase.common.IdParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 업체 정보 수정 UseCase
 * SRP: 업체 정보 수정만을 담당 (위치 정보 제외)
 */
@Service
@RequiredArgsConstructor
public class UpdatePlaceUseCase {

	private static final int MAX_KEYWORDS = 10;

	private final PlaceInfoRepository placeInfoRepository;
	private final KeywordRepository keywordRepository;
	private final PlaceMapper placeMapper;

	/**
	 * 업체 정보 수정 (위치 정보 제외)
	 *
	 * @param placeId 업체 ID (String - API 통신용)
	 * @param userId  요청 사용자 ID
	 * @param request 수정 요청 DTO
	 * @return 수정된 업체 정보
	 */
	@Transactional
	public PlaceInfoResponse execute(String placeId, String userId, PlaceUpdateRequest request) {
		PlaceInfo placeInfo = placeInfoRepository.findById(IdParser.parsePlaceId(placeId))
				.orElseThrow(PlaceNotFoundException::new);

		validateOwnership(placeInfo, userId);

		placeMapper.updateEntity(placeInfo, request);
		updateKeywords(placeInfo, request.getKeywordIds());

		return placeMapper.toResponse(placeInfo);
	}

	private void validateOwnership(PlaceInfo placeInfo, String userId) {
		if (!placeInfo.getUserId().equals(userId)) {
			throw ForbiddenException.notOwner();
		}
	}

	private void updateKeywords(PlaceInfo placeInfo, List<Long> keywordIds) {
		if (keywordIds == null) {
			return;
		}

		if (keywordIds.size() > MAX_KEYWORDS) {
			throw new IllegalArgumentException("키워드는 최대 " + MAX_KEYWORDS + "개까지만 선택 가능합니다.");
		}

		placeInfo.getKeywords().clear();

		if (keywordIds.isEmpty()) {
			return;
		}

		List<Keyword> keywords = keywordRepository.findAllById(keywordIds);

		if (keywords.size() != keywordIds.size()) {
			throw new IllegalArgumentException("유효하지 않은 키워드 ID가 포함되어 있습니다.");
		}

		Set<Keyword> keywordSet = new HashSet<>(keywords);
		placeInfo.setKeywords(keywordSet);
	}
}
