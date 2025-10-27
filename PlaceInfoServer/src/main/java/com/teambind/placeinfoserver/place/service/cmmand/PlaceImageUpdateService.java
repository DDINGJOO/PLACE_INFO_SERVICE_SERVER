package com.teambind.placeinfoserver.place.service.cmmand;

import com.teambind.placeinfoserver.place.common.exception.CustomException;
import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import com.teambind.placeinfoserver.place.domain.entity.PlaceImage;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.events.event.ImagesChangeEventWrapper;
import com.teambind.placeinfoserver.place.events.event.SequentialImageChangeEvent;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.service.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceImageUpdateService {
	
	private final PlaceInfoRepository placeInfoRepository;
	private final PlaceMapper placeMapper;
	
	
	/**
	 * 업체 이미지 업데이트
	 * 이벤트를 받아 기존 이미지를 삭제하고 새 이미지들을 순서대로 추가
	 *
	 * @param event 이미지 변경 이벤트
	 * @return 업데이트된 업체 ID
	 */
	@Transactional
	public String updateImage(ImagesChangeEventWrapper event) {
		PlaceInfo placeInfo = placeInfoRepository.findById(event.getReferenceId())
				.orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

		// 기존 이미지 삭제
		placeInfo.removeAllImage();

		// 순서에 맞춰서 이미지 세팅
		if (event.getImages() == null || event.getImages().isEmpty()) {
			return placeInfo.getId();
		}
		
		for (SequentialImageChangeEvent imageChangeEvent : event.getImages()) {
			placeInfo.addImage(
					PlaceImage.builder()
							.imageUrl(imageChangeEvent.getImageUrl())
							.build()
			);
		}
		
		// @Transactional이므로 자동으로 변경사항 반영 (더티 체킹)
		return placeInfo.getId();
	}
}
