package com.teambind.placeinfoserver.place.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	private PlaceLocationRequest location;
	private PlaceParkingUpdateRequest parking;
}
