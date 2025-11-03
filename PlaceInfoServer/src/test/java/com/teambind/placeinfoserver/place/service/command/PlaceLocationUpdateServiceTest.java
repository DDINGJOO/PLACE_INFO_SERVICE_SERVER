package com.teambind.placeinfoserver.place.service.command;

import com.teambind.placeinfoserver.place.common.exception.CustomException;
import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.enums.AddressSource;
import com.teambind.placeinfoserver.place.dto.request.AddressRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceLocationRequest;
import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PlaceLocationUpdateService 통합 테스트
 * 업체 위치 정보 업데이트 서비스 검증
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaceLocationUpdateServiceTest extends BaseIntegrationTest {
	
	@Autowired
	private PlaceLocationUpdateService locationUpdateService;
	
	@Autowired
	private PlaceInfoRepository placeInfoRepository;
	
	private PlaceInfo testPlace;
	
	@BeforeEach
	void setUp() {
		PlaceTestFactory.resetSequence();
		
		// 테스트 데이터 준비
		testPlace = PlaceTestFactory.createPlaceInfo();
		testPlace = placeInfoRepository.save(testPlace);
	}
	
	// 헬퍼 메서드
	private PlaceLocationRequest createLocationRequest(
			double latitude, double longitude,
			String province, String city, String district
	) {
		AddressRequest addressData = AddressRequest.builder()
				.province(province)
				.city(city)
				.district(district)
				.fullAddress(String.format("%s %s %s 123-45", province, city, district))
				.addressDetail("테스트빌딩")
				.postalCode("12345")
				.build();
		
		return PlaceLocationRequest.builder()
				.from(AddressSource.MANUAL)
				.addressData(addressData)
				.latitude(latitude)
				.longitude(longitude)
				.locationGuide("테스트 위치 안내")
				.build();
	}
	
	@Nested
	@DisplayName("좌표 시스템 테스트")
	class CoordinateSystemTest {
		
		@Test
		@Order(5)
		@DisplayName("PostGIS Point 객체 생성 - 성공")
		void updateLocation_PostGISPoint_Success() {
			// Given - 강남역 좌표
			PlaceLocationRequest request = createLocationRequest(
					37.4979, 127.0276, "서울특별시", "강남구", "역삼동"
			);
			
			// When
			locationUpdateService.updateLocation(String.valueOf(testPlace.getId()), request);
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getLocation().getCoordinates()).isNotNull();
			assertThat(updatedPlace.getLocation().getCoordinates().getX()).isEqualTo(127.0276);
			assertThat(updatedPlace.getLocation().getCoordinates().getY()).isEqualTo(37.4979);
			assertThat(updatedPlace.getLocation().getCoordinates().getSRID()).isEqualTo(4326);
		}
		
		@Test
		@Order(6)
		@DisplayName("위도/경도 필드 동기화 - 성공")
		void updateLocation_LatLngSync_Success() {
			// Given
			PlaceLocationRequest request = PlaceLocationRequest.builder()
					.latitude(35.1796)
					.longitude(129.0756)
					.build();
			
			// When
			locationUpdateService.updateLocation(String.valueOf(testPlace.getId()), request);
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getLocation().getLatitude()).isEqualTo(35.1796);
			assertThat(updatedPlace.getLocation().getLongitude()).isEqualTo(129.0756);
			assertThat(updatedPlace.getLocation().getCoordinates().getY()).isEqualTo(35.1796);
			assertThat(updatedPlace.getLocation().getCoordinates().getX()).isEqualTo(129.0756);
		}
	}
	
	@Nested
	@DisplayName("예외 처리 테스트")
	class ExceptionTest {
		
		@Test
		@Order(7)
		@DisplayName("존재하지 않는 업체 - 예외 발생")
		void updateLocation_PlaceNotFound_ThrowsException() {
			// Given
			PlaceLocationRequest request = createLocationRequest(
					37.5665, 126.9780, "서울특별시", "중구", "명동"
			);
			Long nonExistentId = 999999999L;

			// When & Then
			assertThatThrownBy(() -> locationUpdateService.updateLocation(String.valueOf(nonExistentId), request))
					.isInstanceOf(PlaceNotFoundException.class);
		}
	}
	
	@Nested
	@DisplayName("트랜잭션 및 영속성 테스트")
	class TransactionTest {
		
		@Test
		@Order(8)
		@DisplayName("더티 체킹으로 변경사항 자동 반영")
		void updateLocation_DirtyChecking_Success() {
			// Given
			double originalLat = testPlace.getLocation().getLatitude();
			PlaceLocationRequest request = PlaceLocationRequest.builder()
					.latitude(37.5000)
					.longitude(127.0500)
					.build();
			
			// When
			locationUpdateService.updateLocation(String.valueOf(testPlace.getId()), request);
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getLocation().getLatitude()).isNotEqualTo(originalLat);
			assertThat(updatedPlace.getLocation().getLatitude()).isEqualTo(37.5000);
		}
		
		@Test
		@Order(9)
		@DisplayName("여러 번 업데이트 - 마지막 상태 유지")
		void updateLocation_MultipleUpdates_LastStatePreserved() {
			// Given & When
			// 첫 번째 업데이트
			PlaceLocationRequest request1 = PlaceLocationRequest.builder()
					.latitude(37.5000)
					.longitude(127.0000)
					.build();
			locationUpdateService.updateLocation(String.valueOf(testPlace.getId()), request1);
			
			// 두 번째 업데이트
			PlaceLocationRequest request2 = PlaceLocationRequest.builder()
					.latitude(37.6000)
					.longitude(127.1000)
					.build();
			locationUpdateService.updateLocation(String.valueOf(testPlace.getId()), request2);
			
			// 세 번째 업데이트
			PlaceLocationRequest request3 = PlaceLocationRequest.builder()
					.latitude(37.7000)
					.longitude(127.2000)
					.build();
			locationUpdateService.updateLocation(String.valueOf(testPlace.getId()), request3);
			
			// Then
			PlaceInfo finalPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(finalPlace.getLocation().getLatitude()).isEqualTo(37.7000);
			assertThat(finalPlace.getLocation().getLongitude()).isEqualTo(127.2000);
		}
	}
	
	@Nested
	@DisplayName("실제 위치 좌표 테스트")
	class RealLocationTest {
		
		@Test
		@Order(10)
		@DisplayName("서울 시청 좌표 - 성공")
		void updateLocation_SeoulCityHall_Success() {
			// Given - 서울 시청 좌표
			PlaceLocationRequest request = createLocationRequest(
					37.5663, 126.9779, "서울특별시", "중구", "태평로1가"
			);
			
			// When
			locationUpdateService.updateLocation(String.valueOf(testPlace.getId()), request);
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getLocation().getLatitude()).isEqualTo(37.5663);
			assertThat(updatedPlace.getLocation().getLongitude()).isEqualTo(126.9779);
		}
		
		@Test
		@Order(11)
		@DisplayName("부산역 좌표 - 성공")
		void updateLocation_BusanStation_Success() {
			// Given - 부산역 좌표
			PlaceLocationRequest request = createLocationRequest(
					35.1159, 129.0412, "부산광역시", "동구", "초량동"
			);
			
			// When
			locationUpdateService.updateLocation(String.valueOf(testPlace.getId()), request);
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getLocation().getLatitude()).isEqualTo(35.1159);
			assertThat(updatedPlace.getLocation().getLongitude()).isEqualTo(129.0412);
			assertThat(updatedPlace.getLocation().getAddress().getProvince()).isEqualTo("부산광역시");
		}
		
		@Test
		@Order(12)
		@DisplayName("제주도 좌표 - 성공")
		void updateLocation_JejuIsland_Success() {
			// Given - 제주 시청 좌표
			PlaceLocationRequest request = createLocationRequest(
					33.4996, 126.5312, "제주특별자치도", "제주시", "연동"
			);
			
			// When
			locationUpdateService.updateLocation(String.valueOf(testPlace.getId()), request);
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getLocation().getLatitude()).isEqualTo(33.4996);
			assertThat(updatedPlace.getLocation().getLongitude()).isEqualTo(126.5312);
		}
	}
	
	@Nested
	@DisplayName("위치 정보 업데이트 테스트")
	class UpdateLocationTest {
		
		@Test
		@Order(1)
		@DisplayName("위치 정보 업데이트 - 성공")
		void updateLocation_Success() {
			// Given
			PlaceLocationRequest request = createLocationRequest(
					37.5665, 126.9780, "서울특별시", "중구", "명동"
			);
			
			// When
			String resultId = locationUpdateService.updateLocation(String.valueOf(testPlace.getId()), request);

			// Then
			assertThat(resultId).isEqualTo(String.valueOf(testPlace.getId()));
			
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getLocation()).isNotNull();
			assertThat(updatedPlace.getLocation().getLatitude()).isEqualTo(37.5665);
			assertThat(updatedPlace.getLocation().getLongitude()).isEqualTo(126.9780);
			assertThat(updatedPlace.getLocation().getAddress().getProvince()).isEqualTo("서울특별시");
			assertThat(updatedPlace.getLocation().getAddress().getCity()).isEqualTo("중구");
			assertThat(updatedPlace.getLocation().getAddress().getDistrict()).isEqualTo("명동");
		}
		
		@Test
		@Order(2)
		@DisplayName("좌표만 업데이트 - 성공")
		void updateLocation_CoordinatesOnly_Success() {
			// Given
			PlaceLocationRequest request = PlaceLocationRequest.builder()
					.latitude(37.5172)
					.longitude(127.0473)
					.build();
			
			// When
			String resultId = locationUpdateService.updateLocation(String.valueOf(testPlace.getId()), request);
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getLocation().getLatitude()).isEqualTo(37.5172);
			assertThat(updatedPlace.getLocation().getLongitude()).isEqualTo(127.0473);
			assertThat(updatedPlace.getLocation().getCoordinates()).isNotNull();
			assertThat(updatedPlace.getLocation().getCoordinates().getSRID()).isEqualTo(4326);
		}
		
		@Test
		@Order(3)
		@DisplayName("주소 정보만 업데이트 - 성공")
		void updateLocation_AddressOnly_Success() {
			// Given
			AddressRequest addressData = AddressRequest.builder()
					.province("경기도")
					.city("성남시")
					.district("분당구")
					.fullAddress("경기도 성남시 분당구 정자동 123-45")
					.addressDetail("테스트빌딩 10층")
					.postalCode("13500")
					.build();
			
			PlaceLocationRequest request = PlaceLocationRequest.builder()
					.from(AddressSource.MANUAL)
					.addressData(addressData)
					.locationGuide("지하철 신분당선 정자역 1번 출구")
					.build();
			
			// When
			String resultId = locationUpdateService.updateLocation(String.valueOf(testPlace.getId()), request);
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getLocation().getAddress()).isNotNull();
			assertThat(updatedPlace.getLocation().getAddress().getProvince()).isEqualTo("경기도");
			assertThat(updatedPlace.getLocation().getAddress().getCity()).isEqualTo("성남시");
			assertThat(updatedPlace.getLocation().getLocationGuide()).isEqualTo("지하철 신분당선 정자역 1번 출구");
		}
		
		@Test
		@Order(4)
		@DisplayName("위치 안내 정보 업데이트 - 성공")
		void updateLocation_LocationGuide_Success() {
			// Given
			PlaceLocationRequest request = PlaceLocationRequest.builder()
					.locationGuide("버스 정류장에서 도보 3분, 건물 1층")
					.build();
			
			// When
			String resultId = locationUpdateService.updateLocation(String.valueOf(testPlace.getId()), request);
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getLocation().getLocationGuide()).isEqualTo("버스 정류장에서 도보 3분, 건물 1층");
		}
	}
}
