package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.entity.PlaceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 업체 이미지 Repository (수정된 버전)
 * PlaceImage 엔티티의 ID가 String 타입임을 반영
 */
@Repository
public interface PlaceImageRepository extends JpaRepository<PlaceImage, String> {  // ID 타입을 String으로 수정

    /**
     * 업체별 이미지 목록 조회
     * PlaceImage는 displayOrder와 isActive 필드가 없으므로 단순 조회
     */
    List<PlaceImage> findByPlaceInfoId(Long placeId);

    /**
     * 업체의 첫 번째 이미지 조회 (대표 이미지로 사용)
     * List의 첫 번째 요소를 대표 이미지로 간주
     */
    @Query("SELECT pi FROM PlaceImage pi " +
           "WHERE pi.placeInfo.id = :placeId")
    List<PlaceImage> findAllByPlaceId(@Param("placeId") Long placeId);

    /**
     * 업체별 이미지 개수
     */
    long countByPlaceInfoId(Long placeId);

    /**
     * 특정 업체의 이미지 존재 여부 확인
     */
    boolean existsByPlaceInfoId(Long placeId);

    /**
     * 이미지 ID와 업체 ID로 이미지 조회 (권한 체크용)
     */
    Optional<PlaceImage> findByIdAndPlaceInfoId(String imageId, Long placeId);

    /**
     * 업체별 이미지 모두 삭제
     * CascadeType.ALL로 자동 삭제되므로 일반적으로 필요 없지만,
     * 명시적 삭제가 필요한 경우를 위해 제공
     */
    void deleteByPlaceInfoId(Long placeId);

    /**
     * 이미지 URL로 이미지 찾기
     */
    Optional<PlaceImage> findByImageUrl(String imageUrl);

    /**
     * 여러 업체의 이미지를 한 번에 조회
     */
    @Query("SELECT pi FROM PlaceImage pi " +
           "WHERE pi.placeInfo.id IN :placeIds")
    List<PlaceImage> findByPlaceIds(@Param("placeIds") List<Long> placeIds);

    /**
     * 이미지가 있는 업체 ID 목록 조회
     */
    @Query("SELECT DISTINCT pi.placeInfo.id FROM PlaceImage pi")
    List<Long> findPlaceIdsWithImages();
}