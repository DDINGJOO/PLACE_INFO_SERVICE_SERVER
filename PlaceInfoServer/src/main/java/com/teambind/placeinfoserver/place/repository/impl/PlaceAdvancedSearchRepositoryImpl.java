package com.teambind.placeinfoserver.place.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teambind.placeinfoserver.place.domain.entity.*;
import com.teambind.placeinfoserver.place.domain.enums.ApprovalStatus;
import com.teambind.placeinfoserver.place.dto.cursor.PlaceSearchCursor;
import com.teambind.placeinfoserver.place.dto.request.PlaceSearchRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceSearchResponse;
import com.teambind.placeinfoserver.place.repository.PlaceAdvancedSearchRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * QueryDSL을 활용한 고급 검색 리포지토리 구현
 * 커서 기반 페이징과 최적화된 쿼리 실행
 */
@Repository
@RequiredArgsConstructor
public class PlaceAdvancedSearchRepositoryImpl implements PlaceAdvancedSearchRepository {

	private final JPAQueryFactory queryFactory;
	private final EntityManager entityManager;

	// Q타입 엔티티
	private final QPlaceInfo placeInfo = QPlaceInfo.placeInfo;
	private final QPlaceLocation placeLocation = QPlaceLocation.placeLocation;
	private final QPlaceParking placeParking = QPlaceParking.placeParking;
	private final QPlaceImage placeImage = QPlaceImage.placeImage;
	private final QPlaceContact placeContact = QPlaceContact.placeContact;
	private final QKeyword keyword = QKeyword.keyword;

	@Override
	public PlaceSearchResponse searchWithCursor(PlaceSearchRequest request) {
		long startTime = System.currentTimeMillis();

		// 요청 유효성 검증
		request.validate();

		// 커서 디코딩
		PlaceSearchCursor cursor = PlaceSearchCursor.decode(request.getCursor());

		// 기본 쿼리 빌드
		JPAQuery<PlaceInfo> query = buildBaseQuery(request);

		// 커서 조건 추가
		if (cursor != null) {
			applyCursorCondition(query, cursor, request);
		}

		// 정렬 조건 적용
		applyOrdering(query, request);

		// 페이지 크기 + 1로 조회 (hasNext 판단용)
		List<PlaceInfo> results = query
				.limit(request.getSize() + 1)
				.fetch();

		// 응답 생성
		boolean hasNext = results.size() > request.getSize();
		if (hasNext) {
			results = results.subList(0, request.getSize());
		}

		// DTO 변환
		List<PlaceSearchResponse.PlaceSearchItem> items = convertToItems(results, request);

		// 다음 커서 생성
		String nextCursor = null;
		if (hasNext && !results.isEmpty()) {
			PlaceInfo lastItem = results.get(results.size() - 1);
			nextCursor = createCursor(lastItem, request).encode();
		}

		// 메타데이터 생성
		PlaceSearchResponse.SearchMetadata metadata = PlaceSearchResponse.SearchMetadata.builder()
				.searchTime(System.currentTimeMillis() - startTime)
				.sortBy(request.getSortBy().name())
				.sortDirection(request.getSortDirection().name())
				.centerLat(request.getLatitude())
				.centerLng(request.getLongitude())
				.radiusInMeters(request.getRadiusInMeters())
				.build();

		return PlaceSearchResponse.builder()
				.items(items)
				.nextCursor(nextCursor)
				.hasNext(hasNext)
				.count(items.size())
				.metadata(metadata)
				.build();
	}

	@Override
	public PlaceSearchResponse searchByLocation(PlaceSearchRequest request) {
		if (!request.isLocationBasedSearch()) {
			return PlaceSearchResponse.empty();
		}

		// PostGIS를 활용한 위치 기반 검색
		String sql = """
				SELECT pi.*, pl.*,
				       ST_Distance(pl.coordinates, ST_MakePoint(:lng, :lat)::geography) as distance
				FROM place_info pi
				JOIN place_locations pl ON pi.id = pl.place_info_id
				WHERE pi.deleted_at IS NULL
				  AND pi.is_active = true
				  AND pi.approval_status = :approvalStatus
				  AND ST_DWithin(pl.coordinates, ST_MakePoint(:lng, :lat)::geography, :radius)
				ORDER BY distance
				LIMIT :limit
				""";

		List<Object[]> results = entityManager.createNativeQuery(sql)
				.setParameter("lat", request.getLatitude())
				.setParameter("lng", request.getLongitude())
				.setParameter("radius", request.getRadiusInMeters())
				.setParameter("approvalStatus", request.getApprovalStatus())
				.setParameter("limit", request.getSize())
				.getResultList();

		// 결과 변환 및 반환
		List<PlaceSearchResponse.PlaceSearchItem> items = convertNativeResults(results);

		return PlaceSearchResponse.builder()
				.items(items)
				.hasNext(items.size() >= request.getSize())
				.count(items.size())
				.build();
	}

