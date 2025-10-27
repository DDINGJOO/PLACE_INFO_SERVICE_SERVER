package com.teambind.placeinfoserver.place.utils.geometry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GeometryUtil 유틸리티 테스트")
class GeometryUtilTest {
	
	@Nested
	@DisplayName("Point 생성 테스트")
	class CreatePointTest {
		
		@Test
		@DisplayName("정상: 유효한 위도/경도로 Point 생성")
		void createPoint_WithValidCoordinates() {
			// given
			double latitude = 37.5665;
			double longitude = 126.9780;
			
			// when
			Point point = GeometryUtil.createPoint(latitude, longitude);
			
			// then
			assertThat(point).isNotNull();
			assertThat(point.getY()).isEqualTo(latitude);
			assertThat(point.getX()).isEqualTo(longitude);
			assertThat(point.getSRID()).isEqualTo(GeometryUtil.WGS84_SRID);
		}
		
		@Test
		@DisplayName("정상: 서울 시청 좌표로 Point 생성")
		void createPoint_SeoulCityHall() {
			// given
			double latitude = 37.5665;
			double longitude = 126.9780;
			
			// when
			Point point = GeometryUtil.createPoint(latitude, longitude);
			
			// then
			assertThat(point.getY()).isEqualTo(37.5665);
			assertThat(point.getX()).isEqualTo(126.9780);
		}
		
		@Test
		@DisplayName("엣지: 위도 최소값 (-90)으로 Point 생성")
		void createPoint_WithMinLatitude() {
			// given
			double latitude = -90.0;
			double longitude = 0.0;
			
			// when
			Point point = GeometryUtil.createPoint(latitude, longitude);
			
			// then
			assertThat(point.getY()).isEqualTo(-90.0);
			assertThat(point.getX()).isEqualTo(0.0);
		}
		
		@Test
		@DisplayName("엣지: 위도 최대값 (90)으로 Point 생성")
		void createPoint_WithMaxLatitude() {
			// given
			double latitude = 90.0;
			double longitude = 0.0;
			
			// when
			Point point = GeometryUtil.createPoint(latitude, longitude);
			
			// then
			assertThat(point.getY()).isEqualTo(90.0);
			assertThat(point.getX()).isEqualTo(0.0);
		}
		
		@Test
		@DisplayName("엣지: 경도 최소값 (-180)으로 Point 생성")
		void createPoint_WithMinLongitude() {
			// given
			double latitude = 0.0;
			double longitude = -180.0;
			
			// when
			Point point = GeometryUtil.createPoint(latitude, longitude);
			
			// then
			assertThat(point.getY()).isEqualTo(0.0);
			assertThat(point.getX()).isEqualTo(-180.0);
		}
		
		@Test
		@DisplayName("엣지: 경도 최대값 (180)으로 Point 생성")
		void createPoint_WithMaxLongitude() {
			// given
			double latitude = 0.0;
			double longitude = 180.0;
			
			// when
			Point point = GeometryUtil.createPoint(latitude, longitude);
			
			// then
			assertThat(point.getY()).isEqualTo(0.0);
			assertThat(point.getX()).isEqualTo(180.0);
		}
		
		@Test
		@DisplayName("엣지: 0,0 좌표로 Point 생성 (적도와 본초자오선 교차점)")
		void createPoint_WithZeroCoordinates() {
			// given
			double latitude = 0.0;
			double longitude = 0.0;
			
			// when
			Point point = GeometryUtil.createPoint(latitude, longitude);
			
			// then
			assertThat(point.getY()).isEqualTo(0.0);
			assertThat(point.getX()).isEqualTo(0.0);
		}
		
		@Test
		@DisplayName("예외: 위도가 90을 초과하는 경우")
		void createPoint_WithLatitudeAbove90() {
			// given
			double latitude = 90.1;
			double longitude = 0.0;
			
			// when & then
			assertThatThrownBy(() -> GeometryUtil.createPoint(latitude, longitude))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("위도는 -90 ~ 90 사이여야 합니다")
					.hasMessageContaining("90.100000");
		}
		
		@Test
		@DisplayName("예외: 위도가 -90 미만인 경우")
		void createPoint_WithLatitudeBelow90() {
			// given
			double latitude = -90.1;
			double longitude = 0.0;
			
			// when & then
			assertThatThrownBy(() -> GeometryUtil.createPoint(latitude, longitude))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("위도는 -90 ~ 90 사이여야 합니다")
					.hasMessageContaining("-90.100000");
		}
		
