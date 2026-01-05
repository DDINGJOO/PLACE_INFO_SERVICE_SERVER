package com.teambind.placeinfoserver.place.service.usecase.command;

import com.teambind.placeinfoserver.place.common.exception.application.ForbiddenException;
import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.service.usecase.common.IdParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 업체 활성화 UseCase
 * SRP: 업체 활성화만을 담당
 */
@Service
@RequiredArgsConstructor
public class ActivatePlaceUseCase {

	private final PlaceInfoRepository placeInfoRepository;

	/**
	 * 업체 활성화
	 *
	 * @param placeId 업체 ID (String - API 통신용)
	 * @param userId  요청 사용자 ID
	 * @return 업체 ID (String - API 응답용)
	 */
	@Transactional
	public String execute(String placeId, String userId) {
		PlaceInfo placeInfo = placeInfoRepository.findById(IdParser.parsePlaceId(placeId))
				.orElseThrow(PlaceNotFoundException::new);

		validateOwnership(placeInfo, userId);
		placeInfo.activate();

		return String.valueOf(placeInfo.getId());
	}

	private void validateOwnership(PlaceInfo placeInfo, String userId) {
		if (!placeInfo.getUserId().equals(userId)) {
			throw ForbiddenException.notOwner();
		}
	}
}
