package com.teambind.placeinfoserver.place.controller.swagger;

import com.teambind.placeinfoserver.place.dto.request.LocationSearchRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceBatchDetailRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceSearchRequest;
import com.teambind.placeinfoserver.place.dto.response.CountResponse;
import com.teambind.placeinfoserver.place.dto.response.PlaceBatchDetailResponse;
import com.teambind.placeinfoserver.place.dto.response.PlaceSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Place Search", description = "공간 탐색 API")
public interface PlaceSearchControllerSwagger {

    @Operation(summary = "공간 통합 검색", description = "다양한 조건으로 공간을 검색합니다")
    @ApiResponse(responseCode = "200", description = "검색 성공")
    ResponseEntity<PlaceSearchResponse> search(
            @Parameter(description = "검색 키워드") String keyword,
            @Parameter(description = "장소명") String placeName,
            @Parameter(description = "카테고리") String category,
            @Parameter(description = "장소 타입") String placeType,
            @Parameter(description = "키워드 ID 목록") List<Long> keywordIds,
            @Parameter(description = "주차 가능 여부") Boolean parkingAvailable,
            @Parameter(description = "위도") Double latitude,
            @Parameter(description = "경도") Double longitude,
            @Parameter(description = "검색 반경(미터)") Integer radius,
            @Parameter(description = "시/도") String province,
            @Parameter(description = "시/군/구") String city,
            @Parameter(description = "동/읍/면") String district,
            @Parameter(description = "정렬 기준", example = "DISTANCE, RATING, REVIEW_COUNT, CREATED_AT, PLACE_NAME")
            PlaceSearchRequest.SortBy sortBy,
            @Parameter(description = "정렬 방향", example = "ASC, DESC")
            PlaceSearchRequest.SortDirection sortDirection,
            @Parameter(description = "페이징 커서") String cursor,
            @Parameter(description = "페이지 크기") Integer size,
            @Parameter(description = "등록 상태 필터", example = "REGISTERED, UNREGISTERED")
            String registrationStatus);

    @Operation(summary = "위치 기반 검색", description = "좌표를 중심으로 반경 내 장소를 검색합니다")
    @ApiResponse(responseCode = "200", description = "검색 성공")
    ResponseEntity<PlaceSearchResponse> searchByLocation(LocationSearchRequest request);

    @Operation(summary = "지역별 검색", description = "특정 지역 내 장소를 검색합니다")
    @ApiResponse(responseCode = "200", description = "검색 성공")
    ResponseEntity<PlaceSearchResponse> searchByRegion(
            @Parameter(description = "시/도", required = true) String province,
            @Parameter(description = "시/군/구") String city,
            @Parameter(description = "동/읍/면") String district,
            @Parameter(description = "페이징 커서") String cursor,
            @Parameter(description = "페이지 크기") Integer size,
            @Parameter(description = "등록 상태 필터", example = "REGISTERED, UNREGISTERED")
            String registrationStatus);

    @Operation(summary = "인기 장소 조회", description = "평점과 리뷰 기준 인기 장소를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<PlaceSearchResponse> getPopularPlaces(
            @Parameter(description = "조회 개수") Integer size,
            @Parameter(description = "등록 상태 필터", example = "REGISTERED, UNREGISTERED")
            String registrationStatus);

    @Operation(summary = "최신 장소 조회", description = "최근 등록된 장소를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<PlaceSearchResponse> getRecentPlaces(
            @Parameter(description = "조회 개수") Integer size,
            @Parameter(description = "등록 상태 필터", example = "REGISTERED, UNREGISTERED")
            String registrationStatus);

    @Operation(summary = "검색 결과 개수 조회", description = "검색 조건에 맞는 전체 결과 수를 반환합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<CountResponse> countSearchResults(PlaceSearchRequest request);

    @Operation(
            summary = "공간 배치 상세 조회",
            description = "여러 공간의 상세 정보를 한 번에 조회합니다. 최대 50개까지 조회 가능하며, 존재하지 않는 ID는 failed 필드에 반환됩니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공 (부분 실패 포함)")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 목록, 개수 초과 등)")
    ResponseEntity<PlaceBatchDetailResponse> getPlaceDetailsBatch(PlaceBatchDetailRequest request);
}
