package com.teambind.placeinfoserver.place.events.consumer;

import com.teambind.placeinfoserver.place.common.util.json.JsonUtil;
import com.teambind.placeinfoserver.place.events.event.ImagesChangeEventWrapper;
import com.teambind.placeinfoserver.place.service.cud.PlaceImageUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class eventConsumer {
	private final PlaceImageUpdateService placeImageUpdateService;
	private JsonUtil jsonUtil;
	
	
	@KafkaListener(topics = "place-image-changed", groupId = "place-consumer-group")
	public void placeImageChanged(String message) {
		try {
			ImagesChangeEventWrapper request = jsonUtil.fromJson(message, ImagesChangeEventWrapper.class);
			placeImageUpdateService.updateImage(request);
		} catch (Exception e) {
			// 역직렬화 실패 또는 처리 중 오류 발생 시 로깅/대응
			log.error("Failed to deserialize or process profile-create-request message: {}", message, e);
			// 필요하면 DLQ 전송이나 재시도 로직 추가
		}
	}
	
}
