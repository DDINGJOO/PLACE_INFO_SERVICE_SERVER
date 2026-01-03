package com.teambind.placeinfoserver.place.repository.impl;

import com.teambind.placeinfoserver.place.config.QueryDslTestConfig;
import com.teambind.placeinfoserver.place.domain.entity.Keyword;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.dto.request.PlaceSearchRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceSearchResponse;
import com.teambind.placeinfoserver.place.fixture.PlaceRequestFactory;
import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import com.teambind.placeinfoserver.place.repository.KeywordRepository;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PlaceAdvancedSearchRepositoryImpl 통합 테스트
 * PostgreSQL + PostGIS 컨테이너를 사용하여 실제 DB 환경과 동일하게 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({PlaceAdvancedSearchRepositoryImpl.class, QueryDslTestConfig.class, com.teambind.placeinfoserver.place.config.JpaAuditingTestConfig.class})
@DisplayName("PlaceAdvancedSearchRepositoryImpl 통합 테스트")
class PlaceAdvancedSearchRepositoryImplTest {
	
	private static final PostgreSQLContainer<?> postgresContainer;
	
	static {
		postgresContainer = new PostgreSQLContainer<>(
				DockerImageName.parse("postgis/postgis:15-3.3")
						.asCompatibleSubstituteFor("postgres")
		)
				.withDatabaseName("testdb")
				.withUsername("test")
				.withPassword("test")
				.withReuse(true);
		postgresContainer.start();
	}
	
