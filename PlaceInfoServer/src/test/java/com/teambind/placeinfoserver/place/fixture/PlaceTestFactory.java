package com.teambind.placeinfoserver.place.fixture;

import com.teambind.placeinfoserver.place.domain.entity.*;
import com.teambind.placeinfoserver.place.domain.enums.ApprovalStatus;
import com.teambind.placeinfoserver.place.domain.enums.ParkingType;
import com.teambind.placeinfoserver.place.domain.enums.RegistrationStatus;
import com.teambind.placeinfoserver.place.domain.vo.Address;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 테스트용 엔티티 생성 팩토리
 * 각 테스트에서 객체를 직접 생성하지 않고 이 팩토리를 사용하여 일관성 유지
 */
public class PlaceTestFactory {
	
	private static final GeometryFactory geometryFactory = new GeometryFactory();
	private static int sequenceNumber = 1;
	
	/**
	 * 기본 PlaceInfo 생성 (모든 연관 엔티티 포함)
	 */
	public static PlaceInfo createPlaceInfo() {
		Long placeId = generatePlaceId();
		
		PlaceInfo placeInfo = PlaceInfo.builder()
				.id(placeId)  // Long 타입 ID
				.userId("user_" + sequenceNumber)
				.placeName("테스트 연습실 " + sequenceNumber)
				.description("테스트용 연습실 설명입니다.")
				.category("연습실")
				.placeType("음악")
				.ratingAverage(4.5)
				.reviewCount(10)
				.isActive(true)
				.approvalStatus(ApprovalStatus.APPROVED)
				.images(new ArrayList<>())
				.build();
		
		// 연관 엔티티 설정
		placeInfo.setLocation(createPlaceLocation(placeInfo));
		placeInfo.setContact(createPlaceContact(placeInfo));
		placeInfo.setParking(createPlaceParking(placeInfo));
		
		sequenceNumber++;
		return placeInfo;
	}
	
	/**
	 * 특정 위치의 PlaceInfo 생성 (위치 기반 검색 테스트용)
	 */
	public static PlaceInfo createPlaceInfoWithLocation(String placeName, double latitude, double longitude) {
		Long placeId = generatePlaceId();
		
		PlaceInfo placeInfo = PlaceInfo.builder()
				.id(placeId)  // Long 타입 ID
				.userId("user_" + sequenceNumber)
				.placeName(placeName)
				.description("위치 기반 테스트용 연습실입니다.")
				.category("연습실")
				.placeType("음악")
				.ratingAverage(4.0)
				.reviewCount(5)
				.isActive(true)
				.approvalStatus(ApprovalStatus.APPROVED)
				.images(new ArrayList<>())
				.build();
		
		// 특정 위치로 설정
		placeInfo.setLocation(createPlaceLocationWithCoordinates(placeInfo, latitude, longitude));
		placeInfo.setContact(createPlaceContact(placeInfo));
		placeInfo.setParking(createPlaceParking(placeInfo));
		
		sequenceNumber++;
		return placeInfo;
	}
	
	/**
	 * 승인 대기 상태의 PlaceInfo 생성
	 */
	public static PlaceInfo createPendingPlaceInfo() {
		PlaceInfo placeInfo = createPlaceInfo();
		placeInfo.setApprovalStatus(ApprovalStatus.PENDING);
		return placeInfo;
	}
	
	/**
	 * 거부된 PlaceInfo 생성
	 */
	public static PlaceInfo createRejectedPlaceInfo() {
		PlaceInfo placeInfo = createPlaceInfo();
		placeInfo.setApprovalStatus(ApprovalStatus.REJECTED);
		return placeInfo;
	}
	
	/**
	 * 비활성화된 PlaceInfo 생성
	 */
	public static PlaceInfo createInactivePlaceInfo() {
		PlaceInfo placeInfo = createPlaceInfo();
		placeInfo.setIsActive(false);
		return placeInfo;
	}
	
	/**
	 * 커스텀 빌더로 PlaceInfo 생성
	 */
	public static PlaceInfoBuilder builder() {
		return new PlaceInfoBuilder();
	}
	
	/**
	 * PlaceLocation 생성 (기본 위치: 서울 강남)
	 */
	public static PlaceLocation createPlaceLocation(PlaceInfo placeInfo) {
		return createPlaceLocationWithCoordinates(placeInfo, 37.4979, 127.0276);
	}
	
	/**
	 * PlaceLocation 생성 (특정 좌표)
	 */
	public static PlaceLocation createPlaceLocationWithCoordinates(PlaceInfo placeInfo, double latitude, double longitude) {
		Address address = Address.builder()
				.province("서울특별시")
				.city("강남구")
				.district("역삼동")
				.fullAddress("서울특별시 강남구 역삼동 123-45")
				.addressDetail("테스트빌딩 5층")
				.postalCode("06234")
				.build();
		
		Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
		point.setSRID(4326);
		
		return PlaceLocation.builder()
				.placeInfo(placeInfo)
				.address(address)
				.coordinates(point)
				.latitude(latitude)
				.longitude(longitude)
				.locationGuide("지하철 2호선 역삼역 3번 출구에서 도보 5분")
				.build();
	}
	
	/**
	 * PlaceContact 생성
	 */
	public static PlaceContact createPlaceContact(PlaceInfo placeInfo) {
		return PlaceContact.builder()
				.placeInfo(placeInfo)
				.contact("02-1234-5678")
				.build();
	}
	
