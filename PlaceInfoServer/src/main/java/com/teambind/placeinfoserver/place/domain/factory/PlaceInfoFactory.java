package com.teambind.placeinfoserver.place.domain.factory;

import com.teambind.placeinfoserver.place.common.util.generator.PrimaryKeyGenerator;
import com.teambind.placeinfoserver.place.domain.entity.PlaceContact;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.entity.PlaceLocation;
import com.teambind.placeinfoserver.place.domain.entity.PlaceParking;
import com.teambind.placeinfoserver.place.domain.enums.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * PlaceInfo 엔티티 생성을 담당하는 Factory
 * <p>
 * 책임:
 * - PlaceInfo 엔티티의 일관된 생성
 * - 필수 값 검증
 * - ID 생성
 * - 초기 상태 설정
 */
@Component
@RequiredArgsConstructor
public class PlaceInfoFactory {
	
	private final PrimaryKeyGenerator idGenerator;
	
	/**
	 * 새로운 PlaceInfo 생성
	 *
	 * @param userId      소유자 ID (필수)
	 * @param placeName   업체명 (필수)
	 * @param description 설명 (선택)
	 * @param category    카테고리 (선택)
	 * @param placeType   업체 유형 (선택)
	 * @return 생성된 PlaceInfo
	 * @throws IllegalArgumentException 필수 값이 누락된 경우
	 */
	public PlaceInfo create(
			String userId,
			String placeName,
			String description,
			String category,
			String placeType
	) {
		// 필수 값 검증
		validateRequiredFields(userId, placeName);
		
		// ID 생성
		Long generatedId = idGenerator.generateLongKey();
		
		// 엔티티 생성
		return PlaceInfo.builder()
				.id(generatedId)
				.userId(userId)
				.placeName(placeName)
				.description(description)
				.category(category)
				.placeType(placeType)
				.isActive(false)  // 초기 상태: 비활성
				.approvalStatus(ApprovalStatus.PENDING)  // 초기 상태: 승인 대기
				.reviewCount(0)
				.build();
	}
	
	/**
	 * 연관 엔티티를 포함한 완전한 PlaceInfo 생성
	 *
	 * @param userId      소유자 ID (필수)
	 * @param placeName   업체명 (필수)
	 * @param description 설명
	 * @param category    카테고리
	 * @param placeType   업체 유형
	 * @param contact     연락처 정보
	 * @param location    위치 정보
	 * @param parking     주차 정보
	 * @return 생성된 PlaceInfo
	 */
	public PlaceInfo createWithRelations(
			String userId,
			String placeName,
			String description,
			String category,
			String placeType,
			PlaceContact contact,
			PlaceLocation location,
			PlaceParking parking
	) {
		PlaceInfo placeInfo = create(userId, placeName, description, category, placeType);
		
		// 연관관계 설정 (양방향 관계 자동 설정)
		if (contact != null) {
			placeInfo.setContact(contact);
		}
		
		if (location != null) {
			placeInfo.setLocation(location);
		}
		
		if (parking != null) {
			placeInfo.setParking(parking);
		}
		
		return placeInfo;
	}
	
	/**
	 * 필수 필드 검증
	 */
	private void validateRequiredFields(String userId, String placeName) {
		if (userId == null || userId.isBlank()) {
			throw new IllegalArgumentException("userId는 필수입니다.");
		}
		
		if (placeName == null || placeName.isBlank()) {
			throw new IllegalArgumentException("placeName은 필수입니다.");
		}
		
		if (placeName.length() > 100) {
			throw new IllegalArgumentException("placeName은 100자를 초과할 수 없습니다.");
		}
	}
}
