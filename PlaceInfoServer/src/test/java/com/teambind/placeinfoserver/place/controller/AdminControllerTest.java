package com.teambind.placeinfoserver.place.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.enums.ApprovalStatus;
import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminController 통합 테스트
 * 관리자 기능 REST API 엔드포인트 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminControllerTest extends BaseIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PlaceInfoRepository placeInfoRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private PlaceInfo pendingPlace;
	private PlaceInfo approvedPlace;
	private PlaceInfo rejectedPlace;

	@BeforeEach
	void setUp() {
		PlaceTestFactory.resetSequence();

		// 테스트 데이터 준비
		pendingPlace = PlaceTestFactory.createPendingPlaceInfo();
		approvedPlace = PlaceTestFactory.createPlaceInfo();
		rejectedPlace = PlaceTestFactory.createRejectedPlaceInfo();

		pendingPlace = placeInfoRepository.save(pendingPlace);
		approvedPlace = placeInfoRepository.save(approvedPlace);
		rejectedPlace = placeInfoRepository.save(rejectedPlace);
	}

	@Nested
	@DisplayName("업체 승인 API 테스트")
	class ApproveTest {

		@Test
		@Order(1)
		@DisplayName("업체 승인 - 성공")
		void approve_Success() throws Exception {
			mockMvc.perform(patch("/api/v1/admin/places/{placeId}", pendingPlace.getId())
							.param("type", "approve")
							.param("contents", "true")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isNoContent());

			// 검증
			PlaceInfo updatedPlace = placeInfoRepository.findById(pendingPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		}

		@Test
		@Order(2)
		@DisplayName("이미 승인된 업체 재승인 - 성공 (멱등성)")
		void approve_AlreadyApproved_Success() throws Exception {
			mockMvc.perform(patch("/api/v1/admin/places/{placeId}", approvedPlace.getId())
							.param("type", "approve")
							.param("contents", "true")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isNoContent());

			// 검증
			PlaceInfo updatedPlace = placeInfoRepository.findById(approvedPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		}

		@Test
		@Order(3)
		@DisplayName("거부된 업체 승인 - 성공")
		void approve_RejectedPlace_Success() throws Exception {
			mockMvc.perform(patch("/api/v1/admin/places/{placeId}", rejectedPlace.getId())
							.param("type", "approve")
							.param("contents", "true")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isNoContent());

			// 검증
			PlaceInfo updatedPlace = placeInfoRepository.findById(rejectedPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		}
	}

	@Nested
	@DisplayName("업체 거부 API 테스트")
	class RejectTest {

		@Test
		@Order(4)
		@DisplayName("업체 거부 - 성공")
		void reject_Success() throws Exception {
			mockMvc.perform(patch("/api/v1/admin/places/{placeId}", pendingPlace.getId())
							.param("type", "approve")
							.param("contents", "false")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isNoContent());

			// 검증
			PlaceInfo updatedPlace = placeInfoRepository.findById(pendingPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
		}

		@Test
		@Order(5)
		@DisplayName("승인된 업체 거부 - 성공")
		void reject_ApprovedPlace_Success() throws Exception {
			mockMvc.perform(patch("/api/v1/admin/places/{placeId}", approvedPlace.getId())
							.param("type", "approve")
							.param("contents", "false")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isNoContent());

			// 검증
			PlaceInfo updatedPlace = placeInfoRepository.findById(approvedPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
		}

		@Test
		@Order(6)
		@DisplayName("이미 거부된 업체 재거부 - 성공 (멱등성)")
		void reject_AlreadyRejected_Success() throws Exception {
			mockMvc.perform(patch("/api/v1/admin/places/{placeId}", rejectedPlace.getId())
							.param("type", "approve")
							.param("contents", "false")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isNoContent());

			// 검증
			PlaceInfo updatedPlace = placeInfoRepository.findById(rejectedPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
		}
	}

	@Nested
	@DisplayName("업체 삭제 API 테스트")
	class DeleteTest {

		@Test
		@Order(7)
		@DisplayName("관리자 권한으로 업체 삭제 - 성공")
		void delete_Admin_Success() throws Exception {
			mockMvc.perform(delete("/api/v1/admin/places/{placeId}", approvedPlace.getId())
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isNoContent());

			// 검증 - 소프트 삭제
			PlaceInfo deletedPlace = placeInfoRepository.findById(approvedPlace.getId()).orElseThrow();
			assertThat(deletedPlace.isDeleted()).isTrue();
		}

		@Test
		@Order(8)
		@DisplayName("승인 대기 중인 업체 삭제 - 성공")
		void delete_PendingPlace_Success() throws Exception {
			mockMvc.perform(delete("/api/v1/admin/places/{placeId}", pendingPlace.getId())
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isNoContent());

			// 검증
			PlaceInfo deletedPlace = placeInfoRepository.findById(pendingPlace.getId()).orElseThrow();
			assertThat(deletedPlace.isDeleted()).isTrue();
		}

		@Test
		@Order(9)
		@DisplayName("거부된 업체 삭제 - 성공")
		void delete_RejectedPlace_Success() throws Exception {
			mockMvc.perform(delete("/api/v1/admin/places/{placeId}", rejectedPlace.getId())
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isNoContent());

			// 검증
			PlaceInfo deletedPlace = placeInfoRepository.findById(rejectedPlace.getId()).orElseThrow();
			assertThat(deletedPlace.isDeleted()).isTrue();
		}

		@Test
		@Order(10)
		@DisplayName("존재하지 않는 업체 삭제 - 실패")
		void delete_PlaceNotFound_Fail() throws Exception {
			mockMvc.perform(delete("/api/v1/admin/places/{placeId}", "invalid_place_id")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().is4xxClientError());
		}
	}

	@Nested
	@DisplayName("잘못된 요청 테스트")
	class InvalidRequestTest {

		@Test
		@Order(11)
		@DisplayName("잘못된 타입 파라미터 - 실패")
		void invalidType_Fail() throws Exception {
			mockMvc.perform(patch("/api/v1/admin/places/{placeId}", pendingPlace.getId())
							.param("type", "invalid")
							.param("contents", "true")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}

		@Test
		@Order(12)
		@DisplayName("존재하지 않는 업체 승인 - 실패")
		void approve_PlaceNotFound_Fail() throws Exception {
			mockMvc.perform(patch("/api/v1/admin/places/{placeId}", "invalid_place_id")
							.param("type", "approve")
							.param("contents", "true")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().is4xxClientError());
		}
	}

	@Nested
	@DisplayName("승인 워크플로우 통합 테스트")
	class ApprovalWorkflowTest {

		@Test
		@Order(13)
		@DisplayName("승인 -> 거부 -> 재승인 워크플로우")
		void approvalWorkflow_Success() throws Exception {
			// 1. 승인
			mockMvc.perform(patch("/api/v1/admin/places/{placeId}", pendingPlace.getId())
							.param("type", "approve")
							.param("contents", "true"))
					.andExpect(status().isNoContent());

			PlaceInfo place1 = placeInfoRepository.findById(pendingPlace.getId()).orElseThrow();
			assertThat(place1.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);

			// 2. 거부
			mockMvc.perform(patch("/api/v1/admin/places/{placeId}", pendingPlace.getId())
							.param("type", "approve")
							.param("contents", "false"))
					.andExpect(status().isNoContent());

			PlaceInfo place2 = placeInfoRepository.findById(pendingPlace.getId()).orElseThrow();
			assertThat(place2.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);

			// 3. 재승인
			mockMvc.perform(patch("/api/v1/admin/places/{placeId}", pendingPlace.getId())
							.param("type", "approve")
							.param("contents", "true"))
					.andExpect(status().isNoContent());

			PlaceInfo place3 = placeInfoRepository.findById(pendingPlace.getId()).orElseThrow();
			assertThat(place3.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		}

		@Test
		@Order(14)
		@DisplayName("승인 후 삭제")
		void approveAndDelete_Success() throws Exception {
			// 1. 승인
			mockMvc.perform(patch("/api/v1/admin/places/{placeId}", pendingPlace.getId())
							.param("type", "approve")
							.param("contents", "true"))
					.andExpect(status().isNoContent());

			// 2. 삭제
			mockMvc.perform(delete("/api/v1/admin/places/{placeId}", pendingPlace.getId()))
					.andExpect(status().isNoContent());

			// 검증
			PlaceInfo deletedPlace = placeInfoRepository.findById(pendingPlace.getId()).orElseThrow();
			assertThat(deletedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
			assertThat(deletedPlace.isDeleted()).isTrue();
		}

		@Test
		@Order(15)
		@DisplayName("거부 후 삭제")
		void rejectAndDelete_Success() throws Exception {
			// 1. 거부
			mockMvc.perform(patch("/api/v1/admin/places/{placeId}", pendingPlace.getId())
							.param("type", "approve")
							.param("contents", "false"))
					.andExpect(status().isNoContent());

			// 2. 삭제
			mockMvc.perform(delete("/api/v1/admin/places/{placeId}", pendingPlace.getId()))
					.andExpect(status().isNoContent());

			// 검증
			PlaceInfo deletedPlace = placeInfoRepository.findById(pendingPlace.getId()).orElseThrow();
			assertThat(deletedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
			assertThat(deletedPlace.isDeleted()).isTrue();
		}
	}
}