	@Override
	public PlaceSearchResponse searchByKeywords(PlaceSearchRequest request) {
		if (request.getKeywordIds() == null || request.getKeywordIds().isEmpty()) {
			return searchWithCursor(request);
		}

		JPAQuery<PlaceInfo> query = queryFactory
				.selectFrom(placeInfo)
				.distinct()
				.leftJoin(placeInfo.keywords, keyword)
				.where(
						// 기본 필터
						placeInfo.deletedAt.isNull(),
						placeInfo.isActive.eq(true),
						placeInfo.approvalStatus.eq(ApprovalStatus.valueOf(request.getApprovalStatus())),
						// 키워드 필터
						keyword.id.in(request.getKeywordIds())
				);

		// 추가 조건 적용
		applyAdditionalConditions(query, request);

		// 정렬 적용
		applyOrdering(query, request);

		// 결과 조회
		List<PlaceInfo> results = query
				.limit(request.getSize())
				.fetch();

		// DTO 변환
		List<PlaceSearchResponse.PlaceSearchItem> items = convertToItems(results, request);

		return PlaceSearchResponse.builder()
				.items(items)
				.hasNext(results.size() >= request.getSize())
				.count(items.size())
				.build();
	}

	@Override
	public Long countSearchResults(PlaceSearchRequest request) {
		JPAQuery<PlaceInfo> query = buildBaseQuery(request);
		return query.fetchCount();
	}

	/**
	 * 기본 쿼리 빌드
	 */
	private JPAQuery<PlaceInfo> buildBaseQuery(PlaceSearchRequest request) {
		JPAQuery<PlaceInfo> query = queryFactory
				.selectFrom(placeInfo)
				.distinct();

		// 필요한 연관 엔티티 조인
		query.leftJoin(placeInfo.location, placeLocation).fetchJoin();
		query.leftJoin(placeInfo.parking, placeParking).fetchJoin();
		query.leftJoin(placeInfo.contact, placeContact).fetchJoin();

		// 기본 필터 (항상 적용)
		BooleanBuilder whereClause = new BooleanBuilder();
		whereClause.and(placeInfo.deletedAt.isNull());
		whereClause.and(placeInfo.isActive.eq(request.getIsActive()));
		whereClause.and(placeInfo.approvalStatus.eq(ApprovalStatus.valueOf(request.getApprovalStatus())));

		// 검색 조건 추가
		applySearchConditions(whereClause, request);

		query.where(whereClause);

		return query;
	}

	/**
	 * 검색 조건 적용
	 */
	private void applySearchConditions(BooleanBuilder builder, PlaceSearchRequest request) {
		// 키워드 검색
		if (StringUtils.hasText(request.getKeyword())) {
			builder.and(
					placeInfo.placeName.containsIgnoreCase(request.getKeyword())
							.or(placeInfo.description.containsIgnoreCase(request.getKeyword()))
							.or(placeInfo.category.containsIgnoreCase(request.getKeyword()))
			);
		}

		// 장소명 검색
		if (StringUtils.hasText(request.getPlaceName())) {
			builder.and(placeInfo.placeName.containsIgnoreCase(request.getPlaceName()));
		}

		// 카테고리 필터
		if (StringUtils.hasText(request.getCategory())) {
			builder.and(placeInfo.category.eq(request.getCategory()));
		}

		// 장소 타입 필터
		if (StringUtils.hasText(request.getPlaceType())) {
			builder.and(placeInfo.placeType.eq(request.getPlaceType()));
		}

		// 주차 가능 여부 필터
		if (request.getParkingAvailable() != null) {
			builder.and(placeParking.available.eq(request.getParkingAvailable()));
		}

		// 지역 필터
		if (request.hasRegionFilter()) {
			if (StringUtils.hasText(request.getProvince())) {
				builder.and(placeLocation.address.province.eq(request.getProvince()));
			}
			if (StringUtils.hasText(request.getCity())) {
				builder.and(placeLocation.address.city.eq(request.getCity()));
			}
			if (StringUtils.hasText(request.getDistrict())) {
				builder.and(placeLocation.address.district.eq(request.getDistrict()));
			}
		}
	}

