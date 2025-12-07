package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.domain.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Room Repository
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

	/**
	 * PlaceId로 활성화된 Room 목록 조회
	 */
	List<Room> findByPlaceIdAndIsActiveTrue(Long placeId);

	/**
	 * 여러 PlaceId에 대한 활성화된 Room 목록 일괄 조회
	 * 배치 조회 성능 최적화를 위한 메서드
	 */
	List<Room> findByPlaceIdInAndIsActiveTrue(Set<Long> placeIds);

	/**
	 * PlaceId로 모든 Room 목록 조회
	 */
	List<Room> findByPlaceId(Long placeId);

	/**
	 * RoomId로 Room 조회
	 */
	Optional<Room> findByRoomId(Long roomId);

	/**
	 * PlaceId로 Room 개수 조회
	 */
	Long countByPlaceIdAndIsActiveTrue(Long placeId);

	/**
	 * 여러 PlaceId의 활성화된 Room ID 목록 조회 (검색 결과용)
	 */
	@Query("SELECT r.placeId, r.roomId FROM Room r WHERE r.placeId IN :placeIds AND r.isActive = true")
	List<Object[]> findRoomIdsByPlaceIds(@Param("placeIds") List<Long> placeIds);
}