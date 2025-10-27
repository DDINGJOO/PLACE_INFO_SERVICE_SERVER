package com.teambind.placeinfoserver.place.common.util.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * 지리 데이터 유틸리티
 * PostGIS Point 객체 생성 및 변환 기능 제공
 */
public class GeometryUtil {
	
	/**
	 * WGS84 좌표계 (SRID 4326)
	 */
	public static final int WGS84_SRID = 4326;
	
	/**
	 * GeometryFactory 싱글톤 인스턴스
	 */
	private static final GeometryFactory GEOMETRY_FACTORY =
			new GeometryFactory(new PrecisionModel(), WGS84_SRID);
	
	/**
	 * 위도/경도로 Point 객체 생성
	 *
	 * @param latitude  위도 (-90 ~ 90)
	 * @param longitude 경도 (-180 ~ 180)
	 * @return PostGIS Point 객체
	 * @throws IllegalArgumentException 위도/경도 범위가 유효하지 않은 경우
	 */
	public static Point createPoint(double latitude, double longitude) {
		validateLatitude(latitude);
		validateLongitude(longitude);
		
		// PostGIS에서는 경도(X), 위도(Y) 순서
		return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
	}
	
	/**
	 * 위도 유효성 검증
	 *
	 * @param latitude 위도
	 * @throws IllegalArgumentException 위도가 -90 ~ 90 범위를 벗어난 경우
	 */
	private static void validateLatitude(double latitude) {
		if (latitude < -90.0 || latitude > 90.0) {
			throw new IllegalArgumentException(
					String.format("위도는 -90 ~ 90 사이여야 합니다. 입력값: %.6f", latitude));
		}
	}
	
	/**
	 * 경도 유효성 검증
	 *
	 * @param longitude 경도
	 * @throws IllegalArgumentException 경도가 -180 ~ 180 범위를 벗어난 경우
	 */
	private static void validateLongitude(double longitude) {
		if (longitude < -180.0 || longitude > 180.0) {
			throw new IllegalArgumentException(
					String.format("경도는 -180 ~ 180 사이여야 합니다. 입력값: %.6f", longitude));
		}
	}
	
	/**
	 * Point 객체에서 위도 추출
	 *
	 * @param point PostGIS Point 객체
	 * @return 위도
	 */
	public static double getLatitude(Point point) {
		if (point == null) {
			throw new IllegalArgumentException("Point 객체가 null입니다.");
		}
		return point.getY();
	}
	
	/**
	 * Point 객체에서 경도 추출
	 *
	 * @param point PostGIS Point 객체
	 * @return 경도
	 */
	public static double getLongitude(Point point) {
		if (point == null) {
			throw new IllegalArgumentException("Point 객체가 null입니다.");
		}
		return point.getX();
	}
	
	/**
	 * GeometryFactory 인스턴스 반환
	 *
	 * @return GeometryFactory
	 */
	public static GeometryFactory getGeometryFactory() {
		return GEOMETRY_FACTORY;
	}
}