		@Test
		@DisplayName("예외: 경도가 180을 초과하는 경우")
		void createPoint_WithLongitudeAbove180() {
			// given
			double latitude = 0.0;
			double longitude = 180.1;
			
			// when & then
			assertThatThrownBy(() -> GeometryUtil.createPoint(latitude, longitude))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("경도는 -180 ~ 180 사이여야 합니다")
					.hasMessageContaining("180.100000");
		}
		
		@Test
		@DisplayName("예외: 경도가 -180 미만인 경우")
		void createPoint_WithLongitudeBelow180() {
			// given
			double latitude = 0.0;
			double longitude = -180.1;
			
			// when & then
			assertThatThrownBy(() -> GeometryUtil.createPoint(latitude, longitude))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("경도는 -180 ~ 180 사이여야 합니다")
					.hasMessageContaining("-180.100000");
		}
		
		@Test
		@DisplayName("예외: 위도와 경도가 모두 범위를 벗어난 경우")
		void createPoint_WithBothOutOfRange() {
			// given
			double latitude = 100.0;
			double longitude = 200.0;
			
			// when & then
			assertThatThrownBy(() -> GeometryUtil.createPoint(latitude, longitude))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("위도는 -90 ~ 90 사이여야 합니다");
		}
		
		@Test
		@DisplayName("엣지: 매우 정밀한 소수점 좌표")
		void createPoint_WithPreciseCoordinates() {
			// given
			double latitude = 37.123456789012345;
			double longitude = 126.987654321098765;
			
			// when
			Point point = GeometryUtil.createPoint(latitude, longitude);
			
			// then
			assertThat(point.getY()).isEqualTo(latitude);
			assertThat(point.getX()).isEqualTo(longitude);
		}
	}
	
	@Nested
	@DisplayName("위도 추출 테스트")
	class GetLatitudeTest {
		
		@Test
		@DisplayName("정상: Point에서 위도 추출")
		void getLatitude_FromPoint() {
			// given
			Point point = GeometryUtil.createPoint(37.5665, 126.9780);
			
			// when
			double latitude = GeometryUtil.getLatitude(point);
			
			// then
			assertThat(latitude).isEqualTo(37.5665);
		}
		
