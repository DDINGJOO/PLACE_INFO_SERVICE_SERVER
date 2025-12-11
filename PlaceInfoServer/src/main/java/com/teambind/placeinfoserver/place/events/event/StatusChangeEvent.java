package com.teambind.placeinfoserver.place.events.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.teambind.placeinfoserver.place.domain.enums.PlaceOperationType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusChangeEvent extends Event {
	private Long placeInfoId;
	private PlaceOperationType status;
	
	public StatusChangeEvent(Long placeInfoId, PlaceOperationType status) {
		super("place-status-change-event");
		this.placeInfoId = placeInfoId;
		this.status = status;
	}
	
	/**
	 * Jackson 역직렬화용 setter - String을 받아서 Long으로 변환
	 */
	public void setPlaceInfoId(String placeInfoId) {
		this.placeInfoId = placeInfoId != null ? Long.parseLong(placeInfoId) : null;
	}
	
	/**
	 * Jackson 역직렬화용 setter
	 */
	public void setStatus(PlaceOperationType status) {
		this.status = status;
	}
}
