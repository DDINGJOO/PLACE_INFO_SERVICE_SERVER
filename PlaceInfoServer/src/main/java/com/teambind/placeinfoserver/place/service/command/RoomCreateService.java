package com.teambind.placeinfoserver.place.service.command;

import com.teambind.placeinfoserver.place.domain.entity.Room;
import com.teambind.placeinfoserver.place.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Room 생성 서비스
 * 외부 이벤트로부터 Room 정보를 받아 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomCreateService {
	
	private final RoomRepository roomRepository;
	
	/**
	 * Room 생성
	 *
	 * @param roomId  Room ID (외부 서비스에서 생성된 ID)
	 * @param placeId PlaceInfo ID
	 */
	@Transactional
	public void createRoom(Long roomId, Long placeId) {
		// 이미 존재하는 Room인지 확인
		if (roomRepository.findByRoomId(roomId).isPresent()) {
			log.warn("Room already exists: roomId={}, placeId={}", roomId, placeId);
			return;
		}
		
		Room room = Room.builder()
				.roomId(roomId)
				.placeId(placeId)
				.isActive(true)
				.build();
		
		roomRepository.save(room);
		log.info("Room created successfully: roomId={}, placeId={}", roomId, placeId);
	}
}
