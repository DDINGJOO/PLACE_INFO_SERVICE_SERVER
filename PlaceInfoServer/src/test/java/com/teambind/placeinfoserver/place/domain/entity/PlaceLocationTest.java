package com.teambind.placeinfoserver.place.domain.entity;

import com.teambind.placeinfoserver.place.common.util.geometry.GeometryUtil;
import com.teambind.placeinfoserver.place.domain.vo.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PlaceLocation 엔티티 테스트")
class PlaceLocationTest {
	
	@Nested
	@DisplayName("엔티티 생성 테스트")
	class CreateTest {
		
		@Test
		@DisplayName("정상: 빌더로 PlaceLocation 생성")
		void createPlaceLocationWithBuilder() {
			// given
			Address address = Address.builder()
					.province("서울특별시")
					.city("강남구")
					.district("역삼동")
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.postalCode("06234")
					.build();
			
			// when
			PlaceLocation location = PlaceLocation.builder()
					.address(address)
					.latitude(37.5665)
					.longitude(126.9780)
					.locationGuide("2호선 강남역 3번 출구에서 도보 5분")
					.build();
			
			// then
			assertThat(location).isNotNull();
			assertThat(location.getAddress()).isEqualTo(address);
			assertThat(location.getLatitude()).isEqualTo(37.5665);
			assertThat(location.getLongitude()).isEqualTo(126.9780);
			assertThat(location.getLocationGuide()).isEqualTo("2호선 강남역 3번 출구에서 도보 5분");
		}
		
		@Test
		@DisplayName("정상: 최소한의 정보로 생성")
		void createPlaceLocationWithMinimalInfo() {
			// when
			PlaceLocation location = PlaceLocation.builder().build();
			
			// then
			assertThat(location).isNotNull();
			assertThat(location.getAddress()).isNull();
			assertThat(location.getLatitude()).isNull();
			assertThat(location.getLongitude()).isNull();
		}
	}
	
	@Nested
	@DisplayName("좌표 설정 테스트 - Point 객체")
	class CoordinatesWithPointTest {
		
		@Test
		@DisplayName("정상: Point 객체로 좌표 설정")
		void setCoordinatesWithPoint() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			Point point = GeometryUtil.createPoint(37.5665, 126.9780);
			
			// when
			location.setCoordinates(point);
			
			// then
			assertThat(location.getCoordinates()).isEqualTo(point);
			assertThat(location.getLatitude()).isEqualTo(37.5665);
			assertThat(location.getLongitude()).isEqualTo(126.9780);
		}
		
		@Test
		@DisplayName("정상: Point 객체로 좌표 설정 시 위도/경도 자동 업데이트")
		void setCoordinates_AutoUpdatesLatLng() {
			// given
			PlaceLocation location = PlaceLocation.builder()
					.latitude(0.0)
					.longitude(0.0)
					.build();
			Point point = GeometryUtil.createPoint(37.5665, 126.9780);
			
			// when
			location.setCoordinates(point);
			
			// then
			assertThat(location.getLatitude()).isEqualTo(37.5665);
			assertThat(location.getLongitude()).isEqualTo(126.9780);
		}
		
