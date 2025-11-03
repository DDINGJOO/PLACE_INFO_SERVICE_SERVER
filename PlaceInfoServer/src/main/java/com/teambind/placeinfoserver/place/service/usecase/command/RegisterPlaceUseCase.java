package com.teambind.placeinfoserver.place.service.usecase.command;

import com.teambind.placeinfoserver.place.common.util.generator.PrimaryKeyGenerator;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.dto.request.PlaceRegisterRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.service.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 업체 등록 UseCase
 * SRP: 업체 등록만을 담당
 */
@Service
@RequiredArgsConstructor
public class RegisterPlaceUseCase {

	private final PlaceInfoRepository placeInfoRepository;
	private final PrimaryKeyGenerator pkeyGenerator;
	private final PlaceMapper placeMapper;

	/**
	 * 업체 등록
	 *
	 * @param request 등록 요청 DTO
	 * @return 등록된 업체 정보
	 */
	@Transactional
	public PlaceInfoResponse execute(PlaceRegisterRequest request) {
		// ID 생성 (Long 타입)
		Long generatedId = pkeyGenerator.generateLongKey();

		// DTO -> Entity 변환
		PlaceInfo placeInfo = placeMapper.toEntity(request, generatedId);

		// 저장
		PlaceInfo savedPlace = placeInfoRepository.save(placeInfo);

		// Entity -> Response DTO 변환
		return placeMapper.toResponse(savedPlace);
	}
}
