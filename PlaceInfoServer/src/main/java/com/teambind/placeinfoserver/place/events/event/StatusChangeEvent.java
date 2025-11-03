package com.teambind.placeinfoserver.place.events.event;

import com.teambind.placeinfoserver.place.domain.enums.PlaceOperationType;

public class StatusChangeEvent extends Event {
	private final Long placeInfoId;
	private final PlaceOperationType status;
	
	public StatusChangeEvent(Long placeInfoId, PlaceOperationType status) {
		super("place-status-change-event");
		this.placeInfoId = placeInfoId;
		this.status = status;
	}
}
