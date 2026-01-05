package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.controller.swagger.AdminControllerSwagger;
import com.teambind.placeinfoserver.place.service.usecase.command.ApprovePlaceUseCase;
import com.teambind.placeinfoserver.place.service.usecase.command.DeletePlaceUseCase;
import com.teambind.placeinfoserver.place.service.usecase.command.RejectPlaceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/admin/places")
@RequiredArgsConstructor
@RestController
public class AdminController implements AdminControllerSwagger {

	private final ApprovePlaceUseCase approvePlaceUseCase;
	private final RejectPlaceUseCase rejectPlaceUseCase;
	private final DeletePlaceUseCase deletePlaceUseCase;

	@Override
	@PatchMapping("/{placeId}")
	public ResponseEntity<Void> approve(
			@RequestParam String type,
			@RequestParam boolean contents,
			@PathVariable(value = "placeId") String placeId) {
		if (type.equalsIgnoreCase("approve")) {
			if (contents) {
				approvePlaceUseCase.execute(placeId);
			} else {
				rejectPlaceUseCase.execute(placeId);
			}
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.badRequest().build();
	}

	@Override
	@DeleteMapping("/{placeId}")
	public ResponseEntity<Void> delete(@PathVariable(value = "placeId") String placeId) {
		deletePlaceUseCase.executeAsAdmin(placeId, "ADMIN");
		return ResponseEntity.noContent().build();
	}
}
