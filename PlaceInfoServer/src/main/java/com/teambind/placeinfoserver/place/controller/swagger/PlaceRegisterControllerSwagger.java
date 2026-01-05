package com.teambind.placeinfoserver.place.controller.swagger;

import com.teambind.placeinfoserver.place.domain.enums.PlaceOperationType;
import com.teambind.placeinfoserver.place.dto.request.PlaceLocationRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceRegisterRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceUpdateRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Tag(name = "Place Register", description = "공간 등록/수정/삭제 API (PLACE_MANAGER 전용)")
public interface PlaceRegisterControllerSwagger {

    @Operation(summary = "업체 등록", description = "새로운 업체를 등록합니다")
    @ApiResponse(responseCode = "200", description = "등록 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    ResponseEntity<PlaceInfoResponse> register(String userId, PlaceRegisterRequest req);

    @Operation(summary = "업체 정보 수정", description = "업체의 기본 정보, 연락처, 주차, 키워드를 수정합니다. 위치 정보는 별도 API를 사용해주세요.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "403", description = "권한 없음 (본인 소유 업체만 수정 가능)")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 업체")
    ResponseEntity<PlaceInfoResponse> update(
            @Parameter(description = "공간 ID", required = true) String placeId,
            PlaceUpdateRequest req);

    @Operation(summary = "업체 상태 변경", description = "업체의 활성화/비활성화 상태를 변경합니다")
    @ApiResponse(responseCode = "204", description = "상태 변경 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 업체")
    ResponseEntity<Void> updatePlaceStatus(
            @Parameter(description = "공간 ID", required = true) String placeId,
            @Parameter(description = "작업 타입", required = true) PlaceOperationType type,
            @Parameter(description = "활성화 여부", required = true) boolean activate);

    @Operation(summary = "위치 정보 수정", description = "업체의 위치 정보를 수정합니다")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 업체")
    ResponseEntity<Map<String, String>> updateLocation(
            @Parameter(description = "공간 ID", required = true) String placeId,
            PlaceLocationRequest req);

    @Operation(summary = "업체 삭제", description = "업체를 삭제합니다 (소프트 삭제)")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 업체")
    ResponseEntity<Void> delete(
            String userId,
            @Parameter(description = "공간 ID", required = true) String placeId);
}