	/**
	 * PlaceParking 생성 (주차 가능)
	 */
	public static PlaceParking createPlaceParking(PlaceInfo placeInfo) {
		return PlaceParking.builder()
				.placeInfo(placeInfo)
				.available(true)
				.parkingType(ParkingType.FREE)
				.description("건물 내 무료 주차 가능 (2시간)")
				.build();
	}
	
	/**
	 * PlaceParking 생성 (주차 불가)
	 */
	public static PlaceParking createNoParkingParking(PlaceInfo placeInfo) {
		return PlaceParking.builder()
				.placeInfo(placeInfo)
				.available(false)
				.build();
	}
	
	/**
	 * PlaceImage 생성
	 */
	public static PlaceImage createPlaceImage(PlaceInfo placeInfo, int order) {
		return PlaceImage.builder()
				.id("img_" + UUID.randomUUID().toString().substring(0, 8))
				.placeInfo(placeInfo)
				.imageUrl("https://example.com/images/test_" + order + ".jpg")
				.build();
	}
	
	/**
	 * Keyword 생성
	 */
	public static Keyword createKeyword(String name) {
		return Keyword.builder()
				.name(name)
				.type(com.teambind.placeinfoserver.place.domain.enums.KeywordType.INSTRUMENT_EQUIPMENT)
				.build();
	}
	
	/**
	 * PlaceInfo에 이미지 추가
	 */
	public static PlaceInfo withImages(PlaceInfo placeInfo, int imageCount) {
		List<PlaceImage> images = new ArrayList<>();
		for (int i = 1; i <= imageCount; i++) {
			images.add(createPlaceImage(placeInfo, i));
		}
		placeInfo.setImages(images);
		return placeInfo;
	}
	
	/**
	 * PlaceInfo에 키워드 추가
	 */
	public static PlaceInfo withKeywords(PlaceInfo placeInfo, List<Keyword> keywords) {
		placeInfo.setKeywords(new java.util.HashSet<>(keywords));
		return placeInfo;
	}
	
	/**
	 * Place ID 생성 (Long 타입 - 내부 사용)
	 * 테스트용 ID는 간단한 시퀀스 기반으로 생성
	 */
	private static Long generatePlaceId() {
		return System.currentTimeMillis() + sequenceNumber;
	}
	
	/**
	 * 시퀀스 번호 초기화 (테스트 간 독립성 보장)
	 */
	public static void resetSequence() {
		sequenceNumber = 1;
	}
	
	/**
	 * PlaceInfo 커스텀 빌더 클래스
	 */
	public static class PlaceInfoBuilder {
		private String userId = "test_user";
		private String placeName = "테스트 연습실";
		private String description = "테스트 설명";
		private String category = "연습실";
		private String placeType = "음악";
		private Double ratingAverage = 4.5;
		private Integer reviewCount = 10;
		private Boolean isActive = true;
		private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;
		private RegistrationStatus registrationStatus = RegistrationStatus.UNREGISTERED;
		private Double latitude = 37.4979;
		private Double longitude = 127.0276;
		private Boolean parkingAvailable = true;
		private ParkingType parkingType = ParkingType.FREE;
		
		public PlaceInfoBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}
		
		public PlaceInfoBuilder placeName(String placeName) {
			this.placeName = placeName;
			return this;
		}
		
		public PlaceInfoBuilder description(String description) {
			this.description = description;
			return this;
		}
		
		public PlaceInfoBuilder category(String category) {
			this.category = category;
			return this;
		}
		
		public PlaceInfoBuilder placeType(String placeType) {
			this.placeType = placeType;
			return this;
		}
		
		public PlaceInfoBuilder rating(Double rating) {
			this.ratingAverage = rating;
			return this;
		}
		
		public PlaceInfoBuilder reviewCount(Integer reviewCount) {
			this.reviewCount = reviewCount;
			return this;
		}
		
		public PlaceInfoBuilder active(Boolean active) {
			this.isActive = active;
			return this;
		}
		
		public PlaceInfoBuilder approvalStatus(ApprovalStatus status) {
			this.approvalStatus = status;
			return this;
		}

		public PlaceInfoBuilder registrationStatus(RegistrationStatus status) {
			this.registrationStatus = status;
			return this;
		}

		public PlaceInfoBuilder registered() {
			this.registrationStatus = RegistrationStatus.REGISTERED;
			return this;
		}

		public PlaceInfoBuilder unregistered() {
			this.registrationStatus = RegistrationStatus.UNREGISTERED;
			return this;
		}

		public PlaceInfoBuilder location(Double latitude, Double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
			return this;
		}
		
		public PlaceInfoBuilder parking(Boolean available, ParkingType type) {
			this.parkingAvailable = available;
			this.parkingType = type;
			return this;
		}
		
		public PlaceInfo build() {
			Long placeId = generatePlaceId();
			
			PlaceInfo placeInfo = PlaceInfo.builder()
					.id(placeId)  // Long 타입 ID
					.userId(userId)
					.placeName(placeName)
					.description(description)
					.category(category)
					.placeType(placeType)
					.ratingAverage(ratingAverage)
					.reviewCount(reviewCount)
					.isActive(isActive)
					.approvalStatus(approvalStatus)
					.registrationStatus(registrationStatus)
					.images(new ArrayList<>())
					.build();
			
			placeInfo.setLocation(createPlaceLocationWithCoordinates(placeInfo, latitude, longitude));
			placeInfo.setContact(createPlaceContact(placeInfo));
			
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(parkingAvailable)
					.parkingType(parkingAvailable ? parkingType : null)
					.build();
			placeInfo.setParking(parking);
			
			sequenceNumber++;
			return placeInfo;
		}
	}
}
