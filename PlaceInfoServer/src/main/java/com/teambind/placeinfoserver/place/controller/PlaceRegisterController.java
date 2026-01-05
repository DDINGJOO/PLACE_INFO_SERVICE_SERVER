package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.common.exception.application.ForbiddenException;
import com.teambind.placeinfoserver.place.controller.annotation.RequirePlaceManager;
import com.teambind.placeinfoserver.place.controller.annotation.ValidateOwnership;
import com.teambind.placeinfoserver.place.domain.enums.PlaceOperationType;
import com.teambind.placeinfoserver.place.dto.request.PlaceLocationRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceRegisterRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceUpdateRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.service.command.PlaceLocationUpdateService;
import com.teambind.placeinfoserver.place.service.usecase.command.ActivatePlaceUseCase;
import com.teambind.placeinfoserver.place.service.usecase.command.DeactivatePlaceUseCase;
import com.teambind.placeinfoserver.place.service.usecase.command.DeletePlaceUseCase;
import com.teambind.placeinfoserver.place.service.usecase.command.RegisterPlaceUseCase;
import com.teambind.placeinfoserver.place.service.usecase.command.UpdatePlaceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 장소 등록/수정/삭제 REST API 컨트롤러 (CQRS - Command)
 * 쓰기 작업만 수행 (등록, 수정, 삭제, 활성화/비활성화)
 * 모든 Command API는 X-App-Type: PLACE_MANAGER, X-User-Id 헤더 필수
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
@Tag(name = "Place Register", description = "공간 등록/수정/삭제 API (PLACE_MANAGER 전용)")
public class PlaceRegisterController {

	private final RegisterPlaceUseCase registerPlaceUseCase;
	private final UpdatePlaceUseCase updatePlaceUseCase;
	private final DeletePlaceUseCase deletePlaceUseCase;
	private final ActivatePlaceUseCase activatePlaceUseCase;
	private final DeactivatePlaceUseCase deactivatePlaceUseCase;
	private final PlaceLocationUpdateService locationService;

	@PostMapping
	@RequirePlaceManager
	@Operation(summary = "업체 등록", description = "새로운 업체를 등록합니다")
	@ApiResponse(responseCode = "200", description = "등록 성공")
	@ApiResponse(responseCode = "400", description = "잘못된 요청")
	@ApiResponse(responseCode = "403", description = "권한 없음")
	public ResponseEntity<PlaceInfoResponse> register(
			@RequestHeader(value = "X-User-Id") String userId,
			@Valid @RequestBody PlaceRegisterRequest req) {
		validateRegisterOwnership(req.getPlaceOwnerId(), userId);

		PlaceInfoResponse response = registerPlaceUseCase.execute(req);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{placeId}")
	@RequirePlaceManager
	@ValidateOwnership
	@Operation(summary = "업체 정보 수정", description = "업체의 기본 정보, 연락처, 주차, 키워드를 수정합니다. 위치 정보는 별도 API를 사용해주세요.")
	@ApiResponse(responseCode = "200", description = "수정 성공")
	@ApiResponse(responseCode = "400", description = "잘못된 요청")
	@ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 업체만 수정 가능)")
	@ApiResponse(responseCode = "404", description = "존재하지 않는 업체")
	public ResponseEntity<PlaceInfoResponse> update(
			@Parameter(description = "공간 ID", required = true) @PathVariable String placeId,
			@Valid @RequestBody PlaceUpdateRequest req) {
		PlaceInfoResponse response = updatePlaceUseCase.execute(placeId, req);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{placeId}")
	@RequirePlaceManager
	@ValidateOwnership
	@Operation(summary = "업체 상태 변경", description = "업체의 활성화/비활성화 상태를 변경합니다")
	@ApiResponse(responseCode = "204", description = "상태 변경 성공")
	@ApiResponse(responseCode = "400", description = "잘못된 요청")
	@ApiResponse(responseCode = "403", description = "권한 없음")
	@ApiResponse(responseCode = "404", description = "존재하지 않는 업체")
	public ResponseEntity<Void> updatePlaceStatus(
			@Parameter(description = "공간 ID", required = true) @PathVariable String placeId,
			@Parameter(description = "작업 타입", required = true) @RequestParam PlaceOperationType type,
			@Parameter(description = "활성화 여부", required = true) @RequestParam boolean activate) {
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
	@RequirePlaceManager
	@ValidateOwnership
	@Operation(summary = "위치 정보 수정", description = "업체의 위치 정보를 수정합니다")
	@ApiResponse(responseCode = "200", description = "수정 성공")
	@ApiResponse(responseCode = "400", description = "잘못된 요청")
	@ApiResponse(responseCode = "403", description = "권한 없음")
	@ApiResponse(responseCode = "404", description = "존재하지 않는 업체")
	public ResponseEntity<Map<String, String>> updateLocation(
			@Parameter(description = "공간 ID", required = true) @PathVariable String placeId,
			@Valid @RequestBody PlaceLocationRequest req) {
		String responseId = locationService.updateLocation(placeId, req);
		return ResponseEntity.ok(Map.of("placeId", responseId));
	}

	@DeleteMapping("/{placeId}")
	@RequirePlaceManager
	@ValidateOwnership
	@Operation(summary = "업체 삭제", description = "업체를 삭제합니다 (소프트 삭제)")
	@ApiResponse(responseCode = "204", description = "삭제 성공")
	@ApiResponse(responseCode = "403", description = "권한 없음")
	@ApiResponse(responseCode = "404", description = "존재하지 않는 업체")
	public ResponseEntity<Void> delete(
			@RequestHeader(value = "X-User-Id") String userId,
			@Parameter(description = "공간 ID", required = true) @PathVariable String placeId) {
		deletePlaceUseCase.execute(placeId, userId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 등록 요청의 소유주 ID와 헤더의 사용자 ID 일치 검증
	 */
	private void validateRegisterOwnership(String requestOwnerId, String userId) {
		if (!requestOwnerId.equals(userId)) {
			throw ForbiddenException.notOwner();
		}
	}
}
