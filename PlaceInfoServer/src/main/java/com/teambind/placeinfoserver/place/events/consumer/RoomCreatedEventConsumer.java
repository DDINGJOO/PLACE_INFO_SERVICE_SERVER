package com.teambind.placeinfoserver.place.events.consumer;

import com.teambind.placeinfoserver.place.common.util.json.JsonUtil;
import com.teambind.placeinfoserver.place.events.event.RoomCreatedEvent;
import com.teambind.placeinfoserver.place.service.command.RoomCreateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Room 생성 이벤트 컨슈머
 * room-created 토픽을 구독하여 Room 생성 이벤트를 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoomCreatedEventConsumer {
	
	private final RoomCreateService roomCreateService;
	private final JsonUtil jsonUtil;
	
	@KafkaListener(topics = "room-created", groupId = "place-consumer-group")
	public void roomCreated(String message) {
		try {
			log.info("Received room-created event: {}", message);
			RoomCreatedEvent event = jsonUtil.fromJson(message, RoomCreatedEvent.class);
			roomCreateService.createRoom(event.getRoomId(), event.getPlaceId());
			log.info("Successfully processed room-created event: roomId={}, placeId={}",
					event.getRoomId(), event.getPlaceId());
		} catch (Exception e) {
			log.error("Failed to process room-created event: {}", message, e);
			// DLQ 전송이나 재시도 로직 추가 가능
		}
	}
}
