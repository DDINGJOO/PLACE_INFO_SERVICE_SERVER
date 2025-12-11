package com.teambind.placeinfoserver.place.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 업체 이미지 엔티티
 * 이미지 자체는 외부 이미지 서버에서 관리
 * 여기서는 이미지 URL/ID와 순서 정보만 관리
 * imageId와 imageUrl은 항상 쌍으로 관리되며, 이미지의 고유 식별자와 접근 URL을 제공
 */
@Entity
@Table(name = "place_images")
@Getter
@NoArgsConstructor
public class PlaceImage {
	
	@Id
	private String id; // 이미지 서버에서 오는 이미지 Id (imageId)
	
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
	 * 이미지 순서 (1부터 시작)
	 * Kafka 이벤트로부터 받은 sequence 정보 저장
	 */
	@Column(name = "sequence")
	private Long sequence;
	
	/**
	 * 생성자에 검증 로직 추가
	 */
	@Builder
	public PlaceImage(String id, PlaceInfo placeInfo, String imageUrl, Long sequence) {
		validateImagePair(id, imageUrl);
		this.id = id;
		this.placeInfo = placeInfo;
		this.imageUrl = imageUrl;
		this.sequence = sequence;
	}
	
	/**
	 * PlaceInfo 연관관계 설정 (Package-private for bidirectional relationship)
	 */
	void setPlaceInfo(PlaceInfo placeInfo) {
		this.placeInfo = placeInfo;
	}
	
	/**
	 * 이미지 URL 설정 (테스트용)
	 */
	@Deprecated
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	/**
	 * imageId와 imageUrl 쌍으로 업데이트
	 *
	 * @param newImageId  새로운 이미지 ID
	 * @param newImageUrl 새로운 이미지 URL
	 */
	public void updateImagePair(String newImageId, String newImageUrl) {
		if (newImageId == null || newImageId.isBlank()) {
			throw new IllegalArgumentException("Image ID cannot be null or empty");
		}
		if (newImageUrl == null || newImageUrl.isBlank()) {
			throw new IllegalArgumentException("Image URL cannot be null or empty");
		}
		validateImagePair(newImageId, newImageUrl);
		this.id = newImageId;
		this.imageUrl = newImageUrl;
	}
	
	/**
	 * imageId와 imageUrl 쌍 검증
	 *
	 * @param imageId  이미지 ID
	 * @param imageUrl 이미지 URL
	 */
	private void validateImagePair(String imageId, String imageUrl) {
		// null/blank 체크
		if (imageId == null || imageId.isBlank()) {
			throw new IllegalArgumentException("Image ID cannot be null or empty");
		}
		if (imageUrl == null || imageUrl.isBlank()) {
			throw new IllegalArgumentException("Image URL cannot be null or empty");
		}
		
		// URL 기본 검증 - null과 빈 문자열만 체크 (다양한 형식 허용)
		// 실제 이미지 서비스에서 다양한 형태의 식별자를 사용할 수 있음
		
		// imageId 최소 길이 검증 (최소 3자 이상)
		if (imageId.length() < 3) {
			throw new IllegalArgumentException("Image ID is too short: " + imageId);
		}
	}
}