		@Test
		@DisplayName("엣지: null Point 설정")
		void setCoordinates_WithNull() {
			// given
			PlaceLocation location = PlaceLocation.builder()
					.latitude(37.5665)
					.longitude(126.9780)
					.build();
			
			// when
			location.setCoordinates(null);
			
			// then
			assertThat(location.getCoordinates()).isNull();
			// 위도/경도는 기존 값 유지 (null Point일 경우 업데이트 안함)
			assertThat(location.getLatitude()).isEqualTo(37.5665);
			assertThat(location.getLongitude()).isEqualTo(126.9780);
		}
	}
	
	@Nested
	@DisplayName("좌표 설정 테스트 - 위도/경도")
	class CoordinatesWithLatLngTest {
		
		@Test
		@DisplayName("정상: 위도/경도로 좌표 설정")
		void setLatLng() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when
			location.setLatLng(37.5665, 126.9780);
			
			// then
			assertThat(location.getLatitude()).isEqualTo(37.5665);
			assertThat(location.getLongitude()).isEqualTo(126.9780);
			assertThat(location.getCoordinates()).isNotNull();
			assertThat(location.getCoordinates().getY()).isEqualTo(37.5665);
			assertThat(location.getCoordinates().getX()).isEqualTo(126.9780);
		}
		
		@Test
		@DisplayName("엣지: 위도 최소값 (-90)")
		void setLatLng_WithMinLatitude() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when
			location.setLatLng(-90.0, 0.0);
			
			// then
			assertThat(location.getLatitude()).isEqualTo(-90.0);
			assertThat(location.getLongitude()).isEqualTo(0.0);
		}
		
		@Test
		@DisplayName("엣지: 위도 최대값 (90)")
		void setLatLng_WithMaxLatitude() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when
			location.setLatLng(90.0, 0.0);
			
			// then
			assertThat(location.getLatitude()).isEqualTo(90.0);
			assertThat(location.getLongitude()).isEqualTo(0.0);
		}
		
		@Test
		@DisplayName("엣지: 경도 최소값 (-180)")
		void setLatLng_WithMinLongitude() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when
			location.setLatLng(0.0, -180.0);
			
			// then
			assertThat(location.getLatitude()).isEqualTo(0.0);
			assertThat(location.getLongitude()).isEqualTo(-180.0);
		}
		
		@Test
		@DisplayName("엣지: 경도 최대값 (180)")
		void setLatLng_WithMaxLongitude() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when
			location.setLatLng(0.0, 180.0);
			
			// then
			assertThat(location.getLatitude()).isEqualTo(0.0);
			assertThat(location.getLongitude()).isEqualTo(180.0);
		}
		
		@Test
		@DisplayName("엣지: 0,0 좌표 (적도와 본초자오선 교차점)")
		void setLatLng_WithZeroZero() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when
			location.setLatLng(0.0, 0.0);
			
			// then
			assertThat(location.getLatitude()).isEqualTo(0.0);
			assertThat(location.getLongitude()).isEqualTo(0.0);
		}
		
		@Test
		@DisplayName("예외: 위도 범위 초과 (> 90)")
		void setLatLng_WithLatitudeAbove90() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when & then
			assertThatThrownBy(() -> location.setLatLng(90.1, 0.0))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("위도는 -90 ~ 90 사이여야 합니다");
		}
		
		@Test
		@DisplayName("예외: 위도 범위 초과 (< -90)")
		void setLatLng_WithLatitudeBelow90() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when & then
			assertThatThrownBy(() -> location.setLatLng(-90.1, 0.0))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("위도는 -90 ~ 90 사이여야 합니다");
		}
		
		@Test
		@DisplayName("예외: 경도 범위 초과 (> 180)")
		void setLatLng_WithLongitudeAbove180() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when & then
			assertThatThrownBy(() -> location.setLatLng(0.0, 180.1))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("경도는 -180 ~ 180 사이여야 합니다");
		}
		
		@Test
		@DisplayName("예외: 경도 범위 초과 (< -180)")
		void setLatLng_WithLongitudeBelow180() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when & then
			assertThatThrownBy(() -> location.setLatLng(0.0, -180.1))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("경도는 -180 ~ 180 사이여야 합니다");
		}
		
		@Test
		@DisplayName("예외: 위도와 경도 모두 범위 초과")
		void setLatLng_WithBothOutOfRange() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when & then
			assertThatThrownBy(() -> location.setLatLng(100.0, 200.0))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("위도는 -90 ~ 90 사이여야 합니다");
		}
	}
	
	@Nested
	@DisplayName("주소 설정 테스트")
	class AddressTest {
		
		@Test
		@DisplayName("정상: 주소 설정")
		void setAddress() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			Address address = Address.builder()
					.province("서울특별시")
					.city("강남구")
					.district("역삼동")
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.postalCode("06234")
					.build();
			
			// when
			location.setAddress(address);
			
			// then
			assertThat(location.getAddress()).isEqualTo(address);
		}
		
		@Test
		@DisplayName("엣지: null 주소 설정")
		void setAddress_WithNull() {
			// given
			PlaceLocation location = PlaceLocation.builder()
					.address(Address.builder()
							.fullAddress("서울특별시 강남구 역삼동 123-45")
							.build())
					.build();
			
			// when
			location.setAddress(null);
			
			// then
			assertThat(location.getAddress()).isNull();
		}
	}
	
	@Nested
	@DisplayName("연관관계 테스트")
	class RelationshipTest {
		
		@Test
		@DisplayName("정상: PlaceInfo와의 양방향 연관관계")
		void relationshipWithPlaceInfo() {
			// given
			PlaceInfo placeInfo = PlaceInfo.builder()
					.userId("user123")
					.placeName("테스트 연습실")
					.build();
			
			PlaceLocation location = PlaceLocation.builder()
					.latitude(37.5665)
					.longitude(126.9780)
					.build();
			
			// when
			placeInfo.setLocation(location);
			
			// then
			assertThat(location.getPlaceInfo()).isEqualTo(placeInfo);
			assertThat(placeInfo.getLocation()).isEqualTo(location);
		}
	}
	
	@Nested
	@DisplayName("실제 위치 좌표 테스트")
	class RealWorldCoordinatesTest {
		
		@Test
		@DisplayName("정상: 서울 시청 좌표")
		void seoulCityHall() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when
			location.setLatLng(37.5665, 126.9780);
			
			// then
			assertThat(location.getLatitude()).isEqualTo(37.5665);
			assertThat(location.getLongitude()).isEqualTo(126.9780);
		}
		
		@Test
		@DisplayName("정상: 강남역 좌표")
		void gangnamStation() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when
			location.setLatLng(37.4979, 127.0276);
			
			// then
			assertThat(location.getLatitude()).isEqualTo(37.4979);
			assertThat(location.getLongitude()).isEqualTo(127.0276);
		}
		
		@Test
		@DisplayName("정상: 제주도 좌표")
		void jejuIsland() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when
			location.setLatLng(33.4996, 126.5312);
			
			// then
			assertThat(location.getLatitude()).isEqualTo(33.4996);
			assertThat(location.getLongitude()).isEqualTo(126.5312);
		}
		
		@Test
		@DisplayName("정상: 부산 해운대 좌표")
		void haeundae() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when
			location.setLatLng(35.1586, 129.1603);
			
			// then
			assertThat(location.getLatitude()).isEqualTo(35.1586);
			assertThat(location.getLongitude()).isEqualTo(129.1603);
		}
	}
	
	@Nested
	@DisplayName("위치 안내 테스트")
	class LocationGuideTest {
		
		@Test
		@DisplayName("정상: 위치 안내 설정")
		void setLocationGuide() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			
			// when
			location.setLocationGuide("2호선 강남역 3번 출구에서 도보 5분");
			
			// then
			assertThat(location.getLocationGuide()).isEqualTo("2호선 강남역 3번 출구에서 도보 5분");
		}
		
		@Test
		@DisplayName("엣지: 위치 안내를 null로 설정")
		void setLocationGuide_WithNull() {
			// given
			PlaceLocation location = PlaceLocation.builder()
					.locationGuide("기존 안내")
					.build();
			
			// when
			location.setLocationGuide(null);
			
			// then
			assertThat(location.getLocationGuide()).isNull();
		}
		
		@Test
		@DisplayName("엣지: 긴 위치 안내 설정 (500자)")
		void setLocationGuide_WithLongText() {
			// given
			PlaceLocation location = PlaceLocation.builder().build();
			String longGuide = "가".repeat(500);
			
			// when
			location.setLocationGuide(longGuide);
			
			// then
			assertThat(location.getLocationGuide()).hasSize(500);
			assertThat(location.getLocationGuide()).isEqualTo(longGuide);
		}
	}
}
