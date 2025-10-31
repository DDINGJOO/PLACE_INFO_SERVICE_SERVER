package com.teambind.placeinfoserver.place.controller;


import com.teambind.placeinfoserver.place.domain.enums.PlaceOperationType;
import com.teambind.placeinfoserver.place.dto.request.PlaceLocationRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceRegisterRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.service.command.PlaceLocationUpdateService;
import com.teambind.placeinfoserver.place.service.command.PlaceRegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 장소 등록/수정/삭제 REST API 컨트롤러 (CQRS - Command)
 * 쓰기 작업만 수행 (등록, 수정, 삭제, 활성화/비활성화)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceRegisterController {
	private final PlaceRegisterService commandService;
	private final PlaceLocationUpdateService locationService;


	
	
	@PostMapping()
	public ResponseEntity<PlaceInfoResponse> register(@Valid @RequestBody PlaceRegisterRequest req) {

		PlaceInfoResponse response = commandService.registerPlace(req);
		return ResponseEntity.ok(response);
	}
	
	@PatchMapping("/{placeId}")
	public ResponseEntity<Void> updatePlaceStatus(
			@RequestParam PlaceOperationType type,
			@RequestParam boolean activate,
			@PathVariable(value = "placeId") String placeId) {
		
		
		//TODO : Change Switch function
		if (type == PlaceOperationType.ACTIVATE) {
			if (activate) {
				commandService.activatePlace(placeId);
			} else {
				commandService.deactivatePlace(placeId);
			}
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.badRequest().build();
	}
	
	@PutMapping("/{placeId}/locations")
	public ResponseEntity<Map<String, String>> registerLocation(
			@PathVariable(value = "placeId") String placeId,
			@Valid @RequestBody PlaceLocationRequest req) {

		String responseId = locationService.updateLocation(placeId, req);
		return ResponseEntity.ok(
				Map.of("placeId", responseId)
		);
	}
	
	@DeleteMapping("/{placeId}")
	public ResponseEntity<Void> delete(@PathVariable(value = "placeId") String placeId) {
		commandService.deletePlace(placeId, "OWNER");
		return ResponseEntity.noContent().build();
	}

	
}
