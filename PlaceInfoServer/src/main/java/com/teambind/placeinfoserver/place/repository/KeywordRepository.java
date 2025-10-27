package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.domain.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 키워드 Repository
 */
@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
}
