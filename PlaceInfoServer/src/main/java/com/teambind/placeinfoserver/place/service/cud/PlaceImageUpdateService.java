package com.teambind.placeinfoserver.place.service.cud;

import com.teambind.placeinfoserver.place.domain.entity.PlaceImage;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.events.event.ImagesChangeEventWrapper;
import com.teambind.placeinfoserver.place.events.event.SequentialImageChangeEvent;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.service.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceImageUpdateService {
	
	private final PlaceInfoRepository placeInfoRepository;
	private final PlaceMapper placeMapper;
	
	
	public String updateImage(ImagesChangeEventWrapper event) {
		PlaceInfo placeInfo = placeInfoRepository.findById(event.getReferenceId()).orElseThrow();
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
		placeInfoRepository.save(placeInfo);
		return placeInfo.getId();
	}
}
