package com.teambind.placeinfoserver.place.service;

import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.Keyword;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.dto.request.PlaceSearchRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceSearchResponse;
import com.teambind.placeinfoserver.place.fixture.PlaceRequestFactory;
import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import com.teambind.placeinfoserver.place.repository.KeywordRepository;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PlaceAdvancedSearchService 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("PlaceAdvancedSearchService 통합 테스트")
class PlaceAdvancedSearchServiceTest extends BaseIntegrationTest {
	
	@Autowired
	private PlaceAdvancedSearchService searchService;
	
	@Autowired
	private PlaceInfoRepository placeInfoRepository;
	
	@Autowired
	private KeywordRepository keywordRepository;
	
	@Autowired
	private EntityManager entityManager;
	
	@BeforeEach
	void setUp() {
		PlaceTestFactory.resetSequence();
		placeInfoRepository.deleteAll();
		keywordRepository.deleteAll();
	}
	
	@Nested
	@DisplayName("통합 검색 테스트")
	class IntegratedSearchTests {
		
		@Test
		@DisplayName("기본 검색이 정상 동작한다")
		void basicSearchWorks() {
			// Given
			PlaceInfo place1 = PlaceTestFactory.createPlaceInfo();
			PlaceInfo place2 = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.saveAll(List.of(place1, place2));
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.createBasicSearchRequest();
			
			// When
			PlaceSearchResponse response = searchService.search(request);
			
			// Then
			assertThat(response).isNotNull();
			assertThat(response.getItems()).hasSize(2);
			assertThat(response.getMetadata()).isNotNull();
		}
		
		@Test
		@DisplayName("키워드 검색이 정상 동작한다")
		void keywordSearchWorks() {
			// Given
			PlaceInfo match = PlaceTestFactory.builder()
					.placeName("드럼 연습실")
					.build();
			PlaceInfo noMatch = PlaceTestFactory.builder()
					.placeName("기타 연습실")
					.build();
			
			placeInfoRepository.saveAll(List.of(match, noMatch));
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.createKeywordSearchRequest("드럼");
			
			// When
			PlaceSearchResponse response = searchService.search(request);
			
			// Then
			assertThat(response.getItems()).hasSize(1);
			assertThat(response.getItems().get(0).getPlaceName()).contains("드럼");
		}
		
		@Test
		@DisplayName("키워드 태그 검색이 정상 동작한다")
		void keywordTagSearchWorks() {
			// Given
			Keyword drumKeyword = PlaceTestFactory.createKeyword("드럼");
			keywordRepository.save(drumKeyword);
			
			PlaceInfo place = PlaceTestFactory.createPlaceInfo();
			place = PlaceTestFactory.withKeywords(place, List.of(drumKeyword));
			placeInfoRepository.save(place);
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.createKeywordTagSearchRequest(
					List.of(drumKeyword.getId())
			);
			
			// When
			PlaceSearchResponse response = searchService.search(request);
			
			// Then
			assertThat(response.getItems()).hasSize(1);
			assertThat(response.getItems().get(0).getKeywords()).contains("드럼");
		}
	}
	
	@Nested
	@DisplayName("커서 기반 검색 테스트")
	class CursorSearchTests {
		
		@Test
		@DisplayName("커서 기반 페이징이 정상 동작한다")
		void cursorPaginationWorks() {
			// Given - 각각 다른 rating 값을 가진 PlaceInfo 생성
			for (int i = 0; i < 10; i++) {
				placeInfoRepository.save(
						PlaceTestFactory.builder()
								.rating(5.0 - (i * 0.1)) // 5.0, 4.9, 4.8, ..., 4.1
								.build()
				);
			}
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.searchRequestBuilder()
					.sortBy(PlaceSearchRequest.SortBy.RATING)
					.sortDirection(PlaceSearchRequest.SortDirection.DESC)
					.size(5)
					.build();
			
			// When
			PlaceSearchResponse firstPage = searchService.searchWithCursor(request);
			
			// Then
			assertThat(firstPage.getItems()).hasSize(5);
			assertThat(firstPage.getHasNext()).isTrue();
			assertThat(firstPage.getNextCursor()).isNotNull();
			
			// When - 다음 페이지 조회
			PlaceSearchRequest nextRequest = PlaceRequestFactory.createCursorSearchRequest(
					firstPage.getNextCursor(),
					5
			);
			nextRequest.setSortBy(PlaceSearchRequest.SortBy.RATING);
			nextRequest.setSortDirection(PlaceSearchRequest.SortDirection.DESC);
			PlaceSearchResponse secondPage = searchService.searchWithCursor(nextRequest);
			
			// Then
			assertThat(secondPage.getItems()).hasSize(5);
			assertThat(secondPage.getHasNext()).isFalse();
		}
		
