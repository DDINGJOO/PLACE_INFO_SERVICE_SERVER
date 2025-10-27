package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 업체 정보 Repository (수정된 버전)
 * 엔티티 구조에 맞게 수정됨
 */
@Repository
public interface PlaceInfoRepository extends JpaRepository<PlaceInfo, String> {
	
	/**
	 * 유저 ID로 업체 목록 조회
	 */
	List<PlaceInfo> findByUserIdAndIsActiveTrue(String userId);
	
	/**
	 * 업체명으로 검색 (부분 일치)
	 */
	List<PlaceInfo> findByPlaceNameContainingAndIsActiveTrue(String placeName);
	
	/**
	 * ID와 유저 ID로 업체 조회 (권한 체크용)
	 */
	Optional<PlaceInfo> findByIdAndUserId(Long id, String userId);
	
	/**
	 * 카테고리별 업체 조회
	 */
	List<PlaceInfo> findByCategoryAndIsActiveTrue(String category);
	
	/**
	 * 업체 타입별 조회
	 */
	List<PlaceInfo> findByPlaceTypeAndIsActiveTrue(String placeType);
	
	/**
	 * 승인 상태별 조회
	 */
	List<PlaceInfo> findByApprovalStatusAndIsActiveTrue(ApprovalStatus approvalStatus);
	
	
	/**
	 * 키워드로 업체 검색
	 */
	@Query("SELECT DISTINCT p FROM PlaceInfo p " +
			"JOIN p.keywords k " +
			"WHERE k.id IN :keywordIds " +
			"AND p.isActive = true " +
			"AND p.approvalStatus = 'APPROVED'")
	List<PlaceInfo> findByKeywordIds(@Param("keywordIds") List<Long> keywordIds);
	
	/**
	 * 키워드 이름으로 업체 검색
	 */
	@Query("SELECT DISTINCT p FROM PlaceInfo p " +
			"JOIN p.keywords k " +
			"WHERE k.name IN :keywordNames " +
			"AND p.isActive = true " +
			"AND p.approvalStatus = 'APPROVED' " +
			"AND k.isActive = true")
	List<PlaceInfo> findByKeywordNames(@Param("keywordNames") List<String> keywordNames);
	
	/**
	 * 주차 가능한 업체만 조회
	 * PlaceParking과 조인하여 검색
	 */
	@Query("SELECT p FROM PlaceInfo p " +
			"JOIN p.parking pk " +
			"WHERE pk.available = true " +
			"AND p.isActive = true " +
			"AND p.approvalStatus = 'APPROVED'")
	List<PlaceInfo> findPlacesWithParking();
	
	/**
	 * 무료 주차 가능한 업체만 조회
	 */
	@Query("SELECT p FROM PlaceInfo p " +
			"JOIN p.parking pk " +
			"WHERE pk.available = true " +
			"AND pk.parkingType = 'FREE' " +
			"AND p.isActive = true " +
			"AND p.approvalStatus = 'APPROVED'")
	List<PlaceInfo> findPlacesWithFreeParking();
	
	/**
	 * 주소로 업체 검색 (부분 일치)
	 * PlaceLocation과 조인하여 검색
	 */
	@Query("SELECT p FROM PlaceInfo p " +
			"JOIN p.location l " +
			"WHERE (l.fullAddress LIKE %:searchTerm% " +
			"OR l.addressDetail LIKE %:searchTerm% " +
			"OR l.province LIKE %:searchTerm% " +
			"OR l.city LIKE %:searchTerm% " +
			"OR l.district LIKE %:searchTerm%) " +
			"AND p.isActive = true " +
			"AND p.approvalStatus = 'APPROVED'")
	List<PlaceInfo> findByAddressContaining(@Param("searchTerm") String searchTerm);
	
	/**
	 * 업체 존재 여부 확인 (유저별)
	 */
	boolean existsByIdAndUserId(Long id, String userId);
	
	/**
	 * 완성도가 높은 업체만 조회
	 * (위치, 연락처, 이미지가 모두 등록된 업체)
	 */
	@Query("SELECT p FROM PlaceInfo p " +
			"WHERE p.location IS NOT NULL " +
			"AND p.contact IS NOT NULL " +
			"AND SIZE(p.images) > 0 " +
			"AND p.isActive = true " +
			"AND p.approvalStatus = 'APPROVED'")
	List<PlaceInfo> findCompletePlaces();
	
	/**
	 * 리뷰가 많은 순으로 업체 조회
	 */
	List<PlaceInfo> findByIsActiveTrueAndApprovalStatusOrderByReviewCountDesc(ApprovalStatus status);
	
	/**
	 * 평점이 높은 순으로 업체 조회
	 */
	List<PlaceInfo> findByIsActiveTrueAndApprovalStatusOrderByRatingAverageDesc(ApprovalStatus status);
}
