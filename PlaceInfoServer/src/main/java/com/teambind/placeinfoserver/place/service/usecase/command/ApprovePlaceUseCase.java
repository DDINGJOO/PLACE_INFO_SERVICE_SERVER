package com.teambind.placeinfoserver.place.service.usecase.command;

import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.service.usecase.common.IdParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 업체 승인 UseCase
 * SRP: 업체 승인만을 담당
 */
@Service
@RequiredArgsConstructor
public class ApprovePlaceUseCase {

	private final PlaceInfoRepository placeInfoRepository;

	/**
	 * 업체 승인
	 *
	 * @param placeId 업체 ID (String - API 통신용)
	 * @return 업체 ID (String - API 응답용)
	 */
	@Transactional
	public String execute(String placeId) {
		PlaceInfo placeInfo = placeInfoRepository.findById(IdParser.parsePlaceId(placeId))
				.orElseThrow(() -> new PlaceNotFoundException());

		placeInfo.approve();
		return String.valueOf(placeInfo.getId());
	}
}
