package com.teambind.placeinfoserver.place.domain.entity;

import com.teambind.placeinfoserver.place.domain.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 업체 정보 엔티티 (Aggregate Root)
 * DDD 적용: 핵심 정보만 보유, 나머지는 정규화된 별도 엔티티로 분리
 * - 연락처: PlaceContact (1:1)
 * - 위치: PlaceLocation (1:1)
 * - 주차: PlaceParking (1:1)
 * - 이미지: PlaceImage (1:N)
 * <p>
 * Room은 별도 Aggregate Root로 분리 (placeId로 참조)
 * <p>
 * 소프트 삭제 적용: 삭제 시 실제 삭제 대신 deleted_at 업데이트
 */
@Entity
@Table(name = "place_info")
@SQLDelete(sql = "UPDATE place_info SET deleted_at = NOW(), deleted_by = ? WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceInfo extends BaseEntity {
	
	@Id
	private String id;
	
	/**
	 * 유저 ID (외부 서비스 참조)
	 * 한 유저는 여러 업체를 소유할 수 있음
	 */
	@Column(name = "user_id", nullable = false, length = 100)
	private String userId;
	
	/**
	 * 업체명
	 */
	@Column(name = "place_name", nullable = false, length = 100)
	private String placeName;
	
	/**
	 * 기본 소개 (최대 500자)
	 */
	@Column(name = "description", length = 500)
	private String description;
	
	/**
	 * 업체 카테고리
	 * 예: 연습실, 공연장, 스튜디오 등
	 */
	@Column(name = "category", length = 50)
	private String category;
	
	/**
	 * 업체 유형
	 * 예: 음악, 댄스, 공연 등
	 */
	@Column(name = "place_type", length = 50)
	private String placeType;
	
	/**
	 * 연락처 정보 (1:1)
	 */
	@OneToOne(mappedBy = "placeInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private PlaceContact contact;
	
	/**
	 * 위치 정보 (1:1)
	 */
	@OneToOne(mappedBy = "placeInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private PlaceLocation location;
	
	/**
	 * 주차 정보 (1:1)
	 */
	@OneToOne(mappedBy = "placeInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private PlaceParking parking;
	
	/**
	 * 업체 이미지 목록 (1:N)
	 * 순서를 유지하여 첫 번째 이미지가 대표 이미지
	 */
	@OneToMany(mappedBy = "placeInfo", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<PlaceImage> images = new ArrayList<>();
	
	/**
	 * 업체 키워드 목록 (N:N)
	 * 최대 10개까지 선택 가능
	 */
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "place_keywords",
			joinColumns = @JoinColumn(name = "place_id"),
			inverseJoinColumns = @JoinColumn(name = "keyword_id")
	)
	@Builder.Default
	private Set<Keyword> keywords = new HashSet<>();
	
	/**
	 * 업체 활성화 상태
	 */
	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;
	
	/**
	 * 업체 승인 상태
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "approval_status", length = 20, nullable = false)
	@Builder.Default
	private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
	
	/**
	 * 평점 평균
	 * 리뷰 서버에서 업데이트
	 */
	@Column(name = "rating_average")
	private Double ratingAverage;
	
	/**
	 * 리뷰 개수
	 * 리뷰 서버에서 업데이트
	 */
	@Column(name = "review_count")
	@Builder.Default
	private Integer reviewCount = 0;
	
	/**
	 * 삭제일시 (소프트 삭제)
	 */
	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
	
	/**
	 * 삭제한 사용자 ID (소프트 삭제)
	 */
	@Column(name = "deleted_by", length = 100)
	private String deletedBy;
	
	// ========== Aggregate 내부 비즈니스 로직 ==========
	
	/**
	 * 연락처 정보 설정
	 */
	public void setContact(PlaceContact contact) {
		if (this.contact != null) {
			this.contact.setPlaceInfo(null);
		}
		this.contact = contact;
		if (contact != null) {
			contact.setPlaceInfo(this);
		}
	}
	
	/**
	 * 위치 정보 설정
	 */
	public void setLocation(PlaceLocation location) {
		if (this.location != null) {
			this.location.setPlaceInfo(null);
		}
		this.location = location;
		if (location != null) {
			location.setPlaceInfo(this);
		}
	}
	
	/**
	 * 주차 정보 설정
	 */
	public void setParking(PlaceParking parking) {
		if (this.parking != null) {
			this.parking.setPlaceInfo(null);
		}
		this.parking = parking;
		if (parking != null) {
			parking.setPlaceInfo(this);
		}
	}
	
	/**
	 * 키워드 추가 (최대 10개 제한)
	 */
	public void addKeyword(Keyword keyword) {
		if (this.keywords.size() >= 10) {
			throw new IllegalStateException("키워드는 최대 10개까지만 선택 가능합니다.");
		}
		this.keywords.add(keyword);
	}
	
	/**
	 * 키워드 제거
	 */
	public void removeKeyword(Keyword keyword) {
		this.keywords.remove(keyword);
	}
	
	/**
	 * 이미지 추가 (최대 10장 제한)
	 */
	public void addImage(PlaceImage image) {
		if (this.images.size() >= 10) {
			throw new IllegalStateException("이미지는 최대 10장까지만 등록 가능합니다.");
		}
		this.images.add(image);
		image.setPlaceInfo(this);
	}
	
	/**
	 * 이미지 제거
	 */
	public void removeImage(PlaceImage image) {
		this.images.remove(image);
		image.setPlaceInfo(null);
	}
	
	/**
	 * 업체 활성화
	 */
	public void activate() {
		this.isActive = true;
	}
	
	/**
	 * 업체 비활성화
	 */
	public void deactivate() {
		this.isActive = false;
	}
	
	/**
	 * 승인
	 */
	public void approve() {
		this.approvalStatus = ApprovalStatus.APPROVED;
	}
	
	/**
	 * 거부
	 */
	public void reject() {
		this.approvalStatus = ApprovalStatus.REJECTED;
	}
	
	/**
	 * 평점 업데이트 (외부 서비스에서 호출)
	 */
	public void updateRating(double average, int count) {
		this.ratingAverage = average;
		this.reviewCount = count;
	}
	
	/**
	 * Aggregate 완전성 검증
	 */
	public boolean isComplete() {
		return placeName != null
				&& !placeName.isBlank()
				&& location != null
				&& contact != null;
	}
	
	/**
	 * 소프트 삭제
	 *
	 * @param deletedBy 삭제한 사용자 ID
	 */
	public void softDelete(String deletedBy) {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = deletedBy;
		this.isActive = false;
	}
	
	/**
	 * 삭제 취소 (복구)
	 */
	public void restore() {
		this.deletedAt = null;
		this.deletedBy = null;
		this.isActive = true;
	}
	
	/**
	 * 삭제 여부 확인
	 */
	public boolean isDeleted() {
		return this.deletedAt != null;
	}
	
	/**
	 * 업체명 변경
	 *
	 * @param newName 새로운 업체명
	 * @throws IllegalArgumentException 업체명이 비어있는 경우
	 */
	public void updatePlaceName(String newName) {
		if (newName == null || newName.isBlank()) {
			throw new IllegalArgumentException("업체명은 필수입니다.");
		}
		this.placeName = newName;
	}
	
	/**
	 * 소개글 변경
	 *
	 * @param newDescription 새로운 소개글
	 */
	public void updateDescription(String newDescription) {
		this.description = newDescription;
	}
	
	/**
	 * 카테고리 변경
	 *
	 * @param newCategory 새로운 카테고리
	 */
	public void updateCategory(String newCategory) {
		this.category = newCategory;
	}
	
	/**
	 * 업체 유형 변경
	 *
	 * @param newType 새로운 업체 유형
	 */
	public void updatePlaceType(String newType) {
		this.placeType = newType;
	}
}
