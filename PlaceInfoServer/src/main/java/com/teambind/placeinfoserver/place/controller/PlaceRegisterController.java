package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.common.exception.application.ForbiddenException;
import com.teambind.placeinfoserver.place.controller.swagger.PlaceRegisterControllerSwagger;
import com.teambind.placeinfoserver.place.domain.enums.AppType;
import com.teambind.placeinfoserver.place.domain.enums.PlaceOperationType;
import com.teambind.placeinfoserver.place.dto.request.PlaceLocationRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceRegisterRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceUpdateRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.service.command.PlaceLocationUpdateService;
import com.teambind.placeinfoserver.place.service.usecase.command.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceRegisterController implements PlaceRegisterControllerSwagger {

	private final RegisterPlaceUseCase registerPlaceUseCase;
	private final UpdatePlaceUseCase updatePlaceUseCase;
	private final DeletePlaceUseCase deletePlaceUseCase;
	private final ActivatePlaceUseCase activatePlaceUseCase;
	private final DeactivatePlaceUseCase deactivatePlaceUseCase;
	private final PlaceLocationUpdateService locationService;

	@Override
	@PostMapping
	public ResponseEntity<PlaceInfoResponse> register(
			@RequestHeader("X-App-Type") String appType,
			@RequestHeader("X-User-Id") String userId,
			@Valid @RequestBody PlaceRegisterRequest req) {
		validatePlaceManagerApp(appType);
		validateRegisterOwnership(req.getPlaceOwnerId(), userId);

		PlaceInfoResponse response = registerPlaceUseCase.execute(req);
		return ResponseEntity.ok(response);
	}

	@Override
	@PutMapping("/{placeId}")
	public ResponseEntity<PlaceInfoResponse> update(
			@RequestHeader("X-App-Type") String appType,
			@RequestHeader("X-User-Id") String userId,
			@PathVariable String placeId,
			@Valid @RequestBody PlaceUpdateRequest req) {
		validatePlaceManagerApp(appType);

		PlaceInfoResponse response = updatePlaceUseCase.execute(placeId, userId, req);
		return ResponseEntity.ok(response);
	}

	@Override
	@PatchMapping("/{placeId}")
	public ResponseEntity<Void> updatePlaceStatus(
			@RequestHeader("X-App-Type") String appType,
			@RequestHeader("X-User-Id") String userId,
			@PathVariable String placeId,
			@RequestParam PlaceOperationType type,
			@RequestParam boolean activate) {
		validatePlaceManagerApp(appType);

		if (type == PlaceOperationType.ACTIVATE) {
			if (activate) {
				activatePlaceUseCase.execute(placeId, userId);
			} else {
				deactivatePlaceUseCase.execute(placeId, userId);
			}
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.badRequest().build();
	}

	@Override
	@PutMapping("/{placeId}/locations")
	public ResponseEntity<Map<String, String>> updateLocation(
			@RequestHeader("X-App-Type") String appType,
			@RequestHeader("X-User-Id") String userId,
			@PathVariable String placeId,
			@Valid @RequestBody PlaceLocationRequest req) {
		validatePlaceManagerApp(appType);

		String responseId = locationService.updateLocation(placeId, userId, req);
		return ResponseEntity.ok(Map.of("placeId", responseId));
	}

	@Override
	@DeleteMapping("/{placeId}")
	public ResponseEntity<Void> delete(
			@RequestHeader("X-App-Type") String appType,
			@RequestHeader("X-User-Id") String userId,
			@PathVariable String placeId) {
		validatePlaceManagerApp(appType);

		deletePlaceUseCase.execute(placeId, userId);
		return ResponseEntity.noContent().build();
	}

	private void validatePlaceManagerApp(String appType) {
		if (!AppType.PLACE_MANAGER.name().equals(appType)) {
			throw ForbiddenException.placeManagerOnly();
		}
	}

	private void validateRegisterOwnership(String requestOwnerId, String userId) {
		if (!requestOwnerId.equals(userId)) {
			throw ForbiddenException.notOwner();
		}
	}
}
