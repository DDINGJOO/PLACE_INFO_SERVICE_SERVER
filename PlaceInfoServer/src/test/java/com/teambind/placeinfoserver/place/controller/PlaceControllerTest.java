package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PlaceController 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaceControllerTest extends BaseIntegrationTest {
	
	private static final String HEADER_APP_TYPE = "X-App-Type";
	private static final String HEADER_USER_ID = "X-User-Id";
	private static final String APP_TYPE_PLACE_MANAGER = "PLACE_MANAGER";
	private static final String APP_TYPE_GENERAL = "GENERAL";
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private PlaceInfoRepository placeInfoRepository;
	
	private PlaceInfo testPlace;
	private String testUserId;
	
	@BeforeEach
	void setUp() {
		PlaceTestFactory.resetSequence();
		
		testPlace = PlaceTestFactory.createPlaceInfo();
		testPlace = placeInfoRepository.save(testPlace);
		testUserId = testPlace.getUserId();
	}
	
	@Nested
	@DisplayName("내 공간 목록 조회 API 테스트")
	class GetMyPlacesTest {
		
		@Test
		@Order(1)
		@DisplayName("내 공간 조회 - 성공")
		void getMyPlaces_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/my")
							.header(HEADER_APP_TYPE, APP_TYPE_PLACE_MANAGER)
							.header(HEADER_USER_ID, testUserId))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$[0].id").exists())
					.andExpect(jsonPath("$[0].placeName").exists())
					.andExpect(jsonPath("$[0].userId").value(testUserId));
		}
		
		@Test
		@Order(2)
		@DisplayName("내 공간 조회 - 등록된 공간이 없는 경우 빈 배열 반환")
		void getMyPlaces_EmptyList() throws Exception {
			String nonExistentUserId = "non_existent_user";
			
			mockMvc.perform(get("/api/v1/places/my")
							.header(HEADER_APP_TYPE, APP_TYPE_PLACE_MANAGER)
							.header(HEADER_USER_ID, nonExistentUserId))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$").isEmpty());
		}
		
		@Test
		@Order(3)
		@DisplayName("내 공간 조회 - X-App-Type 헤더 누락 시 400")
		void getMyPlaces_MissingAppType() throws Exception {
			mockMvc.perform(get("/api/v1/places/my")
							.header(HEADER_USER_ID, testUserId))
					.andDo(print())
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.code").value("AUTH_005"));
		}
		
		@Test
		@Order(4)
		@DisplayName("내 공간 조회 - X-User-Id 헤더 누락 시 400")
		void getMyPlaces_MissingUserId() throws Exception {
			mockMvc.perform(get("/api/v1/places/my")
							.header(HEADER_APP_TYPE, APP_TYPE_PLACE_MANAGER))
					.andDo(print())
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.code").value("AUTH_005"));
		}
		
		@Test
		@Order(5)
		@DisplayName("내 공간 조회 - GENERAL 앱 타입으로 요청 시 403")
		void getMyPlaces_ForbiddenForGeneralApp() throws Exception {
			mockMvc.perform(get("/api/v1/places/my")
							.header(HEADER_APP_TYPE, APP_TYPE_GENERAL)
							.header(HEADER_USER_ID, testUserId))
					.andDo(print())
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.code").value("AUTH_004"));
		}
		
		@Test
		@Order(6)
		@DisplayName("내 공간 조회 - 잘못된 앱 타입 시 400")
		void getMyPlaces_InvalidAppType() throws Exception {
			mockMvc.perform(get("/api/v1/places/my")
							.header(HEADER_APP_TYPE, "INVALID_TYPE")
							.header(HEADER_USER_ID, testUserId))
					.andDo(print())
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.code").value("VALIDATION_003"));
		}
		
		@Test
		@Order(7)
		@DisplayName("내 공간 조회 - 여러 공간 등록 시 모두 조회")
		void getMyPlaces_MultiplePlaces() throws Exception {
			// 같은 userId로 추가 공간 등록
			PlaceInfo secondPlace = PlaceTestFactory.builder()
					.userId(testUserId)
					.placeName("두 번째 연습실")
					.build();
			placeInfoRepository.save(secondPlace);
			
			PlaceInfo thirdPlace = PlaceTestFactory.builder()
					.userId(testUserId)
					.placeName("세 번째 연습실")
					.build();
			placeInfoRepository.save(thirdPlace);
			
			mockMvc.perform(get("/api/v1/places/my")
							.header(HEADER_APP_TYPE, APP_TYPE_PLACE_MANAGER)
							.header(HEADER_USER_ID, testUserId))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$.length()").value(3));
		}
	}
	
	@Nested
	@DisplayName("공간 상세 조회 API 테스트")
	class GetPlaceDetailTest {
		
		@Test
		@Order(1)
		@DisplayName("공간 상세 조회 - 성공")
		void getPlaceDetail_Success() throws Exception {
			mockMvc.perform(get("/api/v1/places/{placeId}", testPlace.getId()))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(String.valueOf(testPlace.getId())))
					.andExpect(jsonPath("$.placeName").value(testPlace.getPlaceName()));
		}
		
		@Test
		@Order(2)
		@DisplayName("공간 상세 조회 - 존재하지 않는 ID 시 404")
		void getPlaceDetail_NotFound() throws Exception {
			mockMvc.perform(get("/api/v1/places/{placeId}", "999999"))
					.andDo(print())
					.andExpect(status().isNotFound());
		}
	}
}
