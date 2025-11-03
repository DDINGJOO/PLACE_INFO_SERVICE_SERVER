package com.teambind.placeinfoserver.place.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 업체 이미지 엔티티
 * 이미지 자체는 외부 이미지 서버에서 관리
 * 여기서는 이미지 URL/ID와 순서 정보만 관리
 */
@Entity
@Table(name = "place_images")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceImage {
	
	@Id
	private String id; // 이미지 서버에서 오는 이미지 Id
	
	/**
	 * 업체 정보 (다대일 관계)
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "place_info_id", nullable = false)
	private PlaceInfo placeInfo;
	
	/**
	 * 이미지 URL 또는 외부 서비스의 이미지 ID
	 * 마이크로서비스 아키텍처에서 이미지 서비스와 연동
	 */
	@Column(name = "image_url", nullable = false)
	private String imageUrl;
	
	
	/**
	 * PlaceInfo 연관관계 설정 (Package-private for bidirectional relationship)
	 */
	void setPlaceInfo(PlaceInfo placeInfo) {
		this.placeInfo = placeInfo;
	}
}