	/**
	 * 커서 조건 적용
	 */
	private void applyCursorCondition(JPAQuery<PlaceInfo> query, PlaceSearchCursor cursor, PlaceSearchRequest request) {
		BooleanExpression cursorCondition = null;

		switch (request.getSortBy()) {
			case RATING -> {
				if (request.getSortDirection() == PlaceSearchRequest.SortDirection.DESC) {
					cursorCondition = placeInfo.ratingAverage.lt(cursor.getLastSortValue())
							.or(placeInfo.ratingAverage.eq(cursor.getLastSortValue())
									.and(placeInfo.id.gt(cursor.getLastId())));
				} else {
					cursorCondition = placeInfo.ratingAverage.gt(cursor.getLastSortValue())
							.or(placeInfo.ratingAverage.eq(cursor.getLastSortValue())
									.and(placeInfo.id.gt(cursor.getLastId())));
				}
			}
			case REVIEW_COUNT -> {
				if (request.getSortDirection() == PlaceSearchRequest.SortDirection.DESC) {
					cursorCondition = placeInfo.reviewCount.lt(cursor.getLastSortValue().intValue())
							.or(placeInfo.reviewCount.eq(cursor.getLastSortValue().intValue())
									.and(placeInfo.id.gt(cursor.getLastId())));
				} else {
					cursorCondition = placeInfo.reviewCount.gt(cursor.getLastSortValue().intValue())
							.or(placeInfo.reviewCount.eq(cursor.getLastSortValue().intValue())
									.and(placeInfo.id.gt(cursor.getLastId())));
				}
			}
			case CREATED_AT -> {
				LocalDateTime lastCreatedAt = LocalDateTime.ofEpochSecond(cursor.getLastSortValue().longValue(), 0, java.time.ZoneOffset.UTC);
				if (request.getSortDirection() == PlaceSearchRequest.SortDirection.DESC) {
					cursorCondition = placeInfo.createdAt.before(lastCreatedAt)
							.or(placeInfo.createdAt.eq(lastCreatedAt)
									.and(placeInfo.id.gt(cursor.getLastId())));
				} else {
					cursorCondition = placeInfo.createdAt.after(lastCreatedAt)
							.or(placeInfo.createdAt.eq(lastCreatedAt)
									.and(placeInfo.id.gt(cursor.getLastId())));
				}
			}
			case PLACE_NAME -> {
				if (request.getSortDirection() == PlaceSearchRequest.SortDirection.DESC) {
					cursorCondition = placeInfo.placeName.lt(cursor.getSecondarySortValue())
							.or(placeInfo.placeName.eq(cursor.getSecondarySortValue())
									.and(placeInfo.id.gt(cursor.getLastId())));
				} else {
					cursorCondition = placeInfo.placeName.gt(cursor.getSecondarySortValue())
							.or(placeInfo.placeName.eq(cursor.getSecondarySortValue())
									.and(placeInfo.id.gt(cursor.getLastId())));
				}
			}
			default -> cursorCondition = placeInfo.id.gt(cursor.getLastId());
		}

		if (cursorCondition != null) {
			query.where(cursorCondition);
		}
	}

