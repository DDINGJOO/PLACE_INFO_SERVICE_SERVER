package com.teambind.placeinfoserver.place.repository;

import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;

public interface PlaceInfoSearchRepository {
	// dsl 테스트를 위한 임시 메소드
	PlaceInfo search(String placeId);
	
}
