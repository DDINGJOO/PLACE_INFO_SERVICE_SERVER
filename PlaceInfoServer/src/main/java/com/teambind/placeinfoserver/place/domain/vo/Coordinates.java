package com.teambind.placeinfoserver.place.domain.vo;

import com.teambind.placeinfoserver.place.common.exception.CustomException;
import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 좌표 Value Object
 * 위도(latitude)와 경도(longitude)를 하나의 개념으로 묶어 관리
 * <p>
 * 불변 객체로 설계되어 안전하게 사용 가능
 * WGS84 좌표계 사용 (SRID 4326)
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Coordinates {
	
	/**
	 * 위도 (-90 ~ 90)
	 */
	@Column(name = "latitude", nullable = false)
	private Double latitude;
	
	/**
	 * 경도 (-180 ~ 180)
	 */
	@Column(name = "longitude", nullable = false)
	private Double longitude;
	
	/**
	 * Private 생성자
	 * 정적 팩토리 메서드를 통해서만 생성 가능
	 */
	private Coordinates(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**
	 * 정적 팩토리 메서드
	 * 좌표 유효성 검증 포함
	 *
	 * @param latitude  위도
	 * @param longitude 경도
	 * @return 유효한 Coordinates 객체
	 * @throws CustomException 좌표가 유효하지 않은 경우
	 */
	public static Coordinates of(Double latitude, Double longitude) {
		validateLatitude(latitude);
		validateLongitude(longitude);
		return new Coordinates(latitude, longitude);
	}
	
	/**
	 * 위도 유효성 검증
	 */
	private static void validateLatitude(Double latitude) {
		if (latitude == null) {
			throw new CustomException(ErrorCode.LOCATION_INVALID_COORDINATES);
		}
		if (latitude < -90.0 || latitude > 90.0) {
			throw new CustomException(ErrorCode.LOCATION_LATITUDE_OUT_OF_RANGE);
		}
	}
	
	/**
	 * 경도 유효성 검증
	 */
	private static void validateLongitude(Double longitude) {
		if (longitude == null) {
			throw new CustomException(ErrorCode.LOCATION_INVALID_COORDINATES);
		}
		if (longitude < -180.0 || longitude > 180.0) {
			throw new CustomException(ErrorCode.LOCATION_LONGITUDE_OUT_OF_RANGE);
		}
	}
	
	/**
	 * 두 좌표 간의 거리 계산 (Haversine 공식)
	 *
	 * @param other 다른 좌표
	 * @return 거리 (미터 단위)
	 */
	public double distanceTo(Coordinates other) {
		Objects.requireNonNull(other, "다른 좌표는 null일 수 없습니다");
		
		final int EARTH_RADIUS_KM = 6371;
		
		double lat1Rad = Math.toRadians(this.latitude);
		double lat2Rad = Math.toRadians(other.latitude);
		double deltaLat = Math.toRadians(other.latitude - this.latitude);
		double deltaLon = Math.toRadians(other.longitude - this.longitude);
		
		double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
				+ Math.cos(lat1Rad) * Math.cos(lat2Rad)
				* Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
		
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		
		return EARTH_RADIUS_KM * c * 1000; // 미터로 변환
	}
	
	/**
	 * 좌표가 유효한지 확인
	 */
	public boolean isValid() {
		return latitude != null && longitude != null
				&& latitude >= -90.0 && latitude <= 90.0
				&& longitude >= -180.0 && longitude <= 180.0;
	}
	
	/**
	 * 문자열 표현
	 */
	@Override
	public String toString() {
		return String.format("Coordinates(%.6f, %.6f)", latitude, longitude);
	}
}
