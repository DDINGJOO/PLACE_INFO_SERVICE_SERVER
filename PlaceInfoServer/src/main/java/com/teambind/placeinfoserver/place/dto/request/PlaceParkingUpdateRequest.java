package com.teambind.placeinfoserver.place.dto.request;

import com.teambind.placeinfoserver.place.domain.enums.ParkingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceParkingUpdateRequest {
	private Boolean available;
	private ParkingType parkingType;
	private String description;
	
}
