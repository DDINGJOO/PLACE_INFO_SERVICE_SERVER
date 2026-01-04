package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 업체 정보 Repository
 * 엔티티 ID 타입: Long (내부 사용)
 * API 응답 시 String으로 변환하여 전달 (JavaScript 호환성)
 */
@Repository
public interface PlaceInfoRepository extends JpaRepository<PlaceInfo, Long> {
	
	/**
	 * 배치 조회를 위한 메서드
	 * N+1 문제 방지를 위해 연관 엔티티를 Fetch Join으로 함께 조회
	 *
	 * @param ids 조회할 placeId 집합
	 * @return 조회된 PlaceInfo 목록 (존재하는 것만)
	 */
	@Query("SELECT DISTINCT p FROM PlaceInfo p " +
			"LEFT JOIN FETCH p.contact " +
			"LEFT JOIN FETCH p.location " +
			"LEFT JOIN FETCH p.parking " +
			"LEFT JOIN FETCH p.images " +
			"LEFT JOIN FETCH p.keywords " +
			"WHERE p.id IN :ids " +
			"AND p.isActive = true")
	List<PlaceInfo> findAllByIdWithDetails(@Param("ids") Set<Long> ids);
	
	/**
	 * 사용자 ID로 본인 등록 공간 목록 조회
	 * 삭제되지 않은 모든 공간 조회 (활성/비활성 모두 포함)
	 *
	 * @param userId 사용자 ID
	 * @return 해당 사용자가 등록한 PlaceInfo 목록
	 */
	@Query("SELECT DISTINCT p FROM PlaceInfo p " +
			"LEFT JOIN FETCH p.contact " +
			"LEFT JOIN FETCH p.location " +
			"LEFT JOIN FETCH p.parking " +
			"LEFT JOIN FETCH p.images " +
			"LEFT JOIN FETCH p.keywords " +
			"WHERE p.userId = :userId " +
			"AND p.deletedAt IS NULL " +
			"ORDER BY p.createdAt DESC")
	List<PlaceInfo> findAllByUserIdWithDetails(@Param("userId") String userId);
	
}
