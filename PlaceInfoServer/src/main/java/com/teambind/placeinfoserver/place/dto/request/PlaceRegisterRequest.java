package com.teambind.placeinfoserver.place.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 장소 등록 요청 DTO
 * JSR-303 Bean Validation을 통한 입력 검증
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceRegisterRequest {

	@NotBlank(message = "장소 소유자 ID는 필수입니다")
	private String placeOwnerId;

	@NotBlank(message = "장소명은 필수입니다")
	@Size(max = 100, message = "장소명은 100자를 초과할 수 없습니다")
	private String placeName;

	@Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다")
	private String description;

	@Size(max = 50, message = "카테고리는 50자를 초과할 수 없습니다")
	private String category;

	@Size(max = 50, message = "장소 타입은 50자를 초과할 수 없습니다")
	private String placeType;

	@Valid
	private PlaceContactRequest contact;
	
	
	@Valid
	private PlaceLocationRequest location;

	@Valid
	private PlaceParkingUpdateRequest parking;
}