		@Test
		@DisplayName("예외: null Point에서 위도 추출")
		void getLatitude_FromNullPoint() {
			// when & then
			assertThatThrownBy(() -> GeometryUtil.getLatitude(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Point 객체가 null입니다");
		}
		
		@Test
		@DisplayName("정상: 다양한 위도 값 추출")
		void getLatitude_VariousValues() {
			// given
			Point point1 = GeometryUtil.createPoint(0.0, 0.0);
			Point point2 = GeometryUtil.createPoint(90.0, 0.0);
			Point point3 = GeometryUtil.createPoint(-90.0, 0.0);
			Point point4 = GeometryUtil.createPoint(45.5, 0.0);
			
			// when & then
			assertThat(GeometryUtil.getLatitude(point1)).isEqualTo(0.0);
			assertThat(GeometryUtil.getLatitude(point2)).isEqualTo(90.0);
			assertThat(GeometryUtil.getLatitude(point3)).isEqualTo(-90.0);
			assertThat(GeometryUtil.getLatitude(point4)).isEqualTo(45.5);
		}
	}
	
	@Nested
	@DisplayName("경도 추출 테스트")
	class GetLongitudeTest {
		
		@Test
		@DisplayName("정상: Point에서 경도 추출")
		void getLongitude_FromPoint() {
			// given
			Point point = GeometryUtil.createPoint(37.5665, 126.9780);
			
			// when
			double longitude = GeometryUtil.getLongitude(point);
			
			// then
			assertThat(longitude).isEqualTo(126.9780);
		}
		
		@Test
		@DisplayName("예외: null Point에서 경도 추출")
		void getLongitude_FromNullPoint() {
			// when & then
			assertThatThrownBy(() -> GeometryUtil.getLongitude(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Point 객체가 null입니다");
		}
		
		@Test
		@DisplayName("정상: 다양한 경도 값 추출")
		void getLongitude_VariousValues() {
			// given
			Point point1 = GeometryUtil.createPoint(0.0, 0.0);
			Point point2 = GeometryUtil.createPoint(0.0, 180.0);
			Point point3 = GeometryUtil.createPoint(0.0, -180.0);
			Point point4 = GeometryUtil.createPoint(0.0, 127.5);
			
			// when & then
			assertThat(GeometryUtil.getLongitude(point1)).isEqualTo(0.0);
			assertThat(GeometryUtil.getLongitude(point2)).isEqualTo(180.0);
			assertThat(GeometryUtil.getLongitude(point3)).isEqualTo(-180.0);
			assertThat(GeometryUtil.getLongitude(point4)).isEqualTo(127.5);
		}
	}
	
	@Nested
	@DisplayName("GeometryFactory 테스트")
	class GeometryFactoryTest {
		
		@Test
		@DisplayName("정상: GeometryFactory 인스턴스 반환")
		void getGeometryFactory() {
			// when
			GeometryFactory factory = GeometryUtil.getGeometryFactory();
			
			// then
			assertThat(factory).isNotNull();
			assertThat(factory.getSRID()).isEqualTo(GeometryUtil.WGS84_SRID);
		}
		
		@Test
		@DisplayName("정상: GeometryFactory는 싱글톤")
		void getGeometryFactory_IsSingleton() {
			// when
			GeometryFactory factory1 = GeometryUtil.getGeometryFactory();
			GeometryFactory factory2 = GeometryUtil.getGeometryFactory();
			
			// then
			assertThat(factory1).isSameAs(factory2);
		}
	}
	
	@Nested
	@DisplayName("SRID 테스트")
	class SRIDTest {
		
		@Test
		@DisplayName("정상: WGS84 SRID 값 확인")
		void wgs84SRIDValue() {
			// then
			assertThat(GeometryUtil.WGS84_SRID).isEqualTo(4326);
		}
		
		@Test
		@DisplayName("정상: 생성된 Point의 SRID 확인")
		void createdPoint_HasCorrectSRID() {
			// given
			Point point = GeometryUtil.createPoint(37.5665, 126.9780);
			
			// when
			int srid = point.getSRID();
			
			// then
			assertThat(srid).isEqualTo(4326);
			assertThat(srid).isEqualTo(GeometryUtil.WGS84_SRID);
		}
	}
	
	@Nested
	@DisplayName("실제 위치 좌표 테스트")
	class RealWorldCoordinatesTest {
		
		@Test
		@DisplayName("정상: 서울 주요 지역 좌표")
		void seoulLocations() {
			// given & when
			Point cityHall = GeometryUtil.createPoint(37.5665, 126.9780);
			Point gangnam = GeometryUtil.createPoint(37.4979, 127.0276);
			Point hongdae = GeometryUtil.createPoint(37.5563, 126.9233);
			
			// then
			assertThat(GeometryUtil.getLatitude(cityHall)).isEqualTo(37.5665);
			assertThat(GeometryUtil.getLongitude(cityHall)).isEqualTo(126.9780);
			
			assertThat(GeometryUtil.getLatitude(gangnam)).isEqualTo(37.4979);
			assertThat(GeometryUtil.getLongitude(gangnam)).isEqualTo(127.0276);
			
			assertThat(GeometryUtil.getLatitude(hongdae)).isEqualTo(37.5563);
			assertThat(GeometryUtil.getLongitude(hongdae)).isEqualTo(126.9233);
		}
		
		@Test
		@DisplayName("정상: 한국 주요 도시 좌표")
		void koreanCities() {
			// given & when
			Point seoul = GeometryUtil.createPoint(37.5665, 126.9780);
			Point busan = GeometryUtil.createPoint(35.1796, 129.0756);
			Point jeju = GeometryUtil.createPoint(33.4996, 126.5312);
			Point incheon = GeometryUtil.createPoint(37.4563, 126.7052);
			
			// then
			assertThat(seoul).isNotNull();
			assertThat(busan).isNotNull();
			assertThat(jeju).isNotNull();
			assertThat(incheon).isNotNull();
			
			assertThat(GeometryUtil.getLatitude(seoul)).isEqualTo(37.5665);
			assertThat(GeometryUtil.getLatitude(busan)).isEqualTo(35.1796);
			assertThat(GeometryUtil.getLatitude(jeju)).isEqualTo(33.4996);
			assertThat(GeometryUtil.getLatitude(incheon)).isEqualTo(37.4563);
		}
		
		@Test
		@DisplayName("정상: 세계 주요 도시 좌표")
		void worldCities() {
			// given & when
			Point newYork = GeometryUtil.createPoint(40.7128, -74.0060);
			Point london = GeometryUtil.createPoint(51.5074, -0.1278);
			Point tokyo = GeometryUtil.createPoint(35.6762, 139.6503);
			Point paris = GeometryUtil.createPoint(48.8566, 2.3522);
			
			// then
			assertThat(newYork).isNotNull();
			assertThat(london).isNotNull();
			assertThat(tokyo).isNotNull();
			assertThat(paris).isNotNull();
			
			// 뉴욕 (서반구 - 음수 경도)
			assertThat(GeometryUtil.getLongitude(newYork)).isNegative();
			
			// 런던 (본초자오선 근처 - 음수 경도)
			assertThat(GeometryUtil.getLongitude(london)).isNegative();
			
			// 도쿄 (동반구 - 양수 경도)
			assertThat(GeometryUtil.getLongitude(tokyo)).isPositive();
			
			// 파리 (동반구 - 양수 경도)
			assertThat(GeometryUtil.getLongitude(paris)).isPositive();
		}
	}
	
	@Nested
	@DisplayName("경계값 테스트")
	class BoundaryValueTest {
		
		@Test
		@DisplayName("엣지: 북극점 (위도 90)")
		void northPole() {
			// given & when
			Point northPole = GeometryUtil.createPoint(90.0, 0.0);
			
			// then
			assertThat(GeometryUtil.getLatitude(northPole)).isEqualTo(90.0);
		}
		
		@Test
		@DisplayName("엣지: 남극점 (위도 -90)")
		void southPole() {
			// given & when
			Point southPole = GeometryUtil.createPoint(-90.0, 0.0);
			
			// then
			assertThat(GeometryUtil.getLatitude(southPole)).isEqualTo(-90.0);
		}
		
		@Test
		@DisplayName("엣지: 날짜변경선 (경도 180)")
		void internationalDateLine_East() {
			// given & when
			Point dateLine = GeometryUtil.createPoint(0.0, 180.0);
			
			// then
			assertThat(GeometryUtil.getLongitude(dateLine)).isEqualTo(180.0);
		}
		
		@Test
		@DisplayName("엣지: 날짜변경선 (경도 -180)")
		void internationalDateLine_West() {
			// given & when
			Point dateLine = GeometryUtil.createPoint(0.0, -180.0);
			
			// then
			assertThat(GeometryUtil.getLongitude(dateLine)).isEqualTo(-180.0);
		}
		
		@Test
		@DisplayName("엣지: 본초자오선 (경도 0)")
		void primeMeridian() {
			// given & when
			Point primeMeridian = GeometryUtil.createPoint(51.4778, 0.0);
			
			// then
			assertThat(GeometryUtil.getLongitude(primeMeridian)).isEqualTo(0.0);
		}
		
		@Test
		@DisplayName("엣지: 적도 (위도 0)")
		void equator() {
			// given & when
			Point equator = GeometryUtil.createPoint(0.0, 100.0);
			
			// then
			assertThat(GeometryUtil.getLatitude(equator)).isEqualTo(0.0);
		}
	}
	
	@Nested
	@DisplayName("좌표 정확도 테스트")
	class CoordinatePrecisionTest {
		
		@Test
		@DisplayName("정상: Point 생성 후 추출한 좌표가 원본과 일치")
		void coordinateRoundTrip() {
			// given
			double originalLat = 37.5665;
			double originalLng = 126.9780;
			
			// when
			Point point = GeometryUtil.createPoint(originalLat, originalLng);
			double extractedLat = GeometryUtil.getLatitude(point);
			double extractedLng = GeometryUtil.getLongitude(point);
			
			// then
			assertThat(extractedLat).isEqualTo(originalLat);
			assertThat(extractedLng).isEqualTo(originalLng);
		}
		
		@Test
		@DisplayName("정상: 여러 번 Point 생성과 추출을 반복해도 값 유지")
		void multipleRoundTrips() {
			// given
			double originalLat = 37.5665;
			double originalLng = 126.9780;
			
			// when
			Point point1 = GeometryUtil.createPoint(originalLat, originalLng);
			double lat1 = GeometryUtil.getLatitude(point1);
			double lng1 = GeometryUtil.getLongitude(point1);
			
			Point point2 = GeometryUtil.createPoint(lat1, lng1);
			double lat2 = GeometryUtil.getLatitude(point2);
			double lng2 = GeometryUtil.getLongitude(point2);
			
			Point point3 = GeometryUtil.createPoint(lat2, lng2);
			double lat3 = GeometryUtil.getLatitude(point3);
			double lng3 = GeometryUtil.getLongitude(point3);
			
			// then
			assertThat(lat3).isEqualTo(originalLat);
			assertThat(lng3).isEqualTo(originalLng);
		}
	}
}
