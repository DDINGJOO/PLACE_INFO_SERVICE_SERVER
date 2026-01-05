package com.teambind.placeinfoserver.place.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceUpdateRequest {
	
	private String placeName;
	private String description;
	private String category;
	private String placeType;
	
	private PlaceContactRequest contact;
	private PlaceParkingUpdateRequest parking;
	
	/**
	 * 키워드 ID 목록 (최대 10개)
	 */
	private List<Long> keywordIds;
}
