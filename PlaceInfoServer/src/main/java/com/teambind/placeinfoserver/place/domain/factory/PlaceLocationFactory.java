package com.teambind.placeinfoserver.place.domain.factory;

import com.teambind.placeinfoserver.place.common.util.geometry.GeometryUtil;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.entity.PlaceLocation;
import com.teambind.placeinfoserver.place.domain.vo.Address;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

/**
 * PlaceLocation 엔티티 생성을 담당하는 Factory
 * <p>
 * 책임:
 * - PlaceLocation 엔티티의 일관된 생성
 * - 좌표 유효성 검증
 * - PostGIS Point 객체 생성
 * - PlaceInfo와의 연관관계 설정
 */
@Component
public class PlaceLocationFactory {
	
	/**
	 * PlaceLocation 생성
	 *
	 * @param placeInfo 연관된 PlaceInfo
	 * @param address   주소 정보 (Value Object)
	 * @param latitude  위도
	 * @param longitude 경도
	 * @return 생성된 PlaceLocation
	 * @throws IllegalArgumentException 좌표가 유효하지 않은 경우
	 */
	public PlaceLocation create(
			PlaceInfo placeInfo,
			Address address,
			Double latitude,
			Double longitude
	) {
		// 필수 값 검증
		validateRequiredFields(address, latitude, longitude);
		
		// 좌표 유효성 검증
		validateCoordinates(latitude, longitude);

		// PostGIS Point 생성
		Point coordinates = GeometryUtil.createPoint(latitude, longitude);
		
		return PlaceLocation.builder()
				.placeInfo(placeInfo)
				.address(address)
				.latitude(latitude)
				.longitude(longitude)
				.coordinates(coordinates)
				.build();
	}
	
	/**
	 * 주소 문자열로 PlaceLocation 생성
	 * (Address VO는 별도로 생성되어 전달되어야 함)
	 */
	public PlaceLocation createFromAddress(
			PlaceInfo placeInfo,
			String province,
			String city,
			String district,
			String fullAddress,
			String addressDetail,
			String postalCode,
			Double latitude,
			Double longitude
	) {
		// Address Value Object 생성
		Address address = Address.builder()
				.province(province)
				.city(city)
				.district(district)
				.fullAddress(fullAddress)
				.addressDetail(addressDetail)
				.postalCode(postalCode)
				.build();
		
		return create(placeInfo, address, latitude, longitude);
	}
	
	/**
	 * 필수 필드 검증
	 * address는 선택적 (좌표만 업데이트하는 경우 null 가능)
	 */
	private void validateRequiredFields(Address address, Double latitude, Double longitude) {
		if (latitude == null || longitude == null) {
			throw new IllegalArgumentException("위도와 경도는 필수입니다.");
		}
	}
	
	/**
	 * 좌표 유효성 검증
	 * - 위도: -90 ~ 90
	 * - 경도: -180 ~ 180
	 */
	private void validateCoordinates(Double latitude, Double longitude) {
		if (latitude < -90 || latitude > 90) {
			throw new IllegalArgumentException(
					String.format("위도는 -90에서 90 사이여야 합니다. 입력값: %f", latitude)
			);
		}
		
		if (longitude < -180 || longitude > 180) {
			throw new IllegalArgumentException(
					String.format("경도는 -180에서 180 사이여야 합니다. 입력값: %f", longitude)
			);
		}
		
		// 대한민국 영역 검증 (선택적 - 필요시 활성화)
		// validateKoreaRegion(latitude, longitude);
	}
	
	/**
	 * 대한민국 영역 검증 (선택적)
	 * 위도: 33.0 ~ 43.0
	 * 경도: 124.0 ~ 132.0
	 */
	@SuppressWarnings("unused")
	private void validateKoreaRegion(Double latitude, Double longitude) {
		if (latitude < 33.0 || latitude > 43.0 || longitude < 124.0 || longitude > 132.0) {
			throw new IllegalArgumentException(
					String.format("좌표가 대한민국 영역을 벗어났습니다. 위도: %f, 경도: %f", latitude, longitude)
			);
		}
	}
}
