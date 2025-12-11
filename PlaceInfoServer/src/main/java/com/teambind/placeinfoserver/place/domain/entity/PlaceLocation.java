package com.teambind.placeinfoserver.place.domain.entity;

import com.teambind.placeinfoserver.place.common.util.geometry.GeometryUtil;
import com.teambind.placeinfoserver.place.domain.vo.Address;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

/**
 * 업체 위치 정보 엔티티
 * PlaceInfo Aggregate 내의 Entity
 * 1:1 관계로 정규화
 */
@Entity
@Table(name = "place_locations")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceLocation extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/**
	 * 소속 업체 (일대일 관계)
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "place_info_id", nullable = false, unique = true)
	private PlaceInfo placeInfo;
	
	/**
	 * 주소 (Value Object)
	 */
	@Embedded
	private Address address;
	
	/**
	 * 위치 좌표 (PostGIS Point 타입)
	 * 위도/경도 저장
	 */
	@Column(name = "coordinates", columnDefinition = "geography(Point,4326)")
	private Point coordinates;
	
	/**
	 * 위도 (별도 저장 - 검색 최적화용)
	 */
	@Column(name = "latitude")
	private Double latitude;
	
	/**
	 * 경도 (별도 저장 - 검색 최적화용)
	 */
	@Column(name = "longitude")
	private Double longitude;
	
	/**
	 * 위치 관련 상세 설명
	 * 예: "2호선 강남역 3번 출구에서 도보 5분"
	 */
	@Column(name = "location_guide", length = 500)
	private String locationGuide;
	
	/**
	 * 좌표 설정 (Point와 개별 lat/lng 동시 설정)
	 */
	public void setCoordinates(Point coordinates) {
		this.coordinates = coordinates;
		if (coordinates != null) {
			this.latitude = coordinates.getY();
			this.longitude = coordinates.getX();
		}
	}
	
	/**
	 * 위도/경도로 좌표 설정
	 *
	 * @param latitude  위도 (-90 ~ 90)
	 * @param longitude 경도 (-180 ~ 180)
	 * @throws IllegalArgumentException 위도/경도 범위가 유효하지 않은 경우
	 */
	public void setLatLng(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.coordinates = GeometryUtil.createPoint(latitude, longitude);
	}
	
	/**
	 * 주소 정보 업데이트
	 */
	public void updateAddress(Address address) {
		if (address != null) {
			this.address = address;
		}
	}
	
	/**
	 * 위치 안내 정보 업데이트
	 */
	public void updateLocationGuide(String locationGuide) {
		this.locationGuide = locationGuide;
	}
	
	/**
	 * 주소 설정 (테스트용)
	 */
	public void setAddress(Address address) {
		this.address = address;
	}
	
	/**
	 * 위치 안내 설정 (테스트용)
	 */
	public void setLocationGuide(String locationGuide) {
		this.locationGuide = locationGuide;
	}
	
	/**
	 * PlaceInfo 연관관계 설정 (Package-private for bidirectional relationship)
	 * PlaceInfo.setLocation()에서만 호출되어야 함
	 */
	void setPlaceInfo(PlaceInfo placeInfo) {
		this.placeInfo = placeInfo;
	}
}
