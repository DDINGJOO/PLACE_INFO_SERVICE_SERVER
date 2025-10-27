package com.teambind.placeinfoserver.place.entity;

/**
 * 주차 타입 Enum
 */
public enum ParkingType {
	FREE("무료"),
	PAID("유료");
	
	private final String description;
	
	ParkingType(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
