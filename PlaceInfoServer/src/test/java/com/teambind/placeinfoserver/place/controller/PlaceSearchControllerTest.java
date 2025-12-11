package com.teambind.placeinfoserver.place.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.dto.request.LocationSearchRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceBatchDetailRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceSearchRequest;
import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PlaceSearchController 통합 테스트
 * MockMvc를 이용한 REST API 엔드포인트 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaceSearchControllerTest extends BaseIntegrationTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private PlaceInfoRepository placeInfoRepository;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private PlaceInfo testPlace1;
	private PlaceInfo testPlace2;
	
	@BeforeEach
	void setUp() {
		PlaceTestFactory.resetSequence();
		
		// 테스트 데이터 준비
		testPlace1 = PlaceTestFactory.createPlaceInfoWithLocation(
				"테스트 연습실 1", 37.4979, 127.0276
		);
		testPlace2 = PlaceTestFactory.createPlaceInfoWithLocation(
				"테스트 스튜디오 2", 37.5000, 127.0300
		);
		
		placeInfoRepository.saveAll(List.of(testPlace1, testPlace2));
	}
	
	@Nested
	@DisplayName("통합 검색 API 테스트")
	class SearchTest {
		
		@Test
		@Order(1)
		@DisplayName("기본 검색 - 성공")
		void search_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search")
							.param("size", "10")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray())
					.andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(2))))
					.andExpect(jsonPath("$.metadata").exists())
					.andExpect(jsonPath("$.hasNext").exists());
		}
		
		@Test
		@Order(2)
		@DisplayName("키워드 검색 - 성공")
		void searchByKeyword_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search")
							.param("keyword", "연습실")
							.param("size", "10")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray())
					.andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(1))));
		}
		
		@Test
		@Order(3)
		@DisplayName("장소명 검색 - 성공")
		void searchByPlaceName_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search")
							.param("placeName", "스튜디오")
							.param("size", "10")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray());
		}
		
		@Test
		@Order(4)
		@DisplayName("정렬 기준 적용 - 성공")
		void searchWithSorting_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search")
							.param("sortBy", "PLACE_NAME")
							.param("sortDirection", "ASC")
							.param("size", "10")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray());
		}
	}
	
	@Nested
	@DisplayName("위치 기반 검색 API 테스트")
	class LocationSearchTest {
		
		@Test
		@Order(5)
		@DisplayName("위치 기반 검색 - 성공")
		void searchByLocation_Success() throws Exception {
			LocationSearchRequest request = new LocationSearchRequest();
			request.setLatitude(37.4979);
			request.setLongitude(127.0276);
			request.setRadius(5000);
			request.setSize(10);
			
			mockMvc.perform(post("/api/v1/places/search/location")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray())
					.andExpect(jsonPath("$.metadata").exists());
		}
		
		@Test
		@Order(6)
		@DisplayName("위치 기반 검색 with 키워드 - 성공")
		void searchByLocationWithKeyword_Success() throws Exception {
			LocationSearchRequest request = new LocationSearchRequest();
			request.setLatitude(37.4979);
			request.setLongitude(127.0276);
			request.setRadius(10000);
			request.setKeyword("연습실");
			request.setSize(10);
			
			mockMvc.perform(post("/api/v1/places/search/location")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray());
		}
	}
	
	@Nested
	@DisplayName("지역별 검색 API 테스트")
	class RegionSearchTest {
		
		@Test
		@Order(7)
		@DisplayName("지역별 검색 - 시/도만 - 성공")
		void searchByRegion_ProvinceOnly_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search/region")
							.param("province", "서울특별시")
							.param("size", "10")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray());
		}
		
		@Test
		@Order(8)
		@DisplayName("지역별 검색 - 시/도, 시/군/구 - 성공")
		void searchByRegion_ProvinceAndCity_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search/region")
							.param("province", "서울특별시")
							.param("city", "강남구")
							.param("size", "10")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray());
		}
		
		@Test
		@Order(9)
		@DisplayName("지역별 검색 - 전체 주소 - 성공")
		void searchByRegion_FullAddress_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search/region")
							.param("province", "서울특별시")
							.param("city", "강남구")
							.param("district", "역삼동")
							.param("size", "10")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray());
		}
	}
	
	@Nested
	@DisplayName("인기 장소 조회 API 테스트")
	class PopularPlacesTest {
		
		@Test
		@Order(10)
		@DisplayName("인기 장소 조회 - 기본 사이즈")
		void getPopularPlaces_DefaultSize_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search/popular")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray());
		}
		
		@Test
		@Order(11)
		@DisplayName("인기 장소 조회 - 커스텀 사이즈")
		void getPopularPlaces_CustomSize_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search/popular")
							.param("size", "5")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray());
		}
	}
	
	@Nested
	@DisplayName("최신 장소 조회 API 테스트")
	class RecentPlacesTest {
		
		@Test
		@Order(12)
		@DisplayName("최신 장소 조회 - 기본 사이즈")
		void getRecentPlaces_DefaultSize_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search/recent")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray());
		}
		
		@Test
		@Order(13)
		@DisplayName("최신 장소 조회 - 커스텀 사이즈")
		void getRecentPlaces_CustomSize_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search/recent")
							.param("size", "3")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray());
		}
	}
	
	@Nested
	@DisplayName("검색 결과 개수 조회 API 테스트")
	class CountSearchResultsTest {
		
		@Test
		@Order(14)
		@DisplayName("검색 결과 개수 조회 - 성공")
		void countSearchResults_Success() throws Exception {
			PlaceSearchRequest request = PlaceSearchRequest.builder()
					.keyword("연습실")
					.size(10)
					.build();
			
			mockMvc.perform(post("/api/v1/places/search/count")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.count").exists())
					.andExpect(jsonPath("$.count").isNumber());
		}
		
		@Test
		@Order(15)
		@DisplayName("검색 결과 개수 조회 - 조건 없음")
		void countSearchResults_NoCondition_Success() throws Exception {
			PlaceSearchRequest request = PlaceSearchRequest.builder()
					.size(10)
					.build();
			
			mockMvc.perform(post("/api/v1/places/search/count")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.count").exists())
					.andExpect(jsonPath("$.count", greaterThanOrEqualTo(2)));
		}
	}
	
	@Nested
	@DisplayName("페이징 테스트")
	class PagingTest {
		
		@Test
		@Order(16)
		@DisplayName("페이지 크기 적용 - 성공")
		void searchWithPageSize_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search")
							.param("size", "1")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray())
					.andExpect(jsonPath("$.items", hasSize(lessThanOrEqualTo(1))))
					.andExpect(jsonPath("$.hasNext").exists());
		}
	}
	
	@Nested
	@DisplayName("복합 조건 검색 테스트")
	class ComplexSearchTest {
		
		@Test
		@Order(17)
		@DisplayName("복합 조건 검색 - 키워드 + 지역")
		void searchWithMultipleConditions_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search")
							.param("keyword", "연습실")
							.param("province", "서울특별시")
							.param("city", "강남구")
							.param("size", "10")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray());
		}
		
		@Test
		@Order(18)
		@DisplayName("복합 조건 검색 - 카테고리 + 주차 가능")
		void searchWithCategoryAndParking_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/search")
							.param("category", "연습실")
							.param("parkingAvailable", "true")
							.param("size", "10")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.items").isArray());
		}
	}
	
	@Nested
	@DisplayName("배치 상세 조회 API 테스트")
	class BatchDetailSearchTest {
		
		@Test
		@Order(19)
		@DisplayName("배치 상세 조회 - 모든 ID 존재")
		void batchDetailSearch_AllFound_Success() throws Exception {
			// Given
			List<Long> placeIds = List.of(testPlace1.getId(), testPlace2.getId());
			PlaceBatchDetailRequest request = PlaceBatchDetailRequest.builder()
					.placeIds(placeIds)
					.build();
			
			// When & Then
			mockMvc.perform(post("/api/v1/places/search/batch/details")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.results").isArray())
					.andExpect(jsonPath("$.results", hasSize(2)))
					.andExpect(jsonPath("$.failed").doesNotExist()); // @JsonInclude로 빈 배열은 제외
		}
		
		@Test
		@Order(20)
		@DisplayName("배치 상세 조회 - 부분 실패")
		void batchDetailSearch_PartialFailure_Success() throws Exception {
			// Given
			List<Long> placeIds = List.of(testPlace1.getId(), 999999L, testPlace2.getId(), 888888L);
			PlaceBatchDetailRequest request = PlaceBatchDetailRequest.builder()
					.placeIds(placeIds)
					.build();
			
			// When & Then
			mockMvc.perform(post("/api/v1/places/search/batch/details")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.results").isArray())
					.andExpect(jsonPath("$.results", hasSize(2)))
					.andExpect(jsonPath("$.failed").isArray())
					.andExpect(jsonPath("$.failed", hasSize(2)))
					.andExpect(jsonPath("$.failed", containsInAnyOrder(999999, 888888)));
		}
		
		@Test
		@Order(21)
		@DisplayName("배치 상세 조회 - 빈 목록으로 요청시 400 에러")
		void batchDetailSearch_EmptyList_BadRequest() throws Exception {
			// Given
			PlaceBatchDetailRequest request = PlaceBatchDetailRequest.builder()
					.placeIds(List.of())
					.build();
			
			// When & Then
			mockMvc.perform(post("/api/v1/places/search/batch/details")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}
		
		@Test
		@Order(22)
		@DisplayName("배치 상세 조회 - 최대 개수 초과시 400 에러")
		void batchDetailSearch_ExceedMaxSize_BadRequest() throws Exception {
			// Given - 51개의 ID 생성 (최대 50개 제한)
			List<Long> tooManyIds = java.util.stream.LongStream.rangeClosed(1, 51)
					.boxed()
					.collect(java.util.stream.Collectors.toList());
			
			PlaceBatchDetailRequest request = PlaceBatchDetailRequest.builder()
					.placeIds(tooManyIds)
					.build();
			
			// When & Then
			mockMvc.perform(post("/api/v1/places/search/batch/details")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}
		
		@Test
		@Order(23)
		@DisplayName("배치 상세 조회 - 중복 ID 처리")
		void batchDetailSearch_DuplicateIds_Success() throws Exception {
			// Given
			List<Long> duplicateIds = List.of(
					testPlace1.getId(),
					testPlace1.getId(),  // 중복
					testPlace2.getId(),
					testPlace2.getId()   // 중복
			);
			
			PlaceBatchDetailRequest request = PlaceBatchDetailRequest.builder()
					.placeIds(duplicateIds)
					.build();
			
			// When & Then
			mockMvc.perform(post("/api/v1/places/search/batch/details")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.results").isArray())
					.andExpect(jsonPath("$.results", hasSize(2)))  // 중복 제거되어 2개만
					.andExpect(jsonPath("$.failed").doesNotExist());
		}
	}
}
