package com.teambind.placeinfoserver.place.fixture;

import com.teambind.placeinfoserver.place.domain.enums.ParkingType;
import com.teambind.placeinfoserver.place.dto.request.*;

import java.util.List;

/**
 * 테스트용 Request DTO 생성 팩토리
 */
public class PlaceRequestFactory {
	
	/**
	 * 기본 PlaceRegisterRequest 생성
	 */
	public static PlaceRegisterRequest createPlaceRegisterRequest() {
		return PlaceRegisterRequest.builder()
				.placeOwnerId("test_user_123")
				.placeName("테스트 연습실")
				.description("테스트용 연습실 설명입니다.")
				.category("연습실")
				.placeType("음악")
				.contact(createPlaceContactRequest())
				.location(createPlaceLocationRequest())
				.parking(createPlaceParkingRequest())
				.build();
	}
	
	/**
	 * PlaceContactRequest 생성
	 */
	private static PlaceContactRequest createPlaceContactRequest() {
		return PlaceContactRequest.builder()
				.contact("02-1234-5678")
				.email("test@example.com")
				.build();
	}
	
	/**
	 * PlaceLocationRequest 생성
	 */
	private static PlaceLocationRequest createPlaceLocationRequest() {
		// 수동 입력 주소 데이터
		AddressRequest addressData = AddressRequest.builder()
				.province("서울특별시")
				.city("강남구")
				.district("역삼동")
				.fullAddress("서울특별시 강남구 역삼동 123-45")
				.addressDetail("테스트빌딩 5층")
				.postalCode("06234")
				.build();
		
		return PlaceLocationRequest.builder()
				.from(com.teambind.placeinfoserver.place.domain.enums.AddressSource.MANUAL)
				.addressData(addressData)
				.latitude(37.4979)
				.longitude(127.0276)
				.locationGuide("지하철 2호선 역삼역 3번 출구에서 도보 5분")
				.build();
	}
	
	/**
	 * PlaceParkingRequest 생성
	 */
	private static PlaceParkingUpdateRequest createPlaceParkingRequest() {
		return PlaceParkingUpdateRequest.builder()
				.available(true)
				.parkingType(ParkingType.FREE)
				.description("건물 내 무료 주차 가능 (2시간)")
				.build();
	}
	
	/**
	 * 기본 PlaceSearchRequest 생성 (검색 조건 없음)
	 */
	public static PlaceSearchRequest createBasicSearchRequest() {
		return PlaceSearchRequest.builder()
				.size(10)
				.sortBy(PlaceSearchRequest.SortBy.CREATED_AT)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.isActive(true)
				.approvalStatus("APPROVED")
				.build();
	}
	
	/**
	 * 키워드 검색 PlaceSearchRequest 생성
	 */
	public static PlaceSearchRequest createKeywordSearchRequest(String keyword) {
		return PlaceSearchRequest.builder()
				.keyword(keyword)
				.size(10)
				.sortBy(PlaceSearchRequest.SortBy.RATING)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.isActive(true)
				.approvalStatus("APPROVED")
				.build();
	}
	
	/**
	 * 위치 기반 검색 PlaceSearchRequest 생성
	 */
	public static PlaceSearchRequest createLocationSearchRequest(double latitude, double longitude, int radius) {
		return PlaceSearchRequest.builder()
				.latitude(latitude)
				.longitude(longitude)
				.radiusInMeters(radius)
				.size(10)
				.sortBy(PlaceSearchRequest.SortBy.DISTANCE)
				.sortDirection(PlaceSearchRequest.SortDirection.ASC)
				.isActive(true)
				.approvalStatus("APPROVED")
				.build();
	}
	
	/**
	 * 지역 필터링 PlaceSearchRequest 생성
	 */
	public static PlaceSearchRequest createRegionSearchRequest(String province, String city, String district) {
		return PlaceSearchRequest.builder()
				.province(province)
				.city(city)
				.district(district)
				.size(10)
				.sortBy(PlaceSearchRequest.SortBy.CREATED_AT)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.isActive(true)
				.approvalStatus("APPROVED")
				.build();
	}
	
	/**
	 * 주차 가능 필터링 PlaceSearchRequest 생성
	 */
	public static PlaceSearchRequest createParkingSearchRequest(boolean parkingAvailable) {
		return PlaceSearchRequest.builder()
				.parkingAvailable(parkingAvailable)
				.size(10)
				.sortBy(PlaceSearchRequest.SortBy.RATING)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.isActive(true)
				.approvalStatus("APPROVED")
				.build();
	}
	
	/**
	 * 카테고리 필터링 PlaceSearchRequest 생성
	 */
	public static PlaceSearchRequest createCategorySearchRequest(String category) {
		return PlaceSearchRequest.builder()
				.category(category)
				.size(10)
				.sortBy(PlaceSearchRequest.SortBy.REVIEW_COUNT)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.isActive(true)
				.approvalStatus("APPROVED")
				.build();
	}
	
	/**
	 * 키워드 태그 검색 PlaceSearchRequest 생성
	 */
	public static PlaceSearchRequest createKeywordTagSearchRequest(List<Long> keywordIds) {
		return PlaceSearchRequest.builder()
				.keywordIds(keywordIds)
				.size(10)
				.sortBy(PlaceSearchRequest.SortBy.RATING)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.isActive(true)
				.approvalStatus("APPROVED")
				.build();
	}
	
	/**
	 * 복합 조건 검색 PlaceSearchRequest 생성
	 */
	public static PlaceSearchRequest createComplexSearchRequest(
			String keyword,
			String category,
			Boolean parkingAvailable,
			String province,
			String city
	) {
		return PlaceSearchRequest.builder()
				.keyword(keyword)
				.category(category)
				.parkingAvailable(parkingAvailable)
				.province(province)
				.city(city)
				.size(10)
				.sortBy(PlaceSearchRequest.SortBy.RATING)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.isActive(true)
				.approvalStatus("APPROVED")
				.build();
	}
	
	/**
	 * 커서 기반 페이징 PlaceSearchRequest 생성
	 */
	public static PlaceSearchRequest createCursorSearchRequest(String cursor, int size) {
		return PlaceSearchRequest.builder()
				.cursor(cursor)
				.size(size)
				.sortBy(PlaceSearchRequest.SortBy.CREATED_AT)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.isActive(true)
				.approvalStatus("APPROVED")
				.build();
	}
	
	/**
	 * 커스텀 PlaceSearchRequest 빌더
	 */
	public static PlaceSearchRequest.PlaceSearchRequestBuilder searchRequestBuilder() {
		return PlaceSearchRequest.builder()
				.size(10)
				.sortBy(PlaceSearchRequest.SortBy.CREATED_AT)
				.sortDirection(PlaceSearchRequest.SortDirection.DESC)
				.isActive(true)
				.approvalStatus("APPROVED");
	}
}
