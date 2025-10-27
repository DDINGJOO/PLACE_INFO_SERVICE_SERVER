package com.teambind.placeinfoserver.place.dto.response;

import com.teambind.placeinfoserver.place.domain.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceInfoSummaryResponse {
	
	private String id;
	private String placeName;
	private String category;
	private String placeType;
	private String thumbnailUrl;
	
	private String shortAddress;
	private Boolean parkingAvailable;
	
	private Double ratingAverage;
	private Integer reviewCount;
	private ApprovalStatus approvalStatus;
	private Boolean isActive;
}
