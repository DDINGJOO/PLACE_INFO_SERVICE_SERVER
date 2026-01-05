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
 * 업체 삭제 UseCase (소프트 삭제)
 * SRP: 업체 삭제만을 담당
 */
@Service
@RequiredArgsConstructor
public class DeletePlaceUseCase {

	private final PlaceInfoRepository placeInfoRepository;

	/**
	 * 업체 삭제 (소프트 삭제) - 소유자 전용
	 *
	 * @param placeId 업체 ID (String - API 통신용)
	 * @param userId  요청한 사용자 ID
	 */
	@Transactional
	public void execute(String placeId, String userId) {
		PlaceInfo placeInfo = placeInfoRepository.findById(IdParser.parsePlaceId(placeId))
				.orElseThrow(() -> new PlaceNotFoundException());

		validateOwnership(placeInfo, userId);

		placeInfo.softDelete(userId);
	}

	/**
	 * 업체 삭제 (소프트 삭제) - 관리자 전용
	 * 소유권 검증 없이 삭제
	 *
	 * @param placeId   업체 ID (String - API 통신용)
	 * @param deletedBy 삭제한 관리자 ID
	 */
	@Transactional
	public void executeAsAdmin(String placeId, String deletedBy) {
		PlaceInfo placeInfo = placeInfoRepository.findById(IdParser.parsePlaceId(placeId))
				.orElseThrow(() -> new PlaceNotFoundException());

		placeInfo.softDelete(deletedBy);
	}

	private void validateOwnership(PlaceInfo placeInfo, String userId) {
		if (!placeInfo.getUserId().equals(userId)) {
			throw ForbiddenException.notOwner();
		}
	}
}
