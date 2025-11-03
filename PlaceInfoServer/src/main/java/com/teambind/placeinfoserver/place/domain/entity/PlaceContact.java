package com.teambind.placeinfoserver.place.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 업체 연락처 정보 엔티티
 * PlaceInfo Aggregate 내의 Entity
 * 1:1 관계로 정규화
 */
@Entity
@Table(name = "place_contacts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceContact extends BaseEntity {
	
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
	 * 연락처 (하이픈 없이 저장)
	 */
	@Column(name = "contact", length = 20)
	private String contact;
	
	/**
	 * 홈페이지 URL 목록 (최대 10개)
	 */
	@ElementCollection
	@CollectionTable(
			name = "place_websites",
			joinColumns = @JoinColumn(name = "place_contact_id")
	)
	@Column(name = "websites", length = 500)
	@OrderColumn(name = "websites_order")
	@Builder.Default
	private List<String> websites = new ArrayList<>();
	
	/**
	 * SNS 링크 (JSON 형태 또는 별도 테이블)
	 * 예: Instagram, Facebook, YouTube 등
	 */
	@ElementCollection
	@CollectionTable(
			name = "place_social_links",
			joinColumns = @JoinColumn(name = "place_contact_id")
	)
	@Column(name = "social_links", length = 500)
	@OrderColumn(name = "social_links_order")
	@Builder.Default
	private List<String> socialLinks = new ArrayList<>();
	
	/**
	 * 이메일
	 */
	@Column(name = "email", length = 100)
	private String email;
	
	/**
	 * 홈페이지 URL 추가
	 */
	public void addWebsite(String websiteUrl) {
		if (this.websites == null) {
			this.websites = new ArrayList<>();
		}
		if (this.websites.size() >= 10) {
			throw new IllegalStateException("홈페이지 URL은 최대 10개까지만 등록 가능합니다.");
		}
		this.websites.add(websiteUrl);
	}
	
	/**
	 * 소셜 링크 추가
	 */
	public void addSocialLink(String socialUrl) {
		if (this.socialLinks == null) {
			this.socialLinks = new ArrayList<>();
		}
		if (this.socialLinks.size() >= 10) {
			throw new IllegalStateException("소셜 링크는 최대 10개까지만 등록 가능합니다.");
		}
		this.socialLinks.add(socialUrl);
	}
	
	/**
	 * 연락처 정보 업데이트
	 */
	public void updateContactInfo(String contact, String email, List<String> websites, List<String> socialLinks) {
		if (contact != null) {
			this.contact = contact;
		}
		if (email != null) {
			this.email = email;
		}
		if (websites != null) {
			this.websites = websites;
		}
		if (socialLinks != null) {
			this.socialLinks = socialLinks;
		}
	}
	
	/**
	 * 연락처 설정 (테스트용)
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}
	
	/**
	 * 이메일 설정 (테스트용)
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * 웹사이트 목록 설정 (테스트용)
	 */
	public void setWebsites(List<String> websites) {
		this.websites = websites;
	}
	
	/**
	 * 소셜 링크 목록 설정 (테스트용)
	 */
	public void setSocialLinks(List<String> socialLinks) {
		this.socialLinks = socialLinks;
	}

	/**
	 * PlaceInfo 연관관계 설정 (Package-private for bidirectional relationship)
	 * PlaceInfo.setContact()에서만 호출되어야 함
	 */
	void setPlaceInfo(PlaceInfo placeInfo) {
		this.placeInfo = placeInfo;
	}
}