	@Autowired
	private PlaceAdvancedSearchRepositoryImpl searchRepository;
	@Autowired
	private PlaceInfoRepository placeInfoRepository;
	@Autowired
	private KeywordRepository keywordRepository;
	@Autowired
	private EntityManager entityManager;
	
	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgresContainer::getUsername);
		registry.add("spring.datasource.password", postgresContainer::getPassword);
	}
	
	@BeforeEach
	void setUp() {
		PlaceTestFactory.resetSequence();
		// 테스트 데이터 초기화는 각 테스트에서 수행
	}
	
	@AfterEach
	void tearDown() {
		placeInfoRepository.deleteAll();
		keywordRepository.deleteAll();
	}
	
	@Nested
	@DisplayName("기본 검색 기능 테스트")
	class BasicSearchTests {
		
		@Test
		@DisplayName("활성화된 승인된 장소만 검색된다")
		void searchOnlyActiveApprovedPlaces() {
			// Given
			PlaceInfo approved = PlaceTestFactory.createPlaceInfo();
			PlaceInfo pending = PlaceTestFactory.createPendingPlaceInfo();
			PlaceInfo rejected = PlaceTestFactory.createRejectedPlaceInfo();
			PlaceInfo inactive = PlaceTestFactory.createInactivePlaceInfo();
			
			placeInfoRepository.saveAll(List.of(approved, pending, rejected, inactive));
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.createBasicSearchRequest();
			
			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			
			// Then
			assertThat(response).isNotNull();
			assertThat(response.getItems()).hasSize(1);
			assertThat(response.getItems().get(0).getId()).isEqualTo(String.valueOf(approved.getId()));
			assertThat(response.getMetadata()).isNotNull();
			assertThat(response.getMetadata().getSearchTime()).isPositive();
		}
		
		@Test
		@DisplayName("키워드로 장소명, 설명, 카테고리를 검색할 수 있다")
		void searchByKeyword() {
			// Given
			PlaceInfo place1 = PlaceTestFactory.builder()
					.placeName("드럼 연습실")
					.description("드럼 전문 연습 공간")
					.category("연습실")
					.build();
			
			PlaceInfo place2 = PlaceTestFactory.builder()
					.placeName("기타 연습실")
					.description("기타와 베이스 연습 가능")
					.category("연습실")
					.build();
			
			PlaceInfo place3 = PlaceTestFactory.builder()
					.placeName("댄스 스튜디오")
					.description("댄스 연습 공간")
					.category("스튜디오")
					.build();
			
			placeInfoRepository.saveAll(List.of(place1, place2, place3));
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.createKeywordSearchRequest("드럼");
			
			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			
			// Then
			assertThat(response.getItems()).hasSize(1);
			assertThat(response.getItems().get(0).getPlaceName()).contains("드럼");
		}
		
		@Test
		@DisplayName("카테고리로 필터링할 수 있다")
		void searchByCategory() {
			// Given
			PlaceInfo practice1 = PlaceTestFactory.builder().category("연습실").build();
			PlaceInfo practice2 = PlaceTestFactory.builder().category("연습실").build();
			PlaceInfo studio = PlaceTestFactory.builder().category("스튜디오").build();
			
			placeInfoRepository.saveAll(List.of(practice1, practice2, studio));
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.createCategorySearchRequest("연습실");
			
			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			
			// Then
			assertThat(response.getItems()).hasSize(2);
			assertThat(response.getItems())
					.allMatch(item -> "연습실".equals(item.getCategory()));
		}
		
		@Test
		@DisplayName("주차 가능 여부로 필터링할 수 있다")
		void searchByParkingAvailability() {
			// Given
			PlaceInfo withParking = PlaceTestFactory.createPlaceInfo(); // 주차 가능
			PlaceInfo noParking = PlaceTestFactory.createPlaceInfo();
			noParking.setParking(PlaceTestFactory.createNoParkingParking(noParking));
			
			placeInfoRepository.saveAll(List.of(withParking, noParking));
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.createParkingSearchRequest(true);
			
			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			
			// Then
			assertThat(response.getItems()).hasSize(1);
			assertThat(response.getItems().get(0).getParkingAvailable()).isTrue();
		}
	}
	
	@Nested
	@DisplayName("정렬 기능 테스트")
	class SortingTests {
		
		@Test
		@DisplayName("평점순으로 정렬할 수 있다 (내림차순)")
		void sortByRatingDesc() {
			// Given
			PlaceInfo place1 = PlaceTestFactory.builder().placeName("A").rating(4.5).build();
			PlaceInfo place2 = PlaceTestFactory.builder().placeName("B").rating(4.8).build();
			PlaceInfo place3 = PlaceTestFactory.builder().placeName("C").rating(4.2).build();
			
			placeInfoRepository.saveAll(List.of(place1, place2, place3));
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.searchRequestBuilder()
					.sortBy(PlaceSearchRequest.SortBy.RATING)
					.sortDirection(PlaceSearchRequest.SortDirection.DESC)
					.build();
			
			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			
			// Then
			assertThat(response.getItems()).hasSize(3);
			assertThat(response.getItems().get(0).getRatingAverage()).isEqualTo(4.8);
			assertThat(response.getItems().get(1).getRatingAverage()).isEqualTo(4.5);
			assertThat(response.getItems().get(2).getRatingAverage()).isEqualTo(4.2);
		}
		
		@Test
		@DisplayName("리뷰 수순으로 정렬할 수 있다")
		void sortByReviewCount() {
			// Given
			PlaceInfo place1 = PlaceTestFactory.builder().reviewCount(10).build();
			PlaceInfo place2 = PlaceTestFactory.builder().reviewCount(50).build();
			PlaceInfo place3 = PlaceTestFactory.builder().reviewCount(30).build();
			
			placeInfoRepository.saveAll(List.of(place1, place2, place3));
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.searchRequestBuilder()
					.sortBy(PlaceSearchRequest.SortBy.REVIEW_COUNT)
					.sortDirection(PlaceSearchRequest.SortDirection.DESC)
					.build();
			
			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			
			// Then
			assertThat(response.getItems()).hasSize(3);
			assertThat(response.getItems().get(0).getReviewCount()).isEqualTo(50);
			assertThat(response.getItems().get(1).getReviewCount()).isEqualTo(30);
			assertThat(response.getItems().get(2).getReviewCount()).isEqualTo(10);
		}
		
		@Test
		@DisplayName("장소명순으로 정렬할 수 있다")
		void sortByPlaceName() {
			// Given
			PlaceInfo placeC = PlaceTestFactory.builder().placeName("C연습실").build();
			PlaceInfo placeA = PlaceTestFactory.builder().placeName("A연습실").build();
			PlaceInfo placeB = PlaceTestFactory.builder().placeName("B연습실").build();
			
			placeInfoRepository.saveAll(List.of(placeC, placeA, placeB));
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.searchRequestBuilder()
					.sortBy(PlaceSearchRequest.SortBy.PLACE_NAME)
					.sortDirection(PlaceSearchRequest.SortDirection.ASC)
					.build();
			
			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			
			// Then
			assertThat(response.getItems()).hasSize(3);
			assertThat(response.getItems().get(0).getPlaceName()).isEqualTo("A연습실");
			assertThat(response.getItems().get(1).getPlaceName()).isEqualTo("B연습실");
			assertThat(response.getItems().get(2).getPlaceName()).isEqualTo("C연습실");
		}
	}
	
	@Nested
	@DisplayName("페이징 기능 테스트")
	class PagingTests {
		
		@Test
		@DisplayName("페이지 크기만큼만 결과를 반환한다")
		void respectsPageSize() {
			// Given
			for (int i = 0; i < 15; i++) {
				placeInfoRepository.save(PlaceTestFactory.createPlaceInfo());
			}
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.searchRequestBuilder()
					.size(5)
					.build();
			
			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			
			// Then
			assertThat(response.getItems()).hasSize(5);
			assertThat(response.getCount()).isEqualTo(5);
			assertThat(response.getHasNext()).isTrue();
			assertThat(response.getNextCursor()).isNotNull();
		}
		
		@Test
		@DisplayName("다음 페이지가 없으면 hasNext가 false이다")
		void hasNextIsFalseWhenNoMoreData() {
			// Given
			placeInfoRepository.save(PlaceTestFactory.createPlaceInfo());
			placeInfoRepository.save(PlaceTestFactory.createPlaceInfo());
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.searchRequestBuilder()
					.size(10)
					.build();
			
			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			
			// Then
			assertThat(response.getItems()).hasSize(2);
			assertThat(response.getHasNext()).isFalse();
			assertThat(response.getNextCursor()).isNull();
		}
		
		@Test
		@DisplayName("커서를 사용하여 다음 페이지를 조회할 수 있다")
		void canNavigateUsingCursor() {
			// Given
			for (int i = 0; i < 10; i++) {
				placeInfoRepository.save(
						PlaceTestFactory.builder()
								.placeName("Place " + i)
								.rating(5.0 - i * 0.1)
								.build()
				);
			}
			entityManager.flush();
			entityManager.clear();
			
			// 첫 번째 페이지 조회
			PlaceSearchRequest firstRequest = PlaceRequestFactory.searchRequestBuilder()
					.size(3)
					.sortBy(PlaceSearchRequest.SortBy.RATING)
					.sortDirection(PlaceSearchRequest.SortDirection.DESC)
					.build();
			
			PlaceSearchResponse firstResponse = searchRepository.searchWithCursor(firstRequest);
			
			// When - 두 번째 페이지 조회
			PlaceSearchRequest secondRequest = PlaceRequestFactory.createCursorSearchRequest(
					firstResponse.getNextCursor(),
					3
			);
			secondRequest.setSortBy(PlaceSearchRequest.SortBy.RATING);
			secondRequest.setSortDirection(PlaceSearchRequest.SortDirection.DESC);
			
			PlaceSearchResponse secondResponse = searchRepository.searchWithCursor(secondRequest);
			
			// Then
			assertThat(firstResponse.getItems()).hasSize(3);
			assertThat(secondResponse.getItems()).hasSize(3);
			
			// 첫 번째 페이지와 두 번째 페이지의 데이터가 중복되지 않아야 함
			assertThat(firstResponse.getItems().get(2).getRatingAverage())
					.isGreaterThan(secondResponse.getItems().get(0).getRatingAverage());
		}
	}
	
	@Nested
	@DisplayName("키워드 태그 검색 테스트")
	class KeywordSearchTests {
		
		@Test
		@DisplayName("특정 키워드를 가진 장소만 검색된다")
		void searchByKeywordTags() {
			// Given
			Keyword drumKeyword = PlaceTestFactory.createKeyword("드럼");
			Keyword guitarKeyword = PlaceTestFactory.createKeyword("기타");
			keywordRepository.saveAll(List.of(drumKeyword, guitarKeyword));
			
			PlaceInfo place1 = PlaceTestFactory.createPlaceInfo();
			place1 = PlaceTestFactory.withKeywords(place1, List.of(drumKeyword));
			
			PlaceInfo place2 = PlaceTestFactory.createPlaceInfo();
			place2 = PlaceTestFactory.withKeywords(place2, List.of(guitarKeyword));
			
			PlaceInfo place3 = PlaceTestFactory.createPlaceInfo();
			place3 = PlaceTestFactory.withKeywords(place3, List.of(drumKeyword, guitarKeyword));
			
			placeInfoRepository.saveAll(List.of(place1, place2, place3));
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.createKeywordTagSearchRequest(
					List.of(drumKeyword.getId())
			);
			
			// When
			PlaceSearchResponse response = searchRepository.searchByKeywords(request);
			
			// Then
			assertThat(response.getItems()).hasSize(2);
			assertThat(response.getItems())
					.allMatch(item -> item.getKeywords() != null && item.getKeywords().contains("드럼"));
		}
	}
	
	@Nested
	@DisplayName("복합 조건 검색 테스트")
	class ComplexSearchTests {
		
		@Test
		@DisplayName("여러 조건을 조합하여 검색할 수 있다")
		void searchWithMultipleConditions() {
			// Given
			PlaceInfo match = PlaceTestFactory.builder()
					.placeName("서울 강남 드럼 연습실")
					.category("연습실")
					.parking(true, null)
					.build();
			
			PlaceInfo noMatch1 = PlaceTestFactory.builder()
					.placeName("서울 강남 기타 연습실")
					.category("스튜디오") // 카테고리 불일치
					.parking(true, null)
					.build();
			
			PlaceInfo noMatch2 = PlaceTestFactory.builder()
					.placeName("서울 강남 드럼 연습실")
					.category("연습실")
					.parking(false, null) // 주차 불가
					.build();
			
			placeInfoRepository.saveAll(List.of(match, noMatch1, noMatch2));
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
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			
			// Then
			assertThat(response.getItems()).hasSize(1);
			assertThat(response.getItems().get(0).getId()).isEqualTo(String.valueOf(match.getId()));
		}
	}
	
	@Nested
	@DisplayName("결과 카운트 테스트")
	class CountTests {
		
		@Test
		@DisplayName("검색 조건에 맞는 전체 개수를 반환한다")
		void countSearchResults() {
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
			Long count = searchRepository.countSearchResults(request);
			
			// Then
			assertThat(count).isEqualTo(25);
		}
	}
	
	@Nested
	@DisplayName("DTO 변환 테스트")
	class DTOConversionTests {
		
		@Test
		@DisplayName("모든 필드가 올바르게 DTO로 변환된다")
		void convertsAllFieldsCorrectly() {
			// Given
			PlaceInfo place = PlaceTestFactory.createPlaceInfo();
			place = PlaceTestFactory.withImages(place, 3);
			
			placeInfoRepository.save(place);
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.createBasicSearchRequest();
			
			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			
			// Then
			assertThat(response.getItems()).hasSize(1);
			PlaceSearchResponse.PlaceSearchItem item = response.getItems().get(0);
			
			assertThat(item.getId()).isEqualTo(String.valueOf(place.getId()));
			assertThat(item.getPlaceName()).isEqualTo(place.getPlaceName());
			assertThat(item.getDescription()).isEqualTo(place.getDescription());
			assertThat(item.getCategory()).isEqualTo(place.getCategory());
			assertThat(item.getPlaceType()).isEqualTo(place.getPlaceType());
			assertThat(item.getRatingAverage()).isEqualTo(place.getRatingAverage());
			assertThat(item.getReviewCount()).isEqualTo(place.getReviewCount());
			assertThat(item.getIsActive()).isEqualTo(place.getIsActive());
			assertThat(item.getApprovalStatus()).isEqualTo(place.getApprovalStatus().name());
			
			// 위치 정보
			assertThat(item.getFullAddress()).isNotNull();
			assertThat(item.getLatitude()).isEqualTo(place.getLocation().getLatitude());
			assertThat(item.getLongitude()).isEqualTo(place.getLocation().getLongitude());
			
			// 주차 정보
			assertThat(item.getParkingAvailable()).isTrue();
			assertThat(item.getParkingType()).isNotNull();
			
			// 연락처
			assertThat(item.getContact()).isNotNull();
			
			// 썸네일 (첫 번째 이미지)
			assertThat(item.getThumbnailUrl()).isNotNull();
		}
	}
	
	@Nested
	@DisplayName("등록 상태 필터 테스트")
	class RegistrationStatusFilterTests {

		@Test
		@DisplayName("등록 업체만 필터링하여 조회한다")
		void filterByRegisteredStatus() {
			// Given
			PlaceInfo registered = PlaceTestFactory.builder().placeName("등록 업체").registered().build();
			PlaceInfo unregistered = PlaceTestFactory.builder().placeName("미등록 업체").unregistered().build();

			placeInfoRepository.saveAll(List.of(registered, unregistered));
			entityManager.flush();
			entityManager.clear();

			PlaceSearchRequest request = PlaceRequestFactory.searchRequestBuilder()
					.registrationStatus("REGISTERED")
					.build();

			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);

			// Then
			assertThat(response.getItems()).hasSize(1);
			assertThat(response.getItems().get(0).getPlaceName()).isEqualTo("등록 업체");
		}

		@Test
		@DisplayName("미등록 업체만 필터링하여 조회한다")
		void filterByUnregisteredStatus() {
			// Given
			PlaceInfo registered = PlaceTestFactory.builder().placeName("등록 업체").registered().build();
			PlaceInfo unregistered = PlaceTestFactory.builder().placeName("미등록 업체").unregistered().build();

			placeInfoRepository.saveAll(List.of(registered, unregistered));
			entityManager.flush();
			entityManager.clear();

			PlaceSearchRequest request = PlaceRequestFactory.searchRequestBuilder()
					.registrationStatus("UNREGISTERED")
					.build();

			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);

			// Then
			assertThat(response.getItems()).hasSize(1);
			assertThat(response.getItems().get(0).getPlaceName()).isEqualTo("미등록 업체");
		}
	}

	@Nested
	@DisplayName("엣지 케이스 테스트")
	class EdgeCaseTests {
		
		@Test
		@DisplayName("검색 결과가 없을 때 빈 응답을 반환한다")
		void returnsEmptyResponseWhenNoResults() {
			// Given
			PlaceSearchRequest request = PlaceRequestFactory.createKeywordSearchRequest("존재하지않는키워드");
			
			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			
			// Then
			assertThat(response.getItems()).isEmpty();
			assertThat(response.getCount()).isZero();
			assertThat(response.getHasNext()).isFalse();
			assertThat(response.getNextCursor()).isNull();
		}
		
		@Test
		@DisplayName("NULL 값이 있는 엔티티도 정상적으로 처리한다")
		void handlesNullValuesGracefully() {
			// Given
			PlaceInfo place = PlaceTestFactory.builder()
					.placeName("최소 정보만 있는 장소")
					.description(null) // NULL
					.rating(null) // NULL
					.reviewCount(0)
					.build();
			
			placeInfoRepository.save(place);
			entityManager.flush();
			entityManager.clear();
			
			PlaceSearchRequest request = PlaceRequestFactory.createBasicSearchRequest();
			
			// When
			PlaceSearchResponse response = searchRepository.searchWithCursor(request);
			
			// Then
			assertThat(response.getItems()).hasSize(1);
			PlaceSearchResponse.PlaceSearchItem item = response.getItems().get(0);
			assertThat(item.getDescription()).isNull();
			assertThat(item.getRatingAverage()).isNull();
		}
	}
}
