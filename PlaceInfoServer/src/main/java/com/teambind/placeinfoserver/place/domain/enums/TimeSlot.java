package com.teambind.placeinfoserver.place.domain.enums;

import lombok.Getter;

/**
 * 시간대 Enum
 * 룸 예약 시간대를 나타냄
 */
@Getter
public enum TimeSlot {
	MORNING("오전", "06:00-12:00"),
	AFTERNOON("오후", "12:00-18:00"),
	EVENING("저녁", "18:00-22:00"),
	NIGHT("심야", "22:00-06:00"),
	ALLDAY("종일", "00:00-24:00");

	private final String displayName;
	private final String timeRange;

	TimeSlot(String displayName, String timeRange) {
		this.displayName = displayName;
		this.timeRange = timeRange;
	}
}
