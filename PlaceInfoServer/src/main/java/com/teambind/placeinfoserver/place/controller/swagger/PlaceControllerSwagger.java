package com.teambind.placeinfoserver.place.controller.swagger;

import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Place", description = "공간 기본 조회 API")
public interface PlaceControllerSwagger {

    @Operation(summary = "내 공간 목록 조회", description = "본인이 등록한 공간 목록을 조회합니다 (PLACE_MANAGER 앱 전용)")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "400", description = "필수 헤더 누락")
    @ApiResponse(responseCode = "403", description = "PLACE_MANAGER 앱만 접근 가능")
    ResponseEntity<List<PlaceInfoResponse>> getMyPlaces(String userId);

    @Operation(summary = "공간 상세 조회", description = "ID로 공간의 상세 정보를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 공간")
    ResponseEntity<PlaceInfoResponse> getPlaceDetail(
            @Parameter(description = "공간 ID", required = true, example = "1") String placeId);
}
