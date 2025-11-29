package com.teambind.placeinfoserver.place.service.command;

import com.teambind.placeinfoserver.place.common.exception.application.InvalidRequestException;
import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
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
	 * String ID를 Long으로 안전하게 변환
	 *
	 * @param placeId String 형태의 업체 ID
	 * @return Long 형태의 업체 ID
	 * @throws CustomException ID 형식이 잘못된 경우
	 */
	private Long parseId(String placeId) {
		try {
			return Long.parseLong(placeId);
		} catch (NumberFormatException e) {
			throw InvalidRequestException.invalidFormat("placeId");
		}
	}
	
	
	/**
	 * 업체 이미지 업데이트
	 * 이벤트를 받아 기존 이미지를 삭제하고 새 이미지들을 순서대로 추가
	 *
	 * @param event 이미지 변경 이벤트 (referenceId는 String - API 통신용)
	 * @return 업데이트된 업체 ID (String - API 응답용)
	 */
	@Transactional
	public String updateImage(ImagesChangeEventWrapper event) {
		PlaceInfo placeInfo = placeInfoRepository.findById(parseId(event.getReferenceId()))
				.orElseThrow(() -> new PlaceNotFoundException());

		// 기존 이미지 삭제
		placeInfo.removeAllImage();

		// 순서에 맞춰서 이미지 세팅
		if (event.getImages() == null || event.getImages().isEmpty()) {
			log.info("No images to update for placeId: {}", event.getReferenceId());
			return String.valueOf(placeInfo.getId());  // Long → String 변환
		}

		for (SequentialImageChangeEvent imageEvent : event.getImages()) {
			// 이미지 쌍 검증
			if (!validateImagePair(imageEvent, event.getReferenceId())) {
				continue; // 유효하지 않은 이미지는 건너뛰기
			}

			String imageId = imageEvent.getImageId();
			String imageUrl = imageEvent.getImageUrl();
			Integer eventSequence = imageEvent.getSequence();

			// sequence 활용하여 이미지 추가
			if (eventSequence != null && eventSequence > 0) {
				// sequence가 있으면 지정된 순서로
				placeInfo.addImageWithSequence(imageId, imageUrl, eventSequence.longValue());
				log.debug("Added image with sequence for placeId: {}, imageId: {}, sequence: {}",
						event.getReferenceId(), imageId, eventSequence);
			} else {
				// sequence가 없으면 자동 순서로
				placeInfo.addImage(imageId, imageUrl);
				log.debug("Added image with auto sequence for placeId: {}, imageId: {}",
						event.getReferenceId(), imageId);
			}
		}

		log.info("Successfully updated {} images for placeId: {}",
				placeInfo.getImages().size(), event.getReferenceId());

		// @Transactional이므로 자동으로 변경사항 반영 (더티 체킹)
		return String.valueOf(placeInfo.getId());  // Long → String 변환
	}

	/**
	 * 이미지 쌍 검증
	 * imageId와 imageUrl이 모두 유효한지 확인
	 *
	 * @param imageEvent 이미지 이벤트
	 * @param placeId    업체 ID (로깅용)
	 * @return 유효한 경우 true, 그렇지 않으면 false
	 */
	private boolean validateImagePair(SequentialImageChangeEvent imageEvent, String placeId) {
		if (imageEvent == null) {
			log.warn("Null image event for placeId: {}", placeId);
			return false;
		}

		String imageId = imageEvent.getImageId();
		String imageUrl = imageEvent.getImageUrl();

		// imageId와 imageUrl 둘 다 필수
		if (imageId == null || imageId.trim().isEmpty()) {
			log.warn("Missing imageId in image pair for placeId: {}, imageUrl: {}",
					placeId, imageUrl);
			return false;
		}

		if (imageUrl == null || imageUrl.trim().isEmpty()) {
			log.warn("Missing imageUrl in image pair for placeId: {}, imageId: {}",
					placeId, imageId);
			return false;
		}

		// URL 기본 검증 완료 (null과 빈 문자열은 이미 체크됨)
		// 다양한 형식의 이미지 식별자 허용 (http, https, 상대경로, image://, 단순 ID 등)

		log.debug("Valid image pair for placeId: {}, imageId: {}, imageUrl: {}",
				placeId, imageId, imageUrl);
		return true;
	}
}
