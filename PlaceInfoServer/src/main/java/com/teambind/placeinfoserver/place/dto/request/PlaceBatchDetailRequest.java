package com.teambind.placeinfoserver.place.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceBatchDetailRequest {
	
	@NotEmpty(message = "조회할 placeId 목록은 비어있을 수 없습니다")
	@Size(min = 1, max = 50, message = "한 번에 조회 가능한 최대 개수는 50개입니다")
	private List<Long> placeIds;
	
	/**
	 * 요청된 placeId 목록을 반환합니다.
	 * 방어적 복사를 통해 불변성을 보장합니다.
	 */
	public List<Long> getPlaceIds() {
		return placeIds != null ? List.copyOf(placeIds) : List.of();
	}
}
