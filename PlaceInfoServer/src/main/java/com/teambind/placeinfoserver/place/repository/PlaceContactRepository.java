package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.domain.entity.PlaceContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 업체 연락처 정보 Repository
 */
@Repository
public interface PlaceContactRepository extends JpaRepository<PlaceContact, Long> {
}
