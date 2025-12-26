package com.teambind.placeinfoserver.place.controller;


import com.teambind.placeinfoserver.place.common.exception.application.ForbiddenException;
import com.teambind.placeinfoserver.place.common.exception.application.InvalidRequestException;
import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.enums.AppType;
import com.teambind.placeinfoserver.place.domain.enums.PlaceOperationType;
import com.teambind.placeinfoserver.place.dto.request.PlaceLocationRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceRegisterRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.service.command.PlaceLocationUpdateService;
import com.teambind.placeinfoserver.place.service.usecase.command.ActivatePlaceUseCase;
import com.teambind.placeinfoserver.place.service.usecase.command.DeactivatePlaceUseCase;
import com.teambind.placeinfoserver.place.service.usecase.command.DeletePlaceUseCase;
import com.teambind.placeinfoserver.place.service.usecase.command.RegisterPlaceUseCase;
import com.teambind.placeinfoserver.place.service.usecase.common.IdParser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 장소 등록/수정/삭제 REST API 컨트롤러 (CQRS - Command)
 * 쓰기 작업만 수행 (등록, 수정, 삭제, 활성화/비활성화)
 * 모든 Command API는 X-App-Type, X-User-Id 헤더 필수
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceRegisterController {

	private static final String HEADER_APP_TYPE = "X-App-Type";
	private static final String HEADER_USER_ID = "X-User-Id";

	private final RegisterPlaceUseCase registerPlaceUseCase;
	private final DeletePlaceUseCase deletePlaceUseCase;
	private final ActivatePlaceUseCase activatePlaceUseCase;
	private final DeactivatePlaceUseCase deactivatePlaceUseCase;
	private final PlaceLocationUpdateService locationService;
	private final PlaceInfoRepository placeInfoRepository;

	/**
	 * 필수 헤더 검증
	 */
	private void validateRequiredHeader(String headerValue, String headerName) {
		if (headerValue == null || headerValue.isBlank()) {
			throw InvalidRequestException.headerMissing(headerName);
		}
	}

	/**
	 * X-App-Type 헤더를 파싱하여 AppType으로 변환
	 */
	private AppType parseAppType(String appTypeHeader) {
		validateRequiredHeader(appTypeHeader, HEADER_APP_TYPE);
		try {
			return AppType.valueOf(appTypeHeader);
		} catch (IllegalArgumentException e) {
			throw InvalidRequestException.invalidFormat(HEADER_APP_TYPE);
		}
	}

	/**
	 * PLACE_MANAGER 앱 타입 검증
	 */
	private void validatePlaceManagerApp(AppType appType) {
		if (appType != AppType.PLACE_MANAGER) {
			throw ForbiddenException.placeManagerOnly();
		}
	}

	/**
	 * 소유주 검증 (기존 리소스에 대해)
	 */
	private void validateOwnership(String placeId, String userId) {
		PlaceInfo placeInfo = placeInfoRepository.findById(IdParser.parsePlaceId(placeId))
				.orElseThrow(PlaceNotFoundException::new);

		if (!placeInfo.getUserId().equals(userId)) {
			throw ForbiddenException.notOwner();
		}
	}

	/**
	 * 등록 요청의 소유주 ID와 헤더의 사용자 ID 일치 검증
	 */
	private void validateRegisterOwnership(String requestOwnerId, String userId) {
		if (!requestOwnerId.equals(userId)) {
			throw ForbiddenException.notOwner();
		}
	}

	@PostMapping()
	public ResponseEntity<PlaceInfoResponse> register(
			@RequestHeader(value = "X-App-Type", required = false) String appTypeHeader,
			@RequestHeader(value = "X-User-Id", required = false) String userId,
			@Valid @RequestBody PlaceRegisterRequest req) {
		validateRequiredHeader(userId, HEADER_USER_ID);
		validatePlaceManagerApp(parseAppType(appTypeHeader));
		validateRegisterOwnership(req.getPlaceOwnerId(), userId);

		PlaceInfoResponse response = registerPlaceUseCase.execute(req);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{placeId}")
	public ResponseEntity<Void> updatePlaceStatus(
			@RequestHeader(value = "X-App-Type", required = false) String appTypeHeader,
			@RequestHeader(value = "X-User-Id", required = false) String userId,
			@RequestParam PlaceOperationType type,
			@RequestParam boolean activate,
			@PathVariable(value = "placeId") String placeId) {
		validateRequiredHeader(userId, HEADER_USER_ID);
		validatePlaceManagerApp(parseAppType(appTypeHeader));
		validateOwnership(placeId, userId);

		if (type == PlaceOperationType.ACTIVATE) {
			if (activate) {
				activatePlaceUseCase.execute(placeId);
			} else {
				deactivatePlaceUseCase.execute(placeId);
			}
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.badRequest().build();
	}

	@PutMapping("/{placeId}/locations")
	public ResponseEntity<Map<String, String>> registerLocation(
			@RequestHeader(value = "X-App-Type", required = false) String appTypeHeader,
			@RequestHeader(value = "X-User-Id", required = false) String userId,
			@PathVariable(value = "placeId") String placeId,
			@Valid @RequestBody PlaceLocationRequest req) {
		validateRequiredHeader(userId, HEADER_USER_ID);
		validatePlaceManagerApp(parseAppType(appTypeHeader));
		validateOwnership(placeId, userId);

		String responseId = locationService.updateLocation(placeId, req);
		return ResponseEntity.ok(
				Map.of("placeId", responseId)
		);
	}

	@DeleteMapping("/{placeId}")
	public ResponseEntity<Void> delete(
			@RequestHeader(value = "X-App-Type", required = false) String appTypeHeader,
			@RequestHeader(value = "X-User-Id", required = false) String userId,
			@PathVariable(value = "placeId") String placeId) {
		validateRequiredHeader(userId, HEADER_USER_ID);
		validatePlaceManagerApp(parseAppType(appTypeHeader));
		validateOwnership(placeId, userId);

		deletePlaceUseCase.execute(placeId, userId);
		return ResponseEntity.noContent().build();
	}
}
