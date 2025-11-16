package com.teambind.placeinfoserver.place.events.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Room 생성 이벤트
 * 외부 서비스에서 Room이 생성되었을 때 발행되는 이벤트
 */
@Setter
@Getter
@NoArgsConstructor
public class RoomCreatedEvent extends Event {
	private Long roomId;
	private Long placeId;

	public RoomCreatedEvent(Long roomId, Long placeId) {
		super("room-created");
		this.roomId = roomId;
		this.placeId = placeId;
	}
}