		@Test
		@DisplayName("결과가 없을 때 빈 응답을 반환한다")
		void returnsEmptyResponseWhenNoResults() {
			// Given
			PlaceSearchRequest request = PlaceRequestFactory.createKeywordSearchRequest("존재하지않음");
			
			// When
			PlaceSearchResponse response = searchService.searchWithCursor(request);
			
			// Then
			assertThat(response.getItems()).isEmpty();
			assertThat(response.getCount()).isZero();
			assertThat(response.getHasNext()).isFalse();
		}
	}
	
	@Nested
	@DisplayName("위치 기반 검색 테스트")
	class LocationSearchTests {
		
		@Test
		@DisplayName("위치 정보 없이 위치 검색 시 예외가 발생한다")
		void throwsExceptionWhenLocationMissing() {
			// Given
			PlaceSearchRequest request = PlaceRequestFactory.searchRequestBuilder()
					.latitude(null)
					.longitude(null)
					.build();
			
			// When & Then
			assertThatThrownBy(() -> searchService.searchByLocation(request))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("위치 정보");
		}
		
		@Test
		@DisplayName("유효하지 않은 위도 범위 시 예외가 발생한다")
		void throwsExceptionForInvalidLatitude() {
			// Given
			PlaceSearchRequest request = PlaceRequestFactory.createLocationSearchRequest(
					100.0, // 유효하지 않은 위도
					127.0,
					5000
			);
			
			// When & Then
			assertThatThrownBy(() -> searchService.searchByLocation(request))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("위도");
		}
		
		@Test
		@DisplayName("유효하지 않은 경도 범위 시 예외가 발생한다")
		void throwsExceptionForInvalidLongitude() {
			// Given
			PlaceSearchRequest request = PlaceRequestFactory.createLocationSearchRequest(
					37.5,
					200.0, // 유효하지 않은 경도
					5000
			);
			
			// When & Then
			assertThatThrownBy(() -> searchService.searchByLocation(request))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("경도");
		}
	}
	
	@Nested
	@DisplayName("키워드 검색 테스트")
	class KeywordSearchServiceTests {
		
		@Test
		@DisplayName("키워드 ID가 비어있으면 빈 응답을 반환한다")
		void returnsEmptyWhenKeywordIdsEmpty() {
			// Given
			PlaceSearchRequest request = PlaceRequestFactory.searchRequestBuilder()
					.keywordIds(List.of())
					.build();
			
			// When
			PlaceSearchResponse response = searchService.searchByKeywords(request);
			
			// Then
			assertThat(response.getItems()).isEmpty();
		}
		
		@Test
		@DisplayName("키워드가 NULL이면 빈 응답을 반환한다")
		void returnsEmptyWhenKeywordIdsNull() {
			// Given
			PlaceSearchRequest request = PlaceRequestFactory.searchRequestBuilder()
					.keywordIds(null)
					.build();
			
			// When
			PlaceSearchResponse response = searchService.searchByKeywords(request);
			
			// Then
			assertThat(response.getItems()).isEmpty();
		}
	}
	
	@Nested
	@DisplayName("지역 기반 검색 테스트")
	class RegionSearchTests {
		
		@Test
		@DisplayName("지역별 검색이 정상 동작한다")
		void regionSearchWorks() {
			// Given
			PlaceInfo seoulPlace = PlaceTestFactory.builder()
					.placeName("서울 연습실")
					.location(37.5, 127.0)
					.build();
			
			placeInfoRepository.save(seoulPlace);
			entityManager.flush();
			entityManager.clear();
			
			// When
			PlaceSearchResponse response = searchService.searchByRegion(
					"서울특별시",
					"강남구",
					"역삼동",
					null,
					10
			);
			
			// Then
			assertThat(response).isNotNull();
			assertThat(response.getItems()).isNotEmpty();
		}
		
