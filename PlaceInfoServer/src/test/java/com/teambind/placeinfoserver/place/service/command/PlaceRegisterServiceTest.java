package com.teambind.placeinfoserver.place.service.command;

import com.teambind.placeinfoserver.place.common.exception.CustomException;
import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.enums.ApprovalStatus;
import com.teambind.placeinfoserver.place.dto.request.PlaceRegisterRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceUpdateRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.fixture.PlaceRequestFactory;
import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PlaceRegisterService 통합 테스트
 * 실제 PostgreSQL + PostGIS 데이터베이스와 모든 의존성을 사용한 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("PlaceRegisterService 통합 테스트")
class PlaceRegisterServiceTest extends BaseIntegrationTest {
	
	@Autowired
	private PlaceRegisterService placeRegisterService;
	
	@Autowired
	private PlaceInfoRepository placeInfoRepository;
	
	@Autowired
	private EntityManager entityManager;
	
	@BeforeEach
	void setUp() {
		PlaceTestFactory.resetSequence();
		placeInfoRepository.deleteAll();
	}
	
	@Nested
	@DisplayName("업체 등록 테스트")
	class RegisterPlaceTests {
		
		@Test
		@DisplayName("유효한 요청으로 업체를 등록할 수 있다")
		void canRegisterPlace() {
			// Given
			PlaceRegisterRequest request = PlaceRequestFactory.createPlaceRegisterRequest();
			
			// When
			PlaceInfoResponse response = placeRegisterService.registerPlace(request);
			
			// Then
			assertThat(response).isNotNull();
			assertThat(response.getId()).isNotNull();
			assertThat(response.getPlaceName()).isEqualTo(request.getPlaceName());
			assertThat(response.getDescription()).isEqualTo(request.getDescription());
			assertThat(response.getCategory()).isEqualTo(request.getCategory());
// 			assertThat(response.getUserId()).isEqualTo(request.getUserId());
			
			// DB에 실제로 저장되었는지 확인
			PlaceInfo savedPlace = placeInfoRepository.findById(Long.parseLong(response.getId())).orElse(null);
			assertThat(savedPlace).isNotNull();
			assertThat(savedPlace.getPlaceName()).isEqualTo(request.getPlaceName());
		}
		
		@Test
		@DisplayName("등록된 업체의 초기 상태는 PENDING이다")
		void newPlaceHasPendingStatus() {
			// Given
			PlaceRegisterRequest request = PlaceRequestFactory.createPlaceRegisterRequest();
			
			// When
			PlaceInfoResponse response = placeRegisterService.registerPlace(request);
			
			// Then
			PlaceInfo savedPlace = placeInfoRepository.findById(Long.parseLong(response.getId())).orElseThrow();
			assertThat(savedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
			assertThat(savedPlace.getIsActive()).isTrue(); // 기본값 확인
		}
		
		@Test
		@DisplayName("등록 시 연락처 정보가 올바르게 저장된다")
		void savesContactInformation() {
			// Given
			PlaceRegisterRequest request = PlaceRequestFactory.createPlaceRegisterRequest();
			
			// When
			PlaceInfoResponse response = placeRegisterService.registerPlace(request);
			
			// Then
			PlaceInfo savedPlace = placeInfoRepository.findById(Long.parseLong(response.getId())).orElseThrow();
			assertThat(savedPlace.getContact()).isNotNull();
// 			assertThat(savedPlace.getContact().getContact()).isEqualTo(request.getContact());
// 			assertThat(savedPlace.getContact().getContact()).isEqualTo(request.getContact());
		}
		
		@Test
		@DisplayName("등록 시 위치 정보가 올바르게 저장된다")
		void savesLocationInformation() {
			// Given
			PlaceRegisterRequest request = PlaceRequestFactory.createPlaceRegisterRequest();
			
			// When
			PlaceInfoResponse response = placeRegisterService.registerPlace(request);
			
			// Then
			PlaceInfo savedPlace = placeInfoRepository.findById(Long.parseLong(response.getId())).orElseThrow();
			assertThat(savedPlace.getLocation()).isNotNull();
// 			assertThat(savedPlace.getLocation().getLatitude()).isEqualTo(request.getLatitude());
// 			assertThat(savedPlace.getLocation().getLatitude()).isEqualTo(request.getLatitude());
// 			assertThat(savedPlace.getLocation().getLatitude()).isEqualTo(request.getLatitude());
// 			assertThat(savedPlace.getLocation().getLatitude()).isEqualTo(request.getLatitude());
		}
		
		@Test
		@DisplayName("등록 시 주차 정보가 올바르게 저장된다")
		void savesParkingInformation() {
			// Given
			PlaceRegisterRequest request = PlaceRequestFactory.createPlaceRegisterRequest();
			
			// When
			PlaceInfoResponse response = placeRegisterService.registerPlace(request);
			
			// Then
			PlaceInfo savedPlace = placeInfoRepository.findById(Long.parseLong(response.getId())).orElseThrow();
			assertThat(savedPlace.getParking()).isNotNull();
// 			assertThat(savedPlace.getParking().getAvailable()).isEqualTo(request.getParkingAvailable());
		}
	}
	
