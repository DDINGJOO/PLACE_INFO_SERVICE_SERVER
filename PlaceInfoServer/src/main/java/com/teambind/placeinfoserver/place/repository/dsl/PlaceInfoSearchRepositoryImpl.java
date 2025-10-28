package com.teambind.placeinfoserver.place.repository.dsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teambind.placeinfoserver.place.domain.entity.QPlaceInfo;
import com.teambind.placeinfoserver.place.repository.PlaceInfoSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlaceInfoSearchRepositoryImpl implements PlaceInfoSearchRepository {
	
	private final QPlaceInfo pi = QPlaceInfo.placeInfo;
	private final JPAQueryFactory queryFactory;
	
	
}
