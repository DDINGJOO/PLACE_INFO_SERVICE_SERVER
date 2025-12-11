package com.teambind.placeinfoserver.place.service.usecase.command;

import com.teambind.placeinfoserver.place.common.exception.application.InvalidRequestException;
import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.enums.ApprovalStatus;
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
 * ApprovePlaceUseCase 및 RejectPlaceUseCase 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ApprovePlaceUseCase/RejectPlaceUseCase 통합 테스트")
class ApprovePlaceUseCaseTest extends BaseIntegrationTest {
	
	@Autowired
	private ApprovePlaceUseCase approvePlaceUseCase;
	
	@Autowired
	private RejectPlaceUseCase rejectPlaceUseCase;
	
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
	@DisplayName("업체 승인 테스트")
	class ApprovalTests {
		
		@Test
		@DisplayName("대기 중인 업체를 승인할 수 있다")
		void canApprovePendingPlace() {
			// Given
			PlaceInfo pendingPlace = PlaceTestFactory.createPendingPlaceInfo();
			placeInfoRepository.save(pendingPlace);
			entityManager.flush();
			entityManager.clear();
			
			// When
			approvePlaceUseCase.execute(String.valueOf(pendingPlace.getId()));
			entityManager.flush();
			entityManager.clear();
			
			// Then
			PlaceInfo approvedPlace = placeInfoRepository.findById(pendingPlace.getId()).orElseThrow();
			assertThat(approvedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
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
			approvePlaceUseCase.execute(String.valueOf(approvedPlace.getId()));
			entityManager.flush();
			entityManager.clear();
			
			// Then
			PlaceInfo stillApprovedPlace = placeInfoRepository.findById(approvedPlace.getId()).orElseThrow();
			assertThat(stillApprovedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		}
		
		@Test
		@DisplayName("거부된 업체를 승인할 수 있다")
		void canApproveRejectedPlace() {
			// Given
			PlaceInfo rejectedPlace = PlaceTestFactory.createPendingPlaceInfo();
			rejectedPlace.reject();
			placeInfoRepository.save(rejectedPlace);
			entityManager.flush();
			entityManager.clear();
			
			// When
			approvePlaceUseCase.execute(String.valueOf(rejectedPlace.getId()));
			entityManager.flush();
			entityManager.clear();
			
			// Then
			PlaceInfo approvedPlace = placeInfoRepository.findById(rejectedPlace.getId()).orElseThrow();
			assertThat(approvedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		}
		
		@Test
		@DisplayName("존재하지 않는 업체 승인 시 예외가 발생한다")
		void throwsExceptionWhenApprovingNonExistentPlace() {
			// When & Then
			assertThatThrownBy(() -> approvePlaceUseCase.execute("999999"))
					.isInstanceOf(PlaceNotFoundException.class);
		}
		
		@Test
		@DisplayName("잘못된 ID 형식으로 승인 시도 시 예외가 발생한다")
		void throwsExceptionForInvalidIdFormat() {
			// When & Then
			assertThatThrownBy(() -> approvePlaceUseCase.execute("invalid_id"))
					.isInstanceOf(InvalidRequestException.class);
		}
	}
	
	@Nested
	@DisplayName("업체 거부 테스트")
	class RejectionTests {
		
		@Test
		@DisplayName("대기 중인 업체를 거부할 수 있다")
		void canRejectPendingPlace() {
			// Given
			PlaceInfo pendingPlace = PlaceTestFactory.createPendingPlaceInfo();
			placeInfoRepository.save(pendingPlace);
			entityManager.flush();
			entityManager.clear();
			
			// When
			rejectPlaceUseCase.execute(String.valueOf(pendingPlace.getId()));
			entityManager.flush();
			entityManager.clear();
			
			// Then
			PlaceInfo rejectedPlace = placeInfoRepository.findById(pendingPlace.getId()).orElseThrow();
			assertThat(rejectedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
		}
		
		@Test
		@DisplayName("이미 거부된 업체를 다시 거부해도 상태가 유지된다")
		void canRejectAlreadyRejectedPlace() {
			// Given
			PlaceInfo rejectedPlace = PlaceTestFactory.createPendingPlaceInfo();
			rejectedPlace.reject();
			placeInfoRepository.save(rejectedPlace);
			entityManager.flush();
			entityManager.clear();
			
			// When
			rejectPlaceUseCase.execute(String.valueOf(rejectedPlace.getId()));
			entityManager.flush();
			entityManager.clear();
			
			// Then
			PlaceInfo stillRejectedPlace = placeInfoRepository.findById(rejectedPlace.getId()).orElseThrow();
			assertThat(stillRejectedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
		}
		
		@Test
		@DisplayName("승인된 업체를 거부할 수 있다")
		void canRejectApprovedPlace() {
			// Given
			PlaceInfo approvedPlace = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(approvedPlace);
			entityManager.flush();
			entityManager.clear();
			
			// When
			rejectPlaceUseCase.execute(String.valueOf(approvedPlace.getId()));
			entityManager.flush();
			entityManager.clear();
			
			// Then
			PlaceInfo rejectedPlace = placeInfoRepository.findById(approvedPlace.getId()).orElseThrow();
			assertThat(rejectedPlace.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
		}
		
		@Test
		@DisplayName("존재하지 않는 업체 거부 시 예외가 발생한다")
		void throwsExceptionWhenRejectingNonExistentPlace() {
			// When & Then
			assertThatThrownBy(() -> rejectPlaceUseCase.execute("999999"))
					.isInstanceOf(PlaceNotFoundException.class);
		}
		
		@Test
		@DisplayName("잘못된 ID 형식으로 거부 시도 시 예외가 발생한다")
		void throwsExceptionForInvalidIdFormat() {
			// When & Then
			assertThatThrownBy(() -> rejectPlaceUseCase.execute("invalid_id"))
					.isInstanceOf(InvalidRequestException.class);
		}
	}
	
	@Nested
	@DisplayName("승인/거부 연속 테스트")
	class ToggleApprovalTests {
		
		@Test
		@DisplayName("업체를 여러 번 승인/거부할 수 있다")
		void canToggleApprovalMultipleTimes() {
			// Given
			PlaceInfo place = PlaceTestFactory.createPendingPlaceInfo();
			placeInfoRepository.save(place);
			entityManager.flush();
			entityManager.clear();
			
			// When & Then
			approvePlaceUseCase.execute(String.valueOf(place.getId()));
			entityManager.flush();
			PlaceInfo approved = placeInfoRepository.findById(place.getId()).orElseThrow();
			assertThat(approved.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
			
			rejectPlaceUseCase.execute(String.valueOf(place.getId()));
			entityManager.flush();
			PlaceInfo rejected = placeInfoRepository.findById(place.getId()).orElseThrow();
			assertThat(rejected.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
			
			approvePlaceUseCase.execute(String.valueOf(place.getId()));
			entityManager.flush();
			PlaceInfo approvedAgain = placeInfoRepository.findById(place.getId()).orElseThrow();
			assertThat(approvedAgain.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		}
	}
}
