package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.domain.entity.PlaceParking;
import com.teambind.placeinfoserver.place.domain.enums.ParkingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 업체 주차 정보 Repository
 */
@Repository
public interface PlaceParkingRepository extends JpaRepository<PlaceParking, Long> {
	
	/**
	 * PlaceInfo ID로 주차 정보 조회
	 */
	Optional<PlaceParking> findByPlaceInfoId(Long placeInfoId);
	
	/**
	 * 주차 가능한 업체의 주차 정보 조회
	 */
	List<PlaceParking> findByAvailableTrue();
	
	/**
	 * 주차 불가능한 업체의 주차 정보 조회
	 */
	List<PlaceParking> findByAvailableFalse();
	
	/**
	 * 주차 타입별 조회 (무료/유료)
	 */
	List<PlaceParking> findByParkingType(ParkingType parkingType);
	
	/**
	 * 무료 주차 가능한 업체 조회
	 */
	List<PlaceParking> findByAvailableTrueAndParkingType(ParkingType parkingType);
	
	/**
	 * 특정 대수 이상 주차 가능한 업체 조회
	 */
	List<PlaceParking> findByAvailableTrueAndCapacityGreaterThanEqual(Integer capacity);
	
	/**
	 * 주차 가능 대수별 정렬 조회
	 */
	List<PlaceParking> findByAvailableTrueOrderByCapacityDesc();
	
	/**
	 * 주차 설명에 특정 키워드가 포함된 업체 조회
	 */
	List<PlaceParking> findByDescriptionContaining(String keyword);
	
	/**
	 * 주차 정보 통계
	 */
	@Query("SELECT " +
			"COUNT(p) as total, " +
			"COUNT(CASE WHEN p.available = true THEN 1 END) as available, " +
			"COUNT(CASE WHEN p.parkingType = 'FREE' THEN 1 END) as free, " +
			"COUNT(CASE WHEN p.parkingType = 'PAID' THEN 1 END) as paid " +
			"FROM PlaceParking p")
	Object[] getParkingStatistics();
	
	/**
	 * 평균 주차 가능 대수
	 */
	@Query("SELECT AVG(p.capacity) FROM PlaceParking p " +
			"WHERE p.available = true AND p.capacity IS NOT NULL")
	Double getAverageCapacity();
	
	/**
	 * 주차 타입별 카운트
	 */
	@Query("SELECT p.parkingType, COUNT(p) FROM PlaceParking p " +
			"WHERE p.available = true " +
			"GROUP BY p.parkingType")
	List<Object[]> countByParkingTypeGrouped();
	
	/**
	 * 주차 가능 여부별 카운트
	 */
	@Query("SELECT p.available, COUNT(p) FROM PlaceParking p " +
			"GROUP BY p.available")
	List<Object[]> countByAvailability();
}