		@Test
		@DisplayName("지역 검색 시 페이지 크기를 지정하지 않으면 기본값 20이 적용된다")
		void usesDefaultSizeForRegionSearch() {
			// Given
			for (int i = 0; i < 25; i++) {
				placeInfoRepository.save(PlaceTestFactory.createPlaceInfo());
			}
			entityManager.flush();
			entityManager.clear();
			
			// When
			PlaceSearchResponse response = searchService.searchByRegion(
					"서울특별시",
					"강남구",
					null,
					null,
					null  // size를 null로 전달
			);
			
			// Then
			assertThat(response.getItems().size()).isLessThanOrEqualTo(20);
		}
	}
	
	@Nested
	@DisplayName("인기 장소 조회 테스트")
	class PopularPlacesTests {
		
		@Test
		@DisplayName("평점 높은 순으로 인기 장소를 조회한다")
		void getsPopularPlacesByRating() {
			// Given
			PlaceInfo lowRating = PlaceTestFactory.builder().rating(3.5).build();
			PlaceInfo midRating = PlaceTestFactory.builder().rating(4.2).build();
			PlaceInfo highRating = PlaceTestFactory.builder().rating(4.8).build();
			
			placeInfoRepository.saveAll(List.of(lowRating, midRating, highRating));
			entityManager.flush();
			entityManager.clear();
			
			// When
			PlaceSearchResponse response = searchService.getPopularPlaces(10);
			
			// Then
			assertThat(response.getItems()).hasSize(3);
			assertThat(response.getItems().get(0).getRatingAverage()).isEqualTo(4.8);
			assertThat(response.getItems().get(1).getRatingAverage()).isEqualTo(4.2);
			assertThat(response.getItems().get(2).getRatingAverage()).isEqualTo(3.5);
		}
		
		@Test
		@DisplayName("인기 장소 조회 시 기본 크기는 10이다")
		void usesDefaultSizeForPopularPlaces() {
			// Given
			for (int i = 0; i < 15; i++) {
				placeInfoRepository.save(
						PlaceTestFactory.builder().rating(4.0 + i * 0.1).build()
				);
			}
			entityManager.flush();
			entityManager.clear();
			
			// When
			PlaceSearchResponse response = searchService.getPopularPlaces(null);
			
			// Then
			assertThat(response.getItems().size()).isLessThanOrEqualTo(10);
		}
	}
	
	@Nested
	@DisplayName("최신 장소 조회 테스트")
	class RecentPlacesTests {
		
		@Test
		@DisplayName("최신 등록순으로 장소를 조회한다")
		void getsRecentPlaces() {
			// Given
			for (int i = 0; i < 5; i++) {
				placeInfoRepository.save(PlaceTestFactory.createPlaceInfo());
			}
			entityManager.flush();
			entityManager.clear();
			
			// When
			PlaceSearchResponse response = searchService.getRecentPlaces(10);
			
			// Then
			assertThat(response.getItems()).hasSize(5);
		}
		
		@Test
		@DisplayName("최신 장소 조회 시 기본 크기는 10이다")
		void usesDefaultSizeForRecentPlaces() {
			// Given
			for (int i = 0; i < 15; i++) {
				placeInfoRepository.save(PlaceTestFactory.createPlaceInfo());
			}
			entityManager.flush();
			entityManager.clear();
			
			// When
			PlaceSearchResponse response = searchService.getRecentPlaces(null);
			
			// Then
			assertThat(response.getItems().size()).isLessThanOrEqualTo(10);
		}
	}
	
	@Nested
	@DisplayName("유효성 검증 테스트")
	class ValidationTests {
		
		@Test
		@DisplayName("페이지 크기가 100을 초과하면 100으로 조정된다")
		void limitsPageSizeTo100() {
			// Given
			for (int i = 0; i < 150; i++) {
				placeInfoRepository.save(PlaceTestFactory.createPlaceInfo());
			}
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.searchRequestBuilder()
					.size(150) // 100 초과
					.build();
			
			// When
			PlaceSearchResponse response = searchService.search(request);
			
			// Then
			assertThat(request.getSize()).isEqualTo(100); // 자동 조정됨
			assertThat(response.getItems().size()).isLessThanOrEqualTo(100);
		}
		
		@Test
		@DisplayName("검색 요청 유효성 검증이 동작한다")
		void validatesSearchRequest() {
			// Given
			PlaceSearchRequest validRequest = PlaceRequestFactory.createLocationSearchRequest(
					37.5,
					127.0,
					5000
			);
			
			// When & Then
			assertThatCode(() -> searchService.search(validRequest))
					.doesNotThrowAnyException();
		}
	}
	
