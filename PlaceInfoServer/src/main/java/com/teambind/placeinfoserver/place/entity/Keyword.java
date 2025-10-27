package com.teambind.placeinfoserver.place.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * 키워드 마스터 데이터 엔티티
 * 업체가 선택할 수 있는 키워드를 미리 정의
 */
@Entity
@Table(name = "keywords",
		uniqueConstraints = @UniqueConstraint(columnNames = {"name", "type"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Keyword {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/**
	 * 키워드명
	 */
	@Column(name = "name", nullable = false, length = 50)
	private String name;
	
	/**
	 * 키워드 타입 (카테고리)
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private KeywordType type;
	
	/**
	 * 키워드 설명
	 */
	@Column(name = "description", length = 200)
	private String description;
	
	/**
	 * 표시 순서
	 */
	@Column(name = "display_order")
	private Integer displayOrder;
	
	/**
	 * 활성화 상태
	 */
	@Column(name = "is_active")
	@Builder.Default
	private Boolean isActive = true;
	
	/**
	 * 이 키워드를 사용하는 업체들 (다대다 관계)
	 */
	@ManyToMany(mappedBy = "keywords")
	@Builder.Default
	private Set<PlaceInfo> places = new HashSet<>();
	
	/**
	 * 편의 메서드: 키워드 타입과 이름을 조합한 전체 이름 반환
	 */
	public String getFullName() {
		return String.format("[%s] %s", type.getDescription(), name);
	}
	
	/**
	 * 미리 정의된 키워드 목록을 위한 정적 메서드들
	 * 실제 운영시 이 데이터는 DB에 마이그레이션 스크립트로 초기화
	 */
	public static class PredefinedKeywords {
		// 공간 유형
		public static final String ENSEMBLE_ROOM = "합주실";
		public static final String PRACTICE_ROOM = "연습실";
		public static final String LESSON_ROOM = "레슨실";
		public static final String RECORDING_ROOM = "녹음실";
		public static final String PERFORMANCE_PRACTICE_ROOM = "공연연습실";
		public static final String BUSKING_PREP_SPACE = "버스킹 준비 공간";
		
		// 악기/장비
		public static final String GRAND_PIANO = "그랜드 피아노";
		public static final String UPRIGHT_PIANO = "업라이트 피아노";
		public static final String DRUM_SET = "드럼 세트";
		public static final String ELECTRIC_GUITAR_AMP = "일렉기타 앰프";
		public static final String BASS_AMP = "베이스 앰프";
		public static final String PA_SYSTEM = "PA 시스템";
		public static final String AUDIO_INTERFACE = "오디오 인터페이스";
		public static final String VOCAL_MIC = "보컬 마이크";
		
		// 편의시설
		public static final String PARKING_AVAILABLE = "주차 가능";
		public static final String RESTROOM_AVAILABLE = "화장실 있음";
		public static final String AIR_CONDITIONING = "냉난방 완비";
		public static final String SOUNDPROOF = "방음 시설";
		public static final String LOUNGE_AREA = "휴게 공간";
		public static final String WIFI_PROVIDED = "와이파이 제공";
		
		// 기타 특성
		public static final String AFFORDABLE_PRICE = "저렴한 가격";
		public static final String PRIVATE_SPACE = "프라이빗 공간";
		public static final String CONVENIENT_TRANSPORT = "교통 편리";
		public static final String NEW_FACILITY = "신축 시설";
		public static final String SPACIOUS = "넓은 공간";
		public static final String CLEAN_INTERIOR = "깔끔한 인테리어";
	}
}
