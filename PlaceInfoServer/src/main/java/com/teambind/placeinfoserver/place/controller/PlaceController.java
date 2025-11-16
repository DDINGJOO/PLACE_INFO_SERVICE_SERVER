package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.service.usecase.query.GetPlaceDetailUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 공간 기본 조회 REST API 컨트롤러
 * ID 기반 상세 조회 등 기본적인 읽기 작업 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
@Tag(name = "Place", description = "공간 기본 조회 API")
public class PlaceController {
	
	private final GetPlaceDetailUseCase getPlaceDetailUseCase;
	
	/**
	 * ID로 공간 상세 조회 API
	 *
	 * @param placeId 공간 ID
	 * @return 공간 상세 정보
	 */
	@GetMapping("/{placeId}")
	@Operation(summary = "공간 상세 조회", description = "ID로 공간의 상세 정보를 조회합니다")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "존재하지 않는 공간")
	public ResponseEntity<PlaceInfoResponse> getPlaceDetail(
			@Parameter(description = "공간 ID", required = true, example = "1")
			@PathVariable String placeId
	) {
		log.info("공간 상세 조회 요청: placeId={}", placeId);
		
		PlaceInfoResponse response = getPlaceDetailUseCase.execute(placeId);
		
		log.info("공간 상세 조회 완료: placeId={}, placeName={}", placeId, response.getPlaceName());
		return ResponseEntity.ok(response);
	}
}
