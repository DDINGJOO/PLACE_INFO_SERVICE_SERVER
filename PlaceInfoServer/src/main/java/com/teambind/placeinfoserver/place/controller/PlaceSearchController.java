package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.controller.swagger.PlaceSearchControllerSwagger;
import com.teambind.placeinfoserver.place.dto.request.LocationSearchRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceBatchDetailRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceSearchRequest;
import com.teambind.placeinfoserver.place.dto.response.CountResponse;
import com.teambind.placeinfoserver.place.dto.response.PlaceBatchDetailResponse;
import com.teambind.placeinfoserver.place.dto.response.PlaceSearchResponse;
import com.teambind.placeinfoserver.place.repository.PlaceAdvancedSearchRepository;
import com.teambind.placeinfoserver.place.service.usecase.query.GetPlaceDetailsBatchUseCase;
import com.teambind.placeinfoserver.place.service.usecase.query.SearchPlacesUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/places/search")
@RequiredArgsConstructor
public class PlaceSearchController implements PlaceSearchControllerSwagger {
	
	// Query UseCases
	private final SearchPlacesUseCase searchPlacesUseCase;
	private final GetPlaceDetailsBatchUseCase getPlaceDetailsBatchUseCase;
	private final PlaceAdvancedSearchRepository searchRepository;
	
	@Override
	@GetMapping(produces = "application/json;charset=UTF-8")
	public ResponseEntity<PlaceSearchResponse> search(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String placeName,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) String placeType,
			@RequestParam(required = false) List<Long> keywordIds,
			@RequestParam(required = false) Boolean parkingAvailable,
			@RequestParam(required = false) Double latitude,
			@RequestParam(required = false) Double longitude,
			@RequestParam(defaultValue = "5000") Integer radius,
			@RequestParam(required = false) String province,
			@RequestParam(required = false) String city,
			@RequestParam(required = false) String district,
			@RequestParam(defaultValue = "DISTANCE") PlaceSearchRequest.SortBy sortBy,
			@RequestParam(defaultValue = "ASC") PlaceSearchRequest.SortDirection sortDirection,
			@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "20") Integer size,
			@RequestParam(required = false) String registrationStatus
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
				.registrationStatus(registrationStatus)
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
	
	@Override
	@PostMapping("/location")
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
				.registrationStatus(request.getRegistrationStatus())
				.build();
		
		log.info("위치 기반 검색: ({}, {}) 반경 {}m",
				request.getLatitude(), request.getLongitude(), request.getRadius());
		
		PlaceSearchResponse response = searchPlacesUseCase.execute(searchRequest);
		return ResponseEntity.ok(response);
	}
	
	@Override
	@GetMapping("/region")
	public ResponseEntity<PlaceSearchResponse> searchByRegion(
			@RequestParam String province,
			@RequestParam(required = false) String city,
			@RequestParam(required = false) String district,
			@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "20") Integer size,
			@RequestParam(required = false) String registrationStatus
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
				.registrationStatus(registrationStatus)
				.build();
		
		PlaceSearchResponse response = searchPlacesUseCase.execute(searchRequest);
		return ResponseEntity.ok(response);
	}
	
	@Override
	@GetMapping("/popular")
	public ResponseEntity<PlaceSearchResponse> getPopularPlaces(
			@RequestParam(defaultValue = "10") Integer size,
			@RequestParam(required = false) String registrationStatus
	) {
		log.info("인기 장소 조회: {} 건", size);
		
		PlaceSearchRequest request = PlaceSearchRequest.builder()
				.sortBy(PlaceSearchRequest.SortBy.RATING)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.size(size != null ? size : 10)
				.isActive(true)
				.approvalStatus("APPROVED")
				.registrationStatus(registrationStatus)
				.build();
		
		PlaceSearchResponse response = searchPlacesUseCase.execute(request);
		return ResponseEntity.ok(response);
	}
	
	@Override
	@GetMapping("/recent")
	public ResponseEntity<PlaceSearchResponse> getRecentPlaces(
			@RequestParam(defaultValue = "10") Integer size,
			@RequestParam(required = false) String registrationStatus
	) {
		log.info("최신 장소 조회: {} 건", size);
		
		PlaceSearchRequest request = PlaceSearchRequest.builder()
				.sortBy(PlaceSearchRequest.SortBy.CREATED_AT)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.size(size != null ? size : 10)
				.isActive(true)
				.approvalStatus("APPROVED")
				.registrationStatus(registrationStatus)
				.build();
		
		PlaceSearchResponse response = searchPlacesUseCase.execute(request);
		return ResponseEntity.ok(response);
	}
	
	@Override
	@PostMapping("/count")
	public ResponseEntity<CountResponse> countSearchResults(
			@RequestBody PlaceSearchRequest request
	) {
		Long count = searchRepository.countSearchResults(request);
		return ResponseEntity.ok(new CountResponse(count));
	}
	
	@Override
	@PostMapping("/batch/details")
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
