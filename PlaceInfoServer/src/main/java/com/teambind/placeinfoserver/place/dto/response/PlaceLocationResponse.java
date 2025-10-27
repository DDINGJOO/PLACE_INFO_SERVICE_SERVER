package com.teambind.placeinfoserver.place.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceLocationResponse {
	
	private AddressResponse address;
	private Double latitude;
	private Double longitude;
	private String locationGuide;
}
