package com.teambind.placeinfoserver.place.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 검색 결과 개수 응답 DTO
 * 검색 조건에 맞는 전체 결과 수를 반환
 */
@Getter
@Setter
@AllArgsConstructor
public class CountResponse {
	private Long count;
}
