package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.domain.entity.Keyword;
import com.teambind.placeinfoserver.place.domain.enums.KeywordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 키워드 Repository
 */
@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
	
	/**
	 * 활성화된 모든 키워드 조회 (표시 순서대로 정렬)
	 */
	List<Keyword> findByIsActiveTrueOrderByDisplayOrderAsc();
	
	/**
	 * 특정 타입의 활성화된 키워드 조회 (표시 순서대로 정렬)
	 */
	List<Keyword> findByTypeAndIsActiveTrueOrderByDisplayOrderAsc(KeywordType type);
}
