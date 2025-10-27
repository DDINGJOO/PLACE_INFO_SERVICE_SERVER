package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.entity.PlaceLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 업체 위치 정보 Repository
 */
@Repository
public interface PlaceLocationRepository extends JpaRepository<PlaceLocation, Long> {
	
	/**
	 * PlaceInfo ID로 위치 정보 조회
	 */
	Optional<PlaceLocation> findByPlaceInfoId(Long placeInfoId);
	
	/**
	 * 도시별 업체 위치 조회
	 */
	List<PlaceLocation> findByCity(String city);
	
	/**
	 * 구/군별 업체 위치 조회
	 */
	List<PlaceLocation> findByDistrict(String district);
	
	/**
	 * 위치 기반 검색 (반경 내 업체 검색)
	 * PostGIS ST_DWithin 함수 사용
	 *
	 * @param latitude  중심 위도
	 * @param longitude 중심 경도
	 * @param distance  검색 반경 (미터)
	 */
	@Query(value = "SELECT pl.* FROM place_locations pl " +
			"WHERE ST_DWithin(" +
			"  pl.coordinates, " +
			"  ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, " +
			"  :distance" +
			") " +
			"ORDER BY ST_Distance(" +
			"  pl.coordinates, " +
			"  ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography" +
			")",
			nativeQuery = true)
	List<PlaceLocation> findNearbyLocations(@Param("latitude") double latitude,
	                                        @Param("longitude") double longitude,
	                                        @Param("distance") double distance);
	
	/**
	 * 주소 검색 (부분 일치)
	 */
	@Query("SELECT pl FROM PlaceLocation pl " +
			"WHERE pl.fullAddress LIKE %:searchTerm% " +
			"OR pl.addressDetail LIKE %:searchTerm%")
	List<PlaceLocation> searchByAddress(@Param("searchTerm") String searchTerm);
	
	/**
	 * 좌표가 설정된 위치만 조회
	 */
	@Query("SELECT pl FROM PlaceLocation pl " +
			"WHERE pl.coordinates IS NOT NULL")
	List<PlaceLocation> findAllWithCoordinates();
}
