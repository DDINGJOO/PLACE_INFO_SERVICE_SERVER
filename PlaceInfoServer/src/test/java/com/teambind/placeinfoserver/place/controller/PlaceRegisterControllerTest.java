package com.teambind.placeinfoserver.place.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.dto.request.PlaceLocationRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceRegisterRequest;
import com.teambind.placeinfoserver.place.fixture.PlaceRequestFactory;
import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PlaceRegisterController 통합 테스트
 * MockMvc를 이용한 REST API 엔드포인트 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaceRegisterControllerTest extends BaseIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PlaceInfoRepository placeInfoRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private PlaceInfo testPlace;

	@BeforeEach
	void setUp() {
		PlaceTestFactory.resetSequence();

		// 테스트 데이터 준비
		testPlace = PlaceTestFactory.createPlaceInfo();
		testPlace = placeInfoRepository.save(testPlace);
	}

	@Nested
	@DisplayName("업체 등록 API 테스트")
	class RegisterTest {

		@Test
		@Order(1)
		@DisplayName("업체 등록 - 성공")
		void register_Success() throws Exception {
			PlaceRegisterRequest request = PlaceRequestFactory.createPlaceRegisterRequest();

			mockMvc.perform(post("/api/v1/places")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").exists())
					.andExpect(jsonPath("$.placeName").value(request.getPlaceName()))
					.andExpect(jsonPath("$.description").value(request.getDescription()))
					.andExpect(jsonPath("$.category").value(request.getCategory()))
					.andExpect(jsonPath("$.placeType").value(request.getPlaceType()));
		}

		@Test
		@Order(2)
		@DisplayName("업체 등록 - 최소 정보만으로 성공")
		void register_MinimalInfo_Success() throws Exception {
			PlaceRegisterRequest request = PlaceRegisterRequest.builder()
					.placeOwnerId("test_user_123")
					.placeName("미니멀 연습실")
					.description("최소 정보 테스트")
					.category("연습실")
					.placeType("음악")
					.build();

			mockMvc.perform(post("/api/v1/places")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").exists())
					.andExpect(jsonPath("$.placeName").value("미니멀 연습실"));
		}
	}

	@Nested
	@DisplayName("업체 활성화/비활성화 API 테스트")
	class ActivationTest {

		@Test
		@Order(3)
		@DisplayName("업체 활성화 - 성공")
		void activate_Success() throws Exception {
			// 먼저 비활성화
			testPlace.setIsActive(false);
			testPlace = placeInfoRepository.save(testPlace);

			mockMvc.perform(patch("/api/v1/places/{placeId}", testPlace.getId())
							.param("type", "activate")
							.param("contents", "true")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isNoContent());

			// 검증
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getIsActive()).isTrue();
		}

		@Test
		@Order(4)
		@DisplayName("업체 비활성화 - 성공")
		void deactivate_Success() throws Exception {
			mockMvc.perform(patch("/api/v1/places/{placeId}", testPlace.getId())
							.param("type", "activate")
							.param("contents", "false")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isNoContent());

			// 검증
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getIsActive()).isFalse();
		}

		@Test
		@Order(5)
		@DisplayName("잘못된 타입 - 실패")
		void invalidType_Fail() throws Exception {
			mockMvc.perform(patch("/api/v1/places/{placeId}", testPlace.getId())
							.param("type", "invalid")
							.param("contents", "true")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("업체 위치 정보 업데이트 API 테스트")
	class LocationUpdateTest {

		@Test
		@Order(6)
		@DisplayName("위치 정보 업데이트 - 성공")
		void registerLocation_Success() throws Exception {
			PlaceLocationRequest locationRequest = PlaceRequestFactory.createPlaceRegisterRequest().getLocation();

			mockMvc.perform(put("/api/v1/places/{placeId}/locations", testPlace.getId())
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(locationRequest)))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.placeId").value(testPlace.getId()));

			// 검증
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getLocation()).isNotNull();
			assertThat(updatedPlace.getLocation().getLatitude()).isEqualTo(locationRequest.getLatitude());
			assertThat(updatedPlace.getLocation().getLongitude()).isEqualTo(locationRequest.getLongitude());
		}

		@Test
		@Order(7)
		@DisplayName("존재하지 않는 업체 - 실패")
		void registerLocation_PlaceNotFound_Fail() throws Exception {
			PlaceLocationRequest locationRequest = PlaceRequestFactory.createPlaceRegisterRequest().getLocation();

			mockMvc.perform(put("/api/v1/places/{placeId}/locations", "invalid_place_id")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(locationRequest)))
					.andDo(print())
					.andExpect(status().is4xxClientError());
		}
	}

	@Nested
	@DisplayName("업체 삭제 API 테스트")
	class DeleteTest {

		@Test
		@Order(8)
		@DisplayName("업체 삭제 - 성공 (소프트 삭제)")
		void delete_Success() throws Exception {
			mockMvc.perform(delete("/api/v1/places/{placeId}", testPlace.getId())
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isNoContent());

			// 검증 - 소프트 삭제이므로 여전히 존재하지만 deleted 필드가 true
			PlaceInfo deletedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(deletedPlace.isDeleted()).isTrue();
		}

		@Test
		@Order(9)
		@DisplayName("존재하지 않는 업체 삭제 - 실패")
		void delete_PlaceNotFound_Fail() throws Exception {
			mockMvc.perform(delete("/api/v1/places/{placeId}", "invalid_place_id")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().is4xxClientError());
		}
	
	}
}
