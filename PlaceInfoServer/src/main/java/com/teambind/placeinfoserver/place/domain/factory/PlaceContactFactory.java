package com.teambind.placeinfoserver.place.domain.factory;

import com.teambind.placeinfoserver.place.domain.entity.PlaceContact;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PlaceContact 엔티티 생성을 담당하는 Factory
 * <p>
 * 책임:
 * - PlaceContact 엔티티의 일관된 생성
 * - 연락처 정보 유효성 검증
 * - PlaceInfo와의 연관관계 설정
 */
@Component
public class PlaceContactFactory {
	
	/**
	 * PlaceContact 생성
	 *
	 * @param placeInfo   연관된 PlaceInfo
	 * @param contact     연락처 (전화번호)
	 * @param email       이메일
	 * @param websites    웹사이트 목록
	 * @param socialLinks SNS 링크 목록
	 * @return 생성된 PlaceContact
	 */
	public PlaceContact create(
			PlaceInfo placeInfo,
			String contact,
			String email,
			List<String> websites,
			List<String> socialLinks
	) {
		// 기본 유효성 검증
		validateContact(contact);
		
		return PlaceContact.builder()
				.placeInfo(placeInfo)
				.contact(contact)
				.email(email)
				.websites(websites)
				.socialLinks(socialLinks)
				.build();
	}
	
	/**
	 * 최소 정보만으로 PlaceContact 생성 (연락처만 필수)
	 */
	public PlaceContact createMinimal(PlaceInfo placeInfo, String contact) {
		validateContact(contact);
		
		return PlaceContact.builder()
				.placeInfo(placeInfo)
				.contact(contact)
				.build();
	}
	
	/**
	 * 연락처 유효성 검증
	 */
	private void validateContact(String contact) {
		if (contact == null || contact.isBlank()) {
			throw new IllegalArgumentException("연락처는 필수입니다.");
		}
		
		// 전화번호 형식 간단 검증 (숫자, 하이픈, 괄호만 허용)
		if (!contact.matches("[0-9-()\\s]+")) {
			throw new IllegalArgumentException("연락처는 숫자와 하이픈, 괄호만 포함할 수 있습니다.");
		}
	}
}