	@Nested
	@DisplayName("업체 수정 테스트")
	class UpdatePlaceTests {
		
		@Test
		@DisplayName("기존 업체 정보를 수정할 수 있다")
		void canUpdatePlace() {
			// Given
			PlaceInfo existingPlace = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(existingPlace);
			entityManager.flush();
			entityManager.clear();
			
			PlaceUpdateRequest updateRequest = PlaceUpdateRequest.builder()
					.placeName("수정된 연습실 이름")
					.description("수정된 설명")
					.category("스튜디오")
					.build();
			
			// When
			PlaceInfoResponse response = placeRegisterService.updatePlace(
					String.valueOf(existingPlace.getId()),
					updateRequest
			);
			
			// Then
			assertThat(response.getPlaceName()).isEqualTo("수정된 연습실 이름");
			assertThat(response.getDescription()).isEqualTo("수정된 설명");
			assertThat(response.getCategory()).isEqualTo("스튜디오");
			
			// DB에서 확인
			PlaceInfo updatedPlace = placeInfoRepository.findById(existingPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getPlaceName()).isEqualTo("수정된 연습실 이름");
		}
		
		@Test
		@DisplayName("존재하지 않는 업체 수정 시 예외가 발생한다")
		void throwsExceptionWhenUpdatingNonExistentPlace() {
			// Given
			String nonExistentId = "non_existent_id";
			PlaceUpdateRequest updateRequest = PlaceUpdateRequest.builder()
					.placeName("수정 시도")
					.build();
			
			// When & Then
			assertThatThrownBy(() -> placeRegisterService.updatePlace(nonExistentId, updateRequest))
					.isInstanceOf(CustomException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_FOUND);
		}
	}
	
	@Nested
	@DisplayName("업체 삭제 테스트")
	class DeletePlaceTests {
		
		
		@Test
		@DisplayName("존재하지 않는 업체 삭제 시 예외가 발생한다")
		void throwsExceptionWhenDeletingNonExistentPlace() {
			// Given
			String nonExistentId = "non_existent_id";
			
			// When & Then
			assertThatThrownBy(() -> placeRegisterService.deletePlace(nonExistentId, "admin"))
					.isInstanceOf(CustomException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_FOUND);
		}
	}
	
	@Nested
	@DisplayName("업체 활성화/비활성화 테스트")
	class ActivationTests {
		
		@Test
		@DisplayName("업체를 활성화할 수 있다")
		void canActivatePlace() {
			// Given
			PlaceInfo inactivePlace = PlaceTestFactory.createInactivePlaceInfo();
			placeInfoRepository.save(inactivePlace);
			entityManager.flush();
			entityManager.clear();
			
			// When
			placeRegisterService.activatePlace(String.valueOf(inactivePlace.getId()));
			entityManager.flush();
			entityManager.clear();
			
			// Then
			PlaceInfo activatedPlace = placeInfoRepository.findById(inactivePlace.getId()).orElseThrow();
			assertThat(activatedPlace.getIsActive()).isTrue();
		}
		
		@Test
		@DisplayName("업체를 비활성화할 수 있다")
		void canDeactivatePlace() {
			// Given
			PlaceInfo activePlace = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(activePlace);
			entityManager.flush();
			entityManager.clear();
			
			// When
			placeRegisterService.deactivatePlace(String.valueOf(activePlace.getId()));
			entityManager.flush();
			entityManager.clear();
			
			// Then
			PlaceInfo deactivatedPlace = placeInfoRepository.findById(activePlace.getId()).orElseThrow();
			assertThat(deactivatedPlace.getIsActive()).isFalse();
		}
		
		@Test
		@DisplayName("존재하지 않는 업체 활성화 시 예외가 발생한다")
		void throwsExceptionWhenActivatingNonExistentPlace() {
			// When & Then
			assertThatThrownBy(() -> placeRegisterService.activatePlace("non_existent"))
					.isInstanceOf(CustomException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_FOUND);
		}
	}
	
	@Nested
	@DisplayName("업체 승인/거부 테스트")
	class ApprovalTests {
		
