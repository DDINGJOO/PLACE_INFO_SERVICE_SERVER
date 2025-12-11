package com.teambind.placeinfoserver.place.events.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Room 생성 이벤트
 * 외부 서비스에서 Room이 생성되었을 때 발행되는 이벤트
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomCreatedEvent extends Event {
	private Long roomId;
	private Long placeId;
	
	public RoomCreatedEvent(Long roomId, Long placeId) {
		super("room-created");
		this.roomId = roomId;
		this.placeId = placeId;
	}
	
	/**
	 * Jackson 역직렬화용 setter - String을 받아서 Long으로 변환
	 */
	public void setRoomId(String roomId) {
		this.roomId = roomId != null ? Long.parseLong(roomId) : null;
	}
	
	/**
	 * Jackson 역직렬화용 setter - String을 받아서 Long으로 변환
	 */
	public void setPlaceId(String placeId) {
		this.placeId = placeId != null ? Long.parseLong(placeId) : null;
	}
}
