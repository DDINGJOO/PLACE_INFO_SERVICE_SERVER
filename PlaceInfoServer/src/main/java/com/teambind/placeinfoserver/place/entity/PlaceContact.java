package com.teambind.placeinfoserver.place.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
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
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 업체 (일대일 관계)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false, unique = true)
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
        joinColumns = @JoinColumn(name = "contact_id")
    )
    @Column(name = "website_url", length = 500)
    @OrderColumn(name = "display_order")
    @Builder.Default
    private List<String> websites = new ArrayList<>();

    /**
     * SNS 링크 (JSON 형태 또는 별도 테이블)
     * 예: Instagram, Facebook, YouTube 등
     */
    @ElementCollection
    @CollectionTable(
        name = "place_social_links",
        joinColumns = @JoinColumn(name = "contact_id")
    )
    @Column(name = "social_url", length = 500)
    @OrderColumn(name = "display_order")
    @Builder.Default
    private List<String> socialLinks = new ArrayList<>();

    /**
     * 이메일
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 생성일시
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
}
