package com.teambind.placeinfoserver.place.controller;


import com.teambind.placeinfoserver.place.dto.request.PlaceLocationRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceRegisterRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.service.command.PlaceLocationUpdateService;
import com.teambind.placeinfoserver.place.service.command.PlaceRegisterService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceRegisterController {
	private final PlaceRegisterService infoService;
	private final PlaceLocationUpdateService locationService;


	
	
	@PostMapping()
	public ResponseEntity<PlaceInfoResponse> register(@RequestBody PlaceRegisterRequest req) {
		
		PlaceInfoResponse response = infoService.registerPlace(req);
		return ResponseEntity.ok(response);
	}
	
	@PatchMapping("/{placeId}")
	public ResponseEntity<Void> activate(@RequestParam String type, @RequestParam boolean contents, @PathVariable(value = "placeId") String placeId) {
		if (type.equalsIgnoreCase("activate")) {
			if (contents) {
				infoService.activatePlace(placeId);
			} else {
				infoService.deactivatePlace(placeId);
			}
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.badRequest().build();
	}
	
	@PutMapping("/{placeId}/locations")
	public ResponseEntity<Map<String, String>> registerLocation(@PathVariable(value = "placeId") String placeId, @RequestBody PlaceLocationRequest req) {
		
		String responseId = locationService.updateLocation(placeId, req);
		return ResponseEntity.ok(
				Map.of("placeId", responseId)
		);
	}
	
	@DeleteMapping("/{placeId}")
	public ResponseEntity<Void> delete(@PathVariable(value = "placeId") String placeId) {
		infoService.deletePlace(placeId, "OWNER");
		return ResponseEntity.noContent().build();
	}

	
}
