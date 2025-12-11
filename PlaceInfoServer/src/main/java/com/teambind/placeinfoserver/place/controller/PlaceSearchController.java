package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.dto.request.LocationSearchRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceBatchDetailRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceSearchRequest;
import com.teambind.placeinfoserver.place.dto.response.CountResponse;
import com.teambind.placeinfoserver.place.dto.response.PlaceBatchDetailResponse;
import com.teambind.placeinfoserver.place.dto.response.PlaceSearchResponse;
import com.teambind.placeinfoserver.place.repository.PlaceAdvancedSearchRepository;
import com.teambind.placeinfoserver.place.service.usecase.query.GetPlaceDetailsBatchUseCase;
import com.teambind.placeinfoserver.place.service.usecase.query.SearchPlacesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공간 탐색 REST API 컨트롤러 (CQRS - Query)
 * 커서 기반 페이징과 다양한 검색 옵션 제공
 * 읽기 전용 작업만 수행
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/places/search")
@RequiredArgsConstructor
@Tag(name = "Place Search", description = "공간 탐색 API")
public class PlaceSearchController {
	
	// Query UseCases
	private final SearchPlacesUseCase searchPlacesUseCase;
	private final GetPlaceDetailsBatchUseCase getPlaceDetailsBatchUseCase;
	private final PlaceAdvancedSearchRepository searchRepository;
	
