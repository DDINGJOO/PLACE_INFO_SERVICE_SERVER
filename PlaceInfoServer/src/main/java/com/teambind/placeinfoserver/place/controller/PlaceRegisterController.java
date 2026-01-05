package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.common.exception.application.ForbiddenException;
import com.teambind.placeinfoserver.place.controller.annotation.RequirePlaceManager;
import com.teambind.placeinfoserver.place.controller.annotation.ValidateOwnership;
import com.teambind.placeinfoserver.place.controller.swagger.PlaceRegisterControllerSwagger;
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
	@RequirePlaceManager
	public ResponseEntity<PlaceInfoResponse> register(
			@RequestHeader(value = "X-User-Id") String userId,
			@Valid @RequestBody PlaceRegisterRequest req) {
		validateRegisterOwnership(req.getPlaceOwnerId(), userId);

		PlaceInfoResponse response = registerPlaceUseCase.execute(req);
		return ResponseEntity.ok(response);
	}

	@Override
	@PutMapping("/{placeId}")
	@RequirePlaceManager
	@ValidateOwnership
	public ResponseEntity<PlaceInfoResponse> update(
			@PathVariable String placeId,
			@Valid @RequestBody PlaceUpdateRequest req) {
		PlaceInfoResponse response = updatePlaceUseCase.execute(placeId, req);
		return ResponseEntity.ok(response);
	}

	@Override
	@PatchMapping("/{placeId}")
	@RequirePlaceManager
	@ValidateOwnership
	public ResponseEntity<Void> updatePlaceStatus(
			@PathVariable String placeId,
			@RequestParam PlaceOperationType type,
			@RequestParam boolean activate) {
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

	@Override
	@PutMapping("/{placeId}/locations")
	@RequirePlaceManager
	@ValidateOwnership
	public ResponseEntity<Map<String, String>> updateLocation(
			@PathVariable String placeId,
			@Valid @RequestBody PlaceLocationRequest req) {
		String responseId = locationService.updateLocation(placeId, req);
		return ResponseEntity.ok(Map.of("placeId", responseId));
	}

	@Override
	@DeleteMapping("/{placeId}")
	@RequirePlaceManager
	@ValidateOwnership
	public ResponseEntity<Void> delete(
			@RequestHeader(value = "X-User-Id") String userId,
			@PathVariable String placeId) {
		deletePlaceUseCase.execute(placeId, userId);
		return ResponseEntity.noContent().build();
	}

	private void validateRegisterOwnership(String requestOwnerId, String userId) {
		if (!requestOwnerId.equals(userId)) {
			throw ForbiddenException.notOwner();
		}
	}
}
