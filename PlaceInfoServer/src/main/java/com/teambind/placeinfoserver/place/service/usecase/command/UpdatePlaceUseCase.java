package com.teambind.placeinfoserver.place.service.usecase.command;

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
	 * @param request 수정 요청 DTO
	 * @return 수정된 업체 정보
	 */
	@Transactional
	public PlaceInfoResponse execute(String placeId, PlaceUpdateRequest request) {
		PlaceInfo placeInfo = placeInfoRepository.findById(IdParser.parsePlaceId(placeId))
				.orElseThrow(PlaceNotFoundException::new);
		
		// 기본 정보, 연락처, 주차 정보 업데이트
		placeMapper.updateEntity(placeInfo, request);
		
		// 키워드 업데이트
		updateKeywords(placeInfo, request.getKeywordIds());
		
		return placeMapper.toResponse(placeInfo);
	}
	
	/**
	 * 키워드 업데이트
	 * 기존 키워드를 모두 제거하고 새로운 키워드로 교체
	 */
	private void updateKeywords(PlaceInfo placeInfo, List<Long> keywordIds) {
		if (keywordIds == null) {
			return;
		}
		
		if (keywordIds.size() > MAX_KEYWORDS) {
			throw new IllegalArgumentException("키워드는 최대 " + MAX_KEYWORDS + "개까지만 선택 가능합니다.");
		}
		
		// 기존 키워드 모두 제거
		placeInfo.getKeywords().clear();
		
		if (keywordIds.isEmpty()) {
			return;
		}
		
		// 새 키워드 조회 및 설정
		List<Keyword> keywords = keywordRepository.findAllById(keywordIds);
		
		if (keywords.size() != keywordIds.size()) {
			throw new IllegalArgumentException("유효하지 않은 키워드 ID가 포함되어 있습니다.");
		}
		
		Set<Keyword> keywordSet = new HashSet<>(keywords);
		placeInfo.setKeywords(keywordSet);
	}
}