	/**
	 * 정렬 조건 적용
	 */
	private void applyOrdering(JPAQuery<PlaceInfo> query, PlaceSearchRequest request) {
		List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

		switch (request.getSortBy()) {
			case RATING -> {
				orderSpecifiers.add(request.getSortDirection() == PlaceSearchRequest.SortDirection.DESC
						? placeInfo.ratingAverage.desc().nullsLast()
						: placeInfo.ratingAverage.asc().nullsFirst());
			}
			case REVIEW_COUNT -> {
				orderSpecifiers.add(request.getSortDirection() == PlaceSearchRequest.SortDirection.DESC
						? placeInfo.reviewCount.desc()
						: placeInfo.reviewCount.asc());
			}
			case CREATED_AT -> {
				orderSpecifiers.add(request.getSortDirection() == PlaceSearchRequest.SortDirection.DESC
						? placeInfo.createdAt.desc()
						: placeInfo.createdAt.asc());
			}
			case PLACE_NAME -> {
				orderSpecifiers.add(request.getSortDirection() == PlaceSearchRequest.SortDirection.DESC
						? placeInfo.placeName.desc()
						: placeInfo.placeName.asc());
			}
			default -> {
				// DISTANCE는 PostGIS 쿼리에서 처리
			}
		}

		// ID로 2차 정렬 (안정적인 페이징을 위해)
		orderSpecifiers.add(placeInfo.id.asc());

		query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]));
	}

	/**
	 * 추가 조건 적용
	 */
	private void applyAdditionalConditions(JPAQuery<PlaceInfo> query, PlaceSearchRequest request) {
		BooleanBuilder additionalConditions = new BooleanBuilder();
		applySearchConditions(additionalConditions, request);
		query.where(additionalConditions);
	}

	/**
	 * 엔티티를 DTO로 변환
	 */
	private List<PlaceSearchResponse.PlaceSearchItem> convertToItems(List<PlaceInfo> entities, PlaceSearchRequest request) {
		return entities.stream()
				.map(entity -> {
					PlaceSearchResponse.PlaceSearchItem.PlaceSearchItemBuilder builder = PlaceSearchResponse.PlaceSearchItem.builder()
							.id(entity.getId())
							.placeName(entity.getPlaceName())
							.description(entity.getDescription())
							.category(entity.getCategory())
							.placeType(entity.getPlaceType())
							.ratingAverage(entity.getRatingAverage())
							.reviewCount(entity.getReviewCount())
							.isActive(entity.getIsActive())
							.approvalStatus(entity.getApprovalStatus());

					// 위치 정보
					if (entity.getLocation() != null) {
						builder.fullAddress(entity.getLocation().getAddress().getFullAddress())
								.latitude(entity.getLocation().getLatitude())
								.longitude(entity.getLocation().getLongitude());
					}

					// 주차 정보
					if (entity.getParking() != null) {
						builder.parkingAvailable(entity.getParking().getAvailable())
								.parkingType(entity.getParking().getParkingType() != null ?
										entity.getParking().getParkingType().name() : null);
					}

					// 연락처
					if (entity.getContact() != null) {
						builder.contact(entity.getContact().getContact());
					}

					// 첫 번째 이미지를 썸네일로
					if (entity.getImages() != null && !entity.getImages().isEmpty()) {
						builder.thumbnailUrl(entity.getImages().get(0).getImageUrl());
					}

					// 키워드
					if (entity.getKeywords() != null) {
						List<String> keywordNames = entity.getKeywords().stream()
								.map(Keyword::getName)
								.collect(Collectors.toList());
						builder.keywords(keywordNames);
					}

					return builder.build();
				})
				.collect(Collectors.toList());
	}

	/**
	 * Native 쿼리 결과를 DTO로 변환
	 */
	private List<PlaceSearchResponse.PlaceSearchItem> convertNativeResults(List<Object[]> results) {
		// Native 쿼리 결과를 PlaceSearchItem으로 변환
		// 구현 생략 (실제 구현 시 필요한 필드 매핑)
		return new ArrayList<>();
	}

	/**
	 * 커서 생성
	 */
	private PlaceSearchCursor createCursor(PlaceInfo lastItem, PlaceSearchRequest request) {
		return switch (request.getSortBy()) {
			case RATING -> PlaceSearchCursor.forRating(
					lastItem.getId(),
					lastItem.getRatingAverage(),
					lastItem.getPlaceName(),
					null,
					true
			);
			case REVIEW_COUNT -> PlaceSearchCursor.forReviewCount(
					lastItem.getId(),
					lastItem.getReviewCount(),
					lastItem.getPlaceName(),
					null,
					true
			);
			case CREATED_AT -> PlaceSearchCursor.forCreatedAt(
					lastItem.getId(),
					lastItem.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC),
					null,
					true
			);
			default -> PlaceSearchCursor.builder()
					.lastId(lastItem.getId())
					.hasNext(true)
					.build();
		};
	}
}
