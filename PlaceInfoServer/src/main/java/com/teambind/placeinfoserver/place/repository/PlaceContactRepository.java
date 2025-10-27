package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.entity.PlaceContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 업체 연락처 정보 Repository
 */
@Repository
public interface PlaceContactRepository extends JpaRepository<PlaceContact, Long> {

    /**
     * PlaceInfo ID로 연락처 정보 조회
     */
    Optional<PlaceContact> findByPlaceInfoId(Long placeInfoId);

    /**
     * 이메일로 업체 연락처 검색
     */
    List<PlaceContact> findByEmail(String email);

    /**
     * 연락처(전화번호)로 업체 찾기
     */
    List<PlaceContact> findByContact(String contact);

    /**
     * 연락처 부분 일치 검색
     */
    List<PlaceContact> findByContactContaining(String contactPart);

    /**
     * 웹사이트 URL을 가진 업체 연락처 조회
     */
    @Query("SELECT pc FROM PlaceContact pc " +
           "WHERE SIZE(pc.websites) > 0")
    List<PlaceContact> findContactsWithWebsites();

    /**
     * 소셜 링크를 가진 업체 연락처 조회
     */
    @Query("SELECT pc FROM PlaceContact pc " +
           "WHERE SIZE(pc.socialLinks) > 0")
    List<PlaceContact> findContactsWithSocialLinks();

    /**
     * 특정 웹사이트 URL을 포함하는 업체 찾기
     */
    @Query("SELECT pc FROM PlaceContact pc " +
           "JOIN pc.websites w " +
           "WHERE w LIKE %:url%")
    List<PlaceContact> findByWebsiteUrl(@Param("url") String url);

    /**
     * 특정 소셜 링크를 포함하는 업체 찾기
     */
    @Query("SELECT pc FROM PlaceContact pc " +
           "JOIN pc.socialLinks sl " +
           "WHERE sl LIKE %:socialUrl%")
    List<PlaceContact> findBySocialLinkUrl(@Param("socialUrl") String socialUrl);

    /**
     * 연락처 정보가 완전한 업체 조회
     * (전화번호와 이메일이 모두 있는 경우)
     */
    @Query("SELECT pc FROM PlaceContact pc " +
           "WHERE pc.contact IS NOT NULL " +
           "AND pc.email IS NOT NULL")
    List<PlaceContact> findCompleteContacts();

    /**
     * 연락처 정보가 없는 업체 조회
     */
    @Query("SELECT pc FROM PlaceContact pc " +
           "WHERE pc.contact IS NULL " +
           "AND pc.email IS NULL")
    List<PlaceContact> findIncompleteContacts();
}