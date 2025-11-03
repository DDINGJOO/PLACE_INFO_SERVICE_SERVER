package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 업체 정보 Repository
 * 엔티티 ID 타입: Long (내부 사용)
 * API 응답 시 String으로 변환하여 전달 (JavaScript 호환성)
 */
@Repository
public interface PlaceInfoRepository extends JpaRepository<PlaceInfo, Long> {

}
