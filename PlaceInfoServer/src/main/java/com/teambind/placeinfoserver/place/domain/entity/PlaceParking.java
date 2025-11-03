package com.teambind.placeinfoserver.place.domain.entity;

import com.teambind.placeinfoserver.place.domain.enums.ParkingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 업체 주차 정보 엔티티
 * PlaceInfo Aggregate 내의 Entity
 * 1:1 관계로 정규화
 */
@Entity
@Table(name = "place_parkings")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceParking extends BaseEntity {
	
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
	 * 주차 가능 여부
	 */
	@Column(name = "available", nullable = false)
	@Builder.Default
	private Boolean available = false;
	
	/**
	 * 주차 타입 (무료/유료)
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "parking_type")
	private ParkingType parkingType;
	
	
	/**
	 * 주차 관련 상세 설명 (최대 500자)
	 * 예: "건물 지하 1층, 2시간 무료 주차 가능"
	 */
	@Column(name = "description", length = 500)
	private String description;
	
	/**
	 * 주차 가능 설정
	 */
	public void enableParking(ParkingType type) {
		this.available = true;
		this.parkingType = type;
	}
	
	/**
	 * 주차 불가 설정
	 */
	public void disableParking() {
		this.available = false;
		this.parkingType = null;
	}
	
	/**
	 * 무료 주차 여부
	 */
	public boolean isFreeParking() {
		return available && parkingType == ParkingType.FREE;
	}
	
	/**
	 * PlaceInfo 연관관계 설정 (Package-private for bidirectional relationship)
	 */
	void setPlaceInfo(PlaceInfo placeInfo) {
		this.placeInfo = placeInfo;
	}
}
