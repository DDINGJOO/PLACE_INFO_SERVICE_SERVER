package com.teambind.placeinfoserver.place.dto.response;

import com.teambind.placeinfoserver.place.domain.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceInfoResponse {
	
	private String id;
	private String userId;
	private String placeName;
	private String description;
	private String category;
	private String placeType;
	
	private PlaceContactResponse contact;
	private PlaceLocationResponse location;
	private PlaceParkingResponse parking;
	
	private List<String> imageUrls;
	private List<KeywordResponse> keywords;
	
	private Boolean isActive;
	private ApprovalStatus approvalStatus;
	private Double ratingAverage;
	private Integer reviewCount;

	// Room 정보
	private Integer roomCount;
	private List<Long> roomIds;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
