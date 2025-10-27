package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 업체 정보 Repository (수정된 버전)
 * 엔티티 구조에 맞게 수정됨
 */
@Repository
public interface PlaceInfoRepository extends JpaRepository<PlaceInfo, String> {

}
