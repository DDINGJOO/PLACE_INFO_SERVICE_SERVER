package com.teambind.placeinfoserver.place.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceLocationRequest {
	
	private AddressRequest address;
	private Double latitude;
	private Double longitude;
	private String locationGuide;
}
