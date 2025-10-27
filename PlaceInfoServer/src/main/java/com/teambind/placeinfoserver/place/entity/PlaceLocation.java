package com.teambind.placeinfoserver.place.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

/**
 * 업체 위치 정보 엔티티
 * PlaceInfo Aggregate 내의 Entity
 * 1:1 관계로 정규화
 */
@Entity
@Table(name = "place_locations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceLocation {

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
     * 도/시 (예: 서울특별시)
     */
    @Column(name = "province", length = 50)
    private String province;

    /**
     * 시/구/군 (예: 강남구)
     */
    @Column(name = "city", length = 50)
    private String city;

    /**
     * 동/읍/면 (예: 역삼동)
     */
    @Column(name = "district", length = 50)
    private String district;

    /**
     * 전체 주소 (검색용)
     */
    @Column(name = "full_address", nullable = false, length = 500)
    private String fullAddress;

    /**
     * 상세 주소 (건물명, 호수 등)
     */
    @Column(name = "address_detail", length = 200)
    private String addressDetail;

    /**
     * 우편번호
     */
    @Column(name = "postal_code", length = 10)
    private String postalCode;

    /**
     * 위치 좌표 (PostGIS Point 타입)
     * 위도/경도 저장
     */
    @Column(name = "coordinates", columnDefinition = "geography(Point,4326)")
    private Point coordinates;

    /**
     * 위도 (별도 저장 - 검색 최적화용)
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * 경도 (별도 저장 - 검색 최적화용)
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * 위치 관련 상세 설명
     * 예: "2호선 강남역 3번 출구에서 도보 5분"
     */
    @Column(name = "location_guide", length = 500)
    private String locationGuide;

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
     * 좌표 설정 (Point와 개별 lat/lng 동시 설정)
     */
    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
        if (coordinates != null) {
            this.latitude = coordinates.getY();
            this.longitude = coordinates.getX();
        }
    }

    /**
     * 위도/경도로 좌표 설정
     */
    public void setLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        // Point 객체는 별도로 생성해서 설정 필요
    }
}