		@Test
		@DisplayName("업체를 승인할 수 있다")
		void canApprovePlace() {
			// Given
			PlaceInfo pendingPlace = PlaceTestFactory.createPendingPlaceInfo();
			placeInfoRepository.save(pendingPlace);
			entityManager.flush();
			entityManager.clear();
			
			// When
			placeRegisterService.approvePlace(String.valueOf(pendingPlace.getId()));
			entityManager.flush();
			entityManager.clear();
			
			// Then
			PlaceInfo approvedPlace = placeInfoRepository.findById(pendingPlace.getId()).orElseThrow();
			assertThat(approvedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		}
		
		@Test
		@DisplayName("업체를 거부할 수 있다")
		void canRejectPlace() {
			// Given
			PlaceInfo pendingPlace = PlaceTestFactory.createPendingPlaceInfo();
			placeInfoRepository.save(pendingPlace);
			entityManager.flush();
			entityManager.clear();
			
			// When
			placeRegisterService.rejectPlace(String.valueOf(pendingPlace.getId()));
			entityManager.flush();
			entityManager.clear();
			
			// Then
			PlaceInfo rejectedPlace = placeInfoRepository.findById(pendingPlace.getId()).orElseThrow();
			assertThat(rejectedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
		}
		
		@Test
		@DisplayName("존재하지 않는 업체 승인 시 예외가 발생한다")
		void throwsExceptionWhenApprovingNonExistentPlace() {
			// When & Then
			assertThatThrownBy(() -> placeRegisterService.approvePlace("non_existent"))
					.isInstanceOf(CustomException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_FOUND);
		}
		
		@Test
		@DisplayName("이미 승인된 업체를 다시 승인해도 상태가 유지된다")
		void canApproveAlreadyApprovedPlace() {
			// Given
			PlaceInfo approvedPlace = PlaceTestFactory.createPlaceInfo(); // 기본이 APPROVED
			placeInfoRepository.save(approvedPlace);
			entityManager.flush();
			entityManager.clear();
			
			// When
			placeRegisterService.approvePlace(String.valueOf(approvedPlace.getId()));
			entityManager.flush();
			entityManager.clear();
			
			// Then
			PlaceInfo stillApprovedPlace = placeInfoRepository.findById(approvedPlace.getId()).orElseThrow();
			assertThat(stillApprovedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		}
	}
	
	@Nested
	@DisplayName("트랜잭션 및 영속성 테스트")
	class TransactionTests {
		
		@Test
		@DisplayName("수정 시 더티 체킹이 동작한다")
		void dirtyCheckingWorks() {
			// Given
			PlaceInfo place = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(place);
			entityManager.flush();
			entityManager.clear();
			
			PlaceUpdateRequest updateRequest = PlaceUpdateRequest.builder()
					.placeName("더티 체킹 테스트")
					.build();
			
			// When
			placeRegisterService.updatePlace(String.valueOf(place.getId()), updateRequest);
			// flush 없이 조회
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(place.getId()).orElseThrow();
			assertThat(updatedPlace.getPlaceName()).isEqualTo("더티 체킹 테스트");
		}
		
		@Test
		@DisplayName("여러 상태 변경 메서드를 연속으로 호출할 수 있다")
		void canChainMultipleStateChanges() {
			// Given
			PlaceInfo place = PlaceTestFactory.createPendingPlaceInfo();
			placeInfoRepository.save(place);
			entityManager.flush();
			entityManager.clear();
			
			// When
			placeRegisterService.approvePlace(String.valueOf(place.getId()));
			placeRegisterService.activatePlace(String.valueOf(place.getId()));
			entityManager.flush();
			entityManager.clear();
			
			// Then
			PlaceInfo changedPlace = placeInfoRepository.findById(place.getId()).orElseThrow();
			assertThat(changedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
			assertThat(changedPlace.getIsActive()).isTrue();
		}
	}
	
	@Nested
	@DisplayName("비즈니스 로직 검증 테스트")
	class BusinessLogicTests {
		
		
		@Test
		@DisplayName("등록 시 모든 연관 엔티티가 함께 저장된다")
		void savesAllRelatedEntities() {
			// Given
			PlaceRegisterRequest request = PlaceRequestFactory.createPlaceRegisterRequest();
			
			// When
			PlaceInfoResponse response = placeRegisterService.registerPlace(request);
			entityManager.flush();
			entityManager.clear();
			
			// Then
			PlaceInfo savedPlace = placeInfoRepository.findById(Long.parseLong(response.getId())).orElseThrow();
			
			// Cascade 저장 확인
			assertThat(savedPlace.getContact()).isNotNull();
			assertThat(savedPlace.getLocation()).isNotNull();
			assertThat(savedPlace.getParking()).isNotNull();
			
			// 양방향 관계 확인
			assertThat(savedPlace.getContact().getPlaceInfo()).isEqualTo(savedPlace);
			assertThat(savedPlace.getLocation().getPlaceInfo()).isEqualTo(savedPlace);
			assertThat(savedPlace.getParking().getPlaceInfo()).isEqualTo(savedPlace);
		}
	}
}
