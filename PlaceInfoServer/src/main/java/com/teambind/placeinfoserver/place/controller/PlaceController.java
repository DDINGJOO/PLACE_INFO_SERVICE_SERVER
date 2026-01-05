package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.controller.annotation.RequirePlaceManager;
import com.teambind.placeinfoserver.place.controller.swagger.PlaceControllerSwagger;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.service.usecase.query.GetPlaceDetailUseCase;
import com.teambind.placeinfoserver.place.service.usecase.query.GetPlacesByUserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController implements PlaceControllerSwagger {

	private final GetPlaceDetailUseCase getPlaceDetailUseCase;
	private final GetPlacesByUserUseCase getPlacesByUserUseCase;

	@Override
	@GetMapping("/my")
	@RequirePlaceManager
	public ResponseEntity<List<PlaceInfoResponse>> getMyPlaces(
			@RequestHeader(value = "X-User-Id") String userId) {
		log.info("내 공간 목록 조회 요청: userId={}", userId);

		List<PlaceInfoResponse> response = getPlacesByUserUseCase.execute(userId);

		log.info("내 공간 목록 조회 완료: userId={}, count={}", userId, response.size());
		return ResponseEntity.ok(response);
	}

	@Override
	@GetMapping("/{placeId}")
	public ResponseEntity<PlaceInfoResponse> getPlaceDetail(@PathVariable String placeId) {
		log.info("공간 상세 조회 요청: placeId={}", placeId);

		PlaceInfoResponse response = getPlaceDetailUseCase.execute(placeId);

		log.info("공간 상세 조회 완료: placeId={}, placeName={}", placeId, response.getPlaceName());
		return ResponseEntity.ok(response);
	}
}
