package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.entity.Keyword;
import com.teambind.placeinfoserver.place.entity.KeywordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 키워드 Repository
 */
@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
	
	/**
	 * 키워드 타입별 조회
	 */
	List<Keyword> findByTypeAndIsActiveTrueOrderByDisplayOrder(KeywordType type);
	
	/**
	 * 활성화된 모든 키워드 조회
	 */
	List<Keyword> findByIsActiveTrueOrderByTypeAscDisplayOrderAsc();
	
	/**
	 * 키워드 이름으로 조회
	 */
	Optional<Keyword> findByNameAndType(String name, KeywordType type);
	
	/**
	 * 키워드 이름 리스트로 조회
	 */
	List<Keyword> findByNameInAndIsActiveTrue(List<String> names);
	
	/**
	 * 키워드를 사용하는 업체 수 조회
	 */
	@Query("SELECT k.id, COUNT(p) FROM Keyword k " +
			"LEFT JOIN k.places p " +
			"WHERE k.isActive = true " +
			"GROUP BY k.id")
	List<Object[]> countPlacesByKeyword();
	
	/**
	 * 특정 업체가 사용하는 키워드 조회
	 */
	@Query("SELECT k FROM Keyword k " +
			"JOIN k.places p " +
			"WHERE p.id = :placeId " +
			"AND k.isActive = true " +
			"ORDER BY k.type, k.displayOrder")
	List<Keyword> findKeywordsByPlaceId(@Param("placeId") Long placeId);
	
	/**
	 * 인기 키워드 조회 (많이 사용되는 순)
	 */
	@Query("SELECT k, COUNT(p) as usageCount FROM Keyword k " +
			"LEFT JOIN k.places p " +
			"WHERE k.isActive = true " +
			"GROUP BY k " +
			"ORDER BY usageCount DESC")
	List<Object[]> findPopularKeywords();
	
	/**
	 * 키워드 존재 여부 확인
	 */
	boolean existsByNameAndType(String name, KeywordType type);
}
