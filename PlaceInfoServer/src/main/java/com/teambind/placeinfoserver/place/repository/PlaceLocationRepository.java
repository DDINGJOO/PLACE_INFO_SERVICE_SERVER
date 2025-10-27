package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.domain.entity.PlaceLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 업체 위치 정보 Repository
 */
@Repository
public interface PlaceLocationRepository extends JpaRepository<PlaceLocation, Long> {
}
