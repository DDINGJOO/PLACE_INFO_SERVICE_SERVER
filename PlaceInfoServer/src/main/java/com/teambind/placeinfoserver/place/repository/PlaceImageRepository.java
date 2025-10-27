package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.domain.entity.PlaceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 업체 이미지 Repository (수정된 버전)
 * PlaceImage 엔티티의 ID가 String 타입임을 반영
 */
@Repository
public interface PlaceImageRepository extends JpaRepository<PlaceImage, String> {  // ID 타입을 String으로 수정
}
