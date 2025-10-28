package com.teambind.placeinfoserver.place.dto.cursor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 커서 기반 페이징을 위한 커서 클래스
 * 다음 페이지 조회를 위한 정보를 Base64로 인코딩하여 클라이언트에 전달
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSearchCursor {
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * 마지막 조회된 항목의 ID
	 */
	private String lastId;
	
	/**
	 * 마지막 조회된 항목의 정렬 값
	 * (예: 평점, 리뷰 수, 거리 등)
	 */
	private Double lastSortValue;
	
	/**
	 * 두 번째 정렬 값 (동일한 값이 여러 개일 경우)
	 */
	private String secondarySortValue;
	
	/**
	 * 현재까지 조회된 전체 개수
	 */
	private Integer totalFetched;
	
	/**
	 * 다음 페이지 존재 여부
	 */
	private Boolean hasNext;
	
	/**
	 * Base64 문자열을 커서 객체로 디코딩
	 */
	public static PlaceSearchCursor decode(String cursor) {
		if (cursor == null || cursor.isEmpty()) {
			return null;
		}
		
		try {
			byte[] decodedBytes = Base64.getUrlDecoder().decode(cursor);
			String json = new String(decodedBytes, StandardCharsets.UTF_8);
			return objectMapper.readValue(json, PlaceSearchCursor.class);
		} catch (Exception e) {
			// 잘못된 커서는 무시하고 처음부터 조회
			return null;
		}
	}
	
	/**
	 * 거리 기반 커서 생성
	 */
	public static PlaceSearchCursor forDistance(String lastId, Double distance, Integer totalFetched, Boolean hasNext) {
		return PlaceSearchCursor.builder()
				.lastId(lastId)
				.lastSortValue(distance)
				.totalFetched(totalFetched)
				.hasNext(hasNext)
				.build();
	}
	
	/**
	 * 평점 기반 커서 생성
	 */
	public static PlaceSearchCursor forRating(String lastId, Double rating, String placeName, Integer totalFetched, Boolean hasNext) {
		return PlaceSearchCursor.builder()
				.lastId(lastId)
				.lastSortValue(rating)
				.secondarySortValue(placeName)
				.totalFetched(totalFetched)
				.hasNext(hasNext)
				.build();
	}
	
	/**
	 * 리뷰 수 기반 커서 생성
	 */
	public static PlaceSearchCursor forReviewCount(String lastId, Integer reviewCount, String placeName, Integer totalFetched, Boolean hasNext) {
		return PlaceSearchCursor.builder()
				.lastId(lastId)
				.lastSortValue(reviewCount.doubleValue())
				.secondarySortValue(placeName)
				.totalFetched(totalFetched)
				.hasNext(hasNext)
				.build();
	}
	
	/**
	 * 생성일 기반 커서 생성
	 */
	public static PlaceSearchCursor forCreatedAt(String lastId, Long timestamp, Integer totalFetched, Boolean hasNext) {
		return PlaceSearchCursor.builder()
				.lastId(lastId)
				.lastSortValue(timestamp.doubleValue())
				.totalFetched(totalFetched)
				.hasNext(hasNext)
				.build();
	}
	
	/**
	 * 커서를 Base64 문자열로 인코딩
	 */
	@JsonIgnore
	public String encode() {
		try {
			String json = objectMapper.writeValueAsString(this);
			return Base64.getUrlEncoder()
					.withoutPadding()
					.encodeToString(json.getBytes(StandardCharsets.UTF_8));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("커서 인코딩 실패", e);
		}
	}
}