	/**
	 * 통합 검색 API
	 * GET 파라미터를 통한 유연한 검색 지원
	 */
	@GetMapping(produces = "application/json;charset=UTF-8")
	@Operation(summary = "공간 통합 검색", description = "다양한 조건으로 공간을 검색합니다")
	@ApiResponse(responseCode = "200", description = "검색 성공")
	public ResponseEntity<PlaceSearchResponse> search(
			@Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
			@Parameter(description = "장소명") @RequestParam(required = false) String placeName,
			@Parameter(description = "카테고리") @RequestParam(required = false) String category,
			@Parameter(description = "장소 타입") @RequestParam(required = false) String placeType,
			@Parameter(description = "키워드 ID 목록") @RequestParam(required = false) List<Long> keywordIds,
			@Parameter(description = "주차 가능 여부") @RequestParam(required = false) Boolean parkingAvailable,
			@Parameter(description = "위도") @RequestParam(required = false) Double latitude,
			@Parameter(description = "경도") @RequestParam(required = false) Double longitude,
			@Parameter(description = "검색 반경(미터)") @RequestParam(defaultValue = "5000") Integer radius,
			@Parameter(description = "시/도") @RequestParam(required = false) String province,
			@Parameter(description = "시/군/구") @RequestParam(required = false) String city,
			@Parameter(description = "동/읍/면") @RequestParam(required = false) String district,
			@Parameter(description = "정렬 기준", example = "DISTANCE, RATING, REVIEW_COUNT, CREATED_AT, PLACE_NAME")
			@RequestParam(defaultValue = "DISTANCE") PlaceSearchRequest.SortBy sortBy,
			@Parameter(description = "정렬 방향", example = "ASC, DESC")
			@RequestParam(defaultValue = "ASC") PlaceSearchRequest.SortDirection sortDirection,
			@Parameter(description = "페이징 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size
	) {
		PlaceSearchRequest request = PlaceSearchRequest.builder()
				.keyword(keyword)
				.placeName(placeName)
				.category(category)
				.placeType(placeType)
				.keywordIds(keywordIds)
				.parkingAvailable(parkingAvailable)
				.latitude(latitude)
				.longitude(longitude)
				.radiusInMeters(radius)
				.province(province)
				.city(city)
				.district(district)
				.sortBy(sortBy)
				.sortDirection(sortDirection)
				.cursor(cursor)
				.size(size)
				.build();
		
		// URL 인코딩 확인을 위한 상세 로깅
		log.info("====== 검색 요청 상세 정보 ======");
		log.info("keyword=[{}], placeName=[{}]", keyword, placeName);
		
		if (keyword != null && !keyword.isEmpty()) {
			log.info("keyword 상세: 길이={}, UTF-8 bytes={}",
					keyword.length(),
					java.util.Arrays.toString(keyword.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		}
		if (placeName != null && !placeName.isEmpty()) {
			log.info("placeName 상세: 길이={}, UTF-8 bytes={}",
					placeName.length(),
					java.util.Arrays.toString(placeName.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		}
		log.info("location=({},{}), cursor={}, size={}", latitude, longitude, cursor, size);
		log.info("================================");
		
		PlaceSearchResponse response = searchPlacesUseCase.execute(request);
		
		log.info("검색 결과: {} 건 조회됨", response.getCount());
		return ResponseEntity.ok(response);
	}
	
	/**
	 * 위치 기반 검색 API
	 * 특정 좌표 중심으로 반경 내 장소 검색
	 */
	@PostMapping("/location")
	@Operation(summary = "위치 기반 검색", description = "좌표를 중심으로 반경 내 장소를 검색합니다")
	@ApiResponse(responseCode = "200", description = "검색 성공")
	public ResponseEntity<PlaceSearchResponse> searchByLocation(
			@Valid @RequestBody LocationSearchRequest request
	) {
		PlaceSearchRequest searchRequest = PlaceSearchRequest.builder()
				.latitude(request.getLatitude())
				.longitude(request.getLongitude())
				.radiusInMeters(request.getRadius())
				.keyword(request.getKeyword())
				.keywordIds(request.getKeywordIds())
				.parkingAvailable(request.getParkingAvailable())
				.sortBy(PlaceSearchRequest.SortBy.DISTANCE)
				.sortDirection(PlaceSearchRequest.SortDirection.ASC)
				.cursor(request.getCursor())
				.size(request.getSize())
				.build();
		
		log.info("위치 기반 검색: ({}, {}) 반경 {}m",
				request.getLatitude(), request.getLongitude(), request.getRadius());
		
		PlaceSearchResponse response = searchPlacesUseCase.execute(searchRequest);
		return ResponseEntity.ok(response);
	}
	
	/**
	 * 지역별 검색 API
	 */
	@GetMapping("/region")
	@Operation(summary = "지역별 검색", description = "특정 지역 내 장소를 검색합니다")
	@ApiResponse(responseCode = "200", description = "검색 성공")
	public ResponseEntity<PlaceSearchResponse> searchByRegion(
			@Parameter(description = "시/도", required = true) @RequestParam String province,
			@Parameter(description = "시/군/구") @RequestParam(required = false) String city,
			@Parameter(description = "동/읍/면") @RequestParam(required = false) String district,
			@Parameter(description = "페이징 커서") @RequestParam(required = false) String cursor,
			@Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size
	) {
		log.info("지역 검색: {}/{}/{}", province, city, district);
		
		PlaceSearchRequest searchRequest = PlaceSearchRequest.builder()
				.province(province)
				.city(city)
				.district(district)
				.cursor(cursor)
				.size(size != null ? size : 20)
				.isActive(true)
				.approvalStatus("APPROVED")
				.build();
		
		PlaceSearchResponse response = searchPlacesUseCase.execute(searchRequest);
		return ResponseEntity.ok(response);
	}
	
	/**
	 * 인기 장소 조회 API
	 */
	@GetMapping("/popular")
	@Operation(summary = "인기 장소 조회", description = "평점과 리뷰 기준 인기 장소를 조회합니다")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	public ResponseEntity<PlaceSearchResponse> getPopularPlaces(
			@Parameter(description = "조회 개수") @RequestParam(defaultValue = "10") Integer size
	) {
		log.info("인기 장소 조회: {} 건", size);
		
		PlaceSearchRequest request = PlaceSearchRequest.builder()
				.sortBy(PlaceSearchRequest.SortBy.RATING)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.size(size != null ? size : 10)
				.isActive(true)
				.approvalStatus("APPROVED")
				.build();
		
		PlaceSearchResponse response = searchPlacesUseCase.execute(request);
		return ResponseEntity.ok(response);
	}
	
	/**
	 * 최신 등록 장소 조회 API
	 */
	@GetMapping("/recent")
	@Operation(summary = "최신 장소 조회", description = "최근 등록된 장소를 조회합니다")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	public ResponseEntity<PlaceSearchResponse> getRecentPlaces(
			@Parameter(description = "조회 개수") @RequestParam(defaultValue = "10") Integer size
	) {
		log.info("최신 장소 조회: {} 건", size);
		
		PlaceSearchRequest request = PlaceSearchRequest.builder()
				.sortBy(PlaceSearchRequest.SortBy.CREATED_AT)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.size(size != null ? size : 10)
				.isActive(true)
				.approvalStatus("APPROVED")
				.build();
		
		PlaceSearchResponse response = searchPlacesUseCase.execute(request);
		return ResponseEntity.ok(response);
	}
	
	/**
	 * 검색 결과 개수 조회 API
	 */
	@PostMapping("/count")
	@Operation(summary = "검색 결과 개수 조회", description = "검색 조건에 맞는 전체 결과 수를 반환합니다")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	public ResponseEntity<CountResponse> countSearchResults(
			@RequestBody PlaceSearchRequest request
	) {
		Long count = searchRepository.countSearchResults(request);
		return ResponseEntity.ok(new CountResponse(count));
	}
	
	/**
	 * 공간 배치 상세 조회 API
	 * <p>
	 * 설계 결정:
	 * - POST 메서드 사용: Request Body로 다수의 ID 전달
	 * - 최대 50개 제한: 성능 최적화 및 메모리 관리
	 * - 부분 성공 패턴: 일부 ID가 존재하지 않아도 정상 응답
	 * - Gateway 친화적 구조: 단순한 results/failed 구조
	 *
	 * @param request 조회할 placeId 목록
	 * @return 조회된 공간 정보와 실패한 ID 목록
	 */
	@PostMapping("/batch/details")
	@Operation(
			summary = "공간 배치 상세 조회",
			description = "여러 공간의 상세 정보를 한 번에 조회합니다. 최대 50개까지 조회 가능하며, 존재하지 않는 ID는 failed 필드에 반환됩니다."
	)
	@ApiResponse(responseCode = "200", description = "조회 성공 (부분 실패 포함)")
	@ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 목록, 개수 초과 등)")
	public ResponseEntity<PlaceBatchDetailResponse> getPlaceDetailsBatch(
			@Valid @RequestBody PlaceBatchDetailRequest request
	) {
		log.info("배치 상세 조회 요청 - placeId 개수: {}", request.getPlaceIds().size());
		
		PlaceBatchDetailResponse response = getPlaceDetailsBatchUseCase.execute(request);
		
		log.info("배치 상세 조회 완료 - 성공: {}, 실패: {}",
				response.getSuccessCount(),
				response.getFailed() != null ? response.getFailed().size() : 0);
		
		return ResponseEntity.ok(response);
	}
}
