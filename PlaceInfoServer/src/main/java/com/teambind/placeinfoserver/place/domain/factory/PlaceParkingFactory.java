package com.teambind.placeinfoserver.place.domain.factory;

import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.entity.PlaceParking;
import com.teambind.placeinfoserver.place.domain.enums.ParkingType;
import org.springframework.stereotype.Component;

/**
 * PlaceParking 엔티티 생성을 담당하는 Factory
 * <p>
 * 책임:
 * - PlaceParking 엔티티의 일관된 생성
 * - 주차 정보 유효성 검증
 * - PlaceInfo와의 연관관계 설정
 */
@Component
public class PlaceParkingFactory {
	
	/**
	 * PlaceParking 생성
	 *
	 * @param placeInfo   연관된 PlaceInfo
	 * @param available   주차 가능 여부
	 * @param parkingType 주차 유형
	 * @param description 주차 정보 설명
	 * @return 생성된 PlaceParking
	 */
	public PlaceParking create(
			PlaceInfo placeInfo,
			Boolean available,
			ParkingType parkingType,
			String description
	) {
		// 비즈니스 규칙 검증: 주차 불가능하면 주차 유형은 null이어야 함
		if (Boolean.FALSE.equals(available) && parkingType != null) {
			throw new IllegalArgumentException("주차가 불가능한 경우 주차 유형을 지정할 수 없습니다.");
		}

		// 주차 가능한데 유형이 없는 경우 기본값 설정
		ParkingType finalParkingType = parkingType;
		if (Boolean.TRUE.equals(available) && parkingType == null) {
			finalParkingType = ParkingType.FREE;  // 기본값: 무료 주차
		}

		return PlaceParking.builder()
				.placeInfo(placeInfo)
				.available(available != null ? available : false)  // 기본값: 불가능
				.parkingType(finalParkingType)
				.description(description)
				.build();
	}
	
	/**
	 * 주차 가능 여부만으로 간단하게 생성
	 */
	public PlaceParking createSimple(PlaceInfo placeInfo, Boolean available) {
		return create(placeInfo, available, null, null);
	}
	
	/**
	 * 주차 불가능 정보 생성
	 */
	public PlaceParking createNotAvailable(PlaceInfo placeInfo) {
		return PlaceParking.builder()
				.placeInfo(placeInfo)
				.available(false)
				.build();
	}
	
	/**
	 * 주차 가능 정보 생성 (유형 포함)
	 */
	public PlaceParking createAvailable(
			PlaceInfo placeInfo,
			ParkingType parkingType,
			String description
	) {
		return create(placeInfo, true, parkingType, description);
	}
}
