package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.domain.entity.PlaceParking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 업체 주차 정보 Repository
 */
@Repository
public interface PlaceParkingRepository extends JpaRepository<PlaceParking, Long> {

}
