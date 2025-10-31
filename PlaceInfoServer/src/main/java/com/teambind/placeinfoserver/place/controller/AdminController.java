package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.service.command.PlaceRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/admin/places")
@RequiredArgsConstructor
@RestController
public class AdminController {
	private final PlaceRegisterService infoService;
	
	@PatchMapping("/{placeId}")
	public ResponseEntity<Void> approve(@RequestParam String type, @RequestParam boolean contents, @PathVariable(value = "placeId") String placeId) {
		if (type.equalsIgnoreCase("approve")) {
			if (contents) {
				infoService.approvePlace(placeId);
			} else {
				infoService.rejectPlace(placeId);
			}
			return ResponseEntity.noContent().build();
		}
		
		return ResponseEntity.badRequest().build();
	}
	
	
	@DeleteMapping("/{placeId}")
	public ResponseEntity<Void> delete(@PathVariable(value = "placeId") String placeId) {
		infoService.deletePlace(placeId, "ADMIN");
		return ResponseEntity.noContent().build();
	}
}
