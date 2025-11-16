package com.teambind.placeinfoserver.place.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 룸 엔티티
 * PlaceInfo와 1:N 관계 (별도 Aggregate Root로 placeId로 참조)
 * 룸의 상세 정보(시간대 등)는 다른 서비스에서 관리
 */
@Entity
@Table(name = "room")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	/**
	 * 룸 ID (외부 서비스에서 생성된 ID)
	 */
	@Column(name = "room_id", nullable = false, unique = true)
	private Long roomId;

	/**
	 * PlaceInfo ID (외부 참조)
	 * Room은 별도 Aggregate Root로 분리되어 ID로만 참조
	 */
	@Column(name = "place_id", nullable = false)
	private Long placeId;

	/**
	 * 활성화 상태
	 */
	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;

	// ========== 비즈니스 로직 ==========

	/**
	 * 룸 활성화
	 */
	public void activate() {
		this.isActive = true;
	}

	/**
	 * 룸 비활성화
	 */
	public void deactivate() {
		this.isActive = false;
	}
}
