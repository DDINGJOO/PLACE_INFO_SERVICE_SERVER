package com.teambind.placeinfoserver.place.service.usecase.command;

import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.dto.request.PlaceUpdateRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.service.mapper.PlaceMapper;
import com.teambind.placeinfoserver.place.service.usecase.common.IdParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 업체 정보 수정 UseCase
 * SRP: 업체 정보 수정만을 담당
 */
@Service
@RequiredArgsConstructor
public class UpdatePlaceUseCase {
	
	private final PlaceInfoRepository placeInfoRepository;
	private final PlaceMapper placeMapper;
	
	/**
	 * 업체 정보 수정
	 *
	 * @param placeId 업체 ID (String - API 통신용)
	 * @param request 수정 요청 DTO
	 * @return 수정된 업체 정보
	 */
	@Transactional
	public PlaceInfoResponse execute(String placeId, PlaceUpdateRequest request) {
		// 업체 조회 (String → Long 변환)
		PlaceInfo placeInfo = placeInfoRepository.findById(IdParser.parsePlaceId(placeId))
				.orElseThrow(PlaceNotFoundException::new);
		
		// 업데이트 (Mapper의 updateEntity 사용)
		placeMapper.updateEntity(placeInfo, request);
		
		// @Transactional이므로 자동으로 변경사항 반영 (더티 체킹)
		// Entity -> Response DTO 변환
		return placeMapper.toResponse(placeInfo);
	}
}