	@Nested
	@DisplayName("검색 결과 카운트 테스트")
	class CountResultsTests {
		
		@Test
		@DisplayName("검색 조건에 맞는 전체 개수를 반환한다")
		void countsSearchResults() {
			// Given
			for (int i = 0; i < 25; i++) {
				placeInfoRepository.save(
						PlaceTestFactory.builder()
								.category("연습실")
								.build()
				);
			}
			for (int i = 0; i < 10; i++) {
				placeInfoRepository.save(
						PlaceTestFactory.builder()
								.category("스튜디오")
								.build()
				);
			}
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.createCategorySearchRequest("연습실");
			
			// When
			Long count = searchService.countSearchResults(request);
			
			// Then
			assertThat(count).isEqualTo(25);
		}
		
		@Test
		@DisplayName("결과가 없을 때 0을 반환한다")
		void returnsZeroWhenNoResults() {
			// Given
			PlaceSearchRequest request = PlaceRequestFactory.createKeywordSearchRequest("존재하지않음");
			
			// When
			Long count = searchService.countSearchResults(request);
			
			// Then
			assertThat(count).isZero();
		}
	}
	
	@Nested
	@DisplayName("복합 조건 검색 테스트")
	class ComplexSearchTests {
		
		@Test
		@DisplayName("여러 조건을 조합하여 검색할 수 있다")
		void searchesWithMultipleConditions() {
			// Given
			PlaceInfo match = PlaceTestFactory.builder()
					.placeName("서울 드럼 연습실")
					.category("연습실")
					.parking(true, null)
					.rating(4.5)
					.build();
			
			PlaceInfo noMatch1 = PlaceTestFactory.builder()
					.placeName("서울 기타 연습실")
					.category("연습실")
					.parking(false, null) // 주차 불가
					.rating(4.5)
					.build();
			
			placeInfoRepository.saveAll(List.of(match, noMatch1));
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.createComplexSearchRequest(
					"드럼",
					"연습실",
					true,
					"서울특별시",
					"강남구"
			);
			
			// When
			PlaceSearchResponse response = searchService.search(request);
			
			// Then
			assertThat(response.getItems()).hasSize(1);
			assertThat(response.getItems().get(0).getPlaceName()).contains("드럼");
		}
	}
	
	@Nested
	@DisplayName("에러 처리 테스트")
	class ErrorHandlingTests {
		
		@Test
		@DisplayName("검색 중 예외 발생 시 빈 응답을 반환한다")
		void returnsEmptyResponseOnException() {
			// Given
			PlaceSearchRequest invalidRequest = PlaceSearchRequest.builder()
					.size(-1) // 잘못된 페이지 크기
					.build();
			
			// When
			PlaceSearchResponse response = searchService.searchWithCursor(invalidRequest);
			
			// Then
			// 예외를 던지지 않고 빈 응답 반환
			assertThat(response).isNotNull();
		}
		
		@Test
		@DisplayName("카운트 조회 중 예외 발생 시 0을 반환한다")
		void returnsZeroOnCountException() {
			// Given
			PlaceSearchRequest invalidRequest = PlaceSearchRequest.builder()
					.size(-1)
					.build();
			
			// When
			Long count = searchService.countSearchResults(invalidRequest);
			
			// Then
			assertThat(count).isEqualTo(0L);
		}
	}
	
	@Nested
	@DisplayName("응답 메타데이터 테스트")
	class MetadataTests {
		
		@Test
		@DisplayName("검색 응답에 메타데이터가 포함된다")
		void includesMetadataInResponse() {
			// Given
			placeInfoRepository.save(PlaceTestFactory.createPlaceInfo());
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.searchRequestBuilder()
					.sortBy(PlaceSearchRequest.SortBy.RATING)
					.sortDirection(PlaceSearchRequest.SortDirection.DESC)
					.build();
			
			// When
			PlaceSearchResponse response = searchService.search(request);
			
			// Then
			assertThat(response.getMetadata()).isNotNull();
			assertThat(response.getMetadata().getSortBy()).isEqualTo("RATING");
			assertThat(response.getMetadata().getSortDirection()).isEqualTo("DESC");
			assertThat(response.getMetadata().getSearchTime()).isNotNull();
		}
	}
}
