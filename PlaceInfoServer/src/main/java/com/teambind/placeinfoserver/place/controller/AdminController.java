package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.service.usecase.command.ApprovePlaceUseCase;
import com.teambind.placeinfoserver.place.service.usecase.command.DeletePlaceUseCase;
import com.teambind.placeinfoserver.place.service.usecase.command.RejectPlaceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 전용 API 컨트롤러
 * - 업체 승인/거부
 * - 업체 삭제 (관리자 권한)
 */
@RequestMapping("/api/v1/admin/places")
@RequiredArgsConstructor
@RestController
public class AdminController {

	// Admin Command UseCases
	private final ApprovePlaceUseCase approvePlaceUseCase;
	private final RejectPlaceUseCase rejectPlaceUseCase;
	private final DeletePlaceUseCase deletePlaceUseCase;
	
	@PatchMapping("/{placeId}")
	public ResponseEntity<Void> approve(@RequestParam String type, @RequestParam boolean contents, @PathVariable(value = "placeId") String placeId) {
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


	@DeleteMapping("/{placeId}")
	public ResponseEntity<Void> delete(@PathVariable(value = "placeId") String placeId) {
		deletePlaceUseCase.execute(placeId, "ADMIN");
		return ResponseEntity.noContent().build();
	}
}
