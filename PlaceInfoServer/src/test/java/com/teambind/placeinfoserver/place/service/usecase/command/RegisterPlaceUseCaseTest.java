package com.teambind.placeinfoserver.place.service.usecase.command;

import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.enums.ApprovalStatus;
import com.teambind.placeinfoserver.place.dto.request.PlaceRegisterRequest;
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

/**
 * RegisterPlaceUseCase 통합 테스트
 * 실제 데이터베이스와 모든 의존성을 사용한 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("RegisterPlaceUseCase 통합 테스트")
class RegisterPlaceUseCaseTest extends BaseIntegrationTest {

	@Autowired
	private RegisterPlaceUseCase registerPlaceUseCase;

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
			PlaceInfoResponse response = registerPlaceUseCase.execute(request);

			// Then
			assertThat(response).isNotNull();
			assertThat(response.getId()).isNotNull();
			assertThat(response.getPlaceName()).isEqualTo(request.getPlaceName());
			assertThat(response.getDescription()).isEqualTo(request.getDescription());
			assertThat(response.getCategory()).isEqualTo(request.getCategory());

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
			PlaceInfoResponse response = registerPlaceUseCase.execute(request);

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
			PlaceInfoResponse response = registerPlaceUseCase.execute(request);

			// Then
			PlaceInfo savedPlace = placeInfoRepository.findById(Long.parseLong(response.getId())).orElseThrow();
			assertThat(savedPlace.getContact()).isNotNull();
		}

		@Test
		@DisplayName("등록 시 위치 정보가 올바르게 저장된다")
		void savesLocationInformation() {
			// Given
			PlaceRegisterRequest request = PlaceRequestFactory.createPlaceRegisterRequest();

			// When
			PlaceInfoResponse response = registerPlaceUseCase.execute(request);

			// Then
			PlaceInfo savedPlace = placeInfoRepository.findById(Long.parseLong(response.getId())).orElseThrow();
			assertThat(savedPlace.getLocation()).isNotNull();
		}

		@Test
		@DisplayName("등록 시 주차 정보가 올바르게 저장된다")
		void savesParkingInformation() {
			// Given
			PlaceRegisterRequest request = PlaceRequestFactory.createPlaceRegisterRequest();

			// When
			PlaceInfoResponse response = registerPlaceUseCase.execute(request);

			// Then
			PlaceInfo savedPlace = placeInfoRepository.findById(Long.parseLong(response.getId())).orElseThrow();
			assertThat(savedPlace.getParking()).isNotNull();
		}

		@Test
		@DisplayName("등록 시 모든 연관 엔티티가 함께 저장된다")
		void savesAllRelatedEntities() {
			// Given
			PlaceRegisterRequest request = PlaceRequestFactory.createPlaceRegisterRequest();

			// When
			PlaceInfoResponse response = registerPlaceUseCase.execute(request);
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

		@Test
		@DisplayName("여러 업체를 연속으로 등록할 수 있다")
		void canRegisterMultiplePlaces() {
			// Given
			PlaceRegisterRequest request1 = PlaceRequestFactory.createPlaceRegisterRequest();
			PlaceRegisterRequest request2 = PlaceRequestFactory.createPlaceRegisterRequest();

			// When
			PlaceInfoResponse response1 = registerPlaceUseCase.execute(request1);
			PlaceInfoResponse response2 = registerPlaceUseCase.execute(request2);

			// Then
			assertThat(response1.getId()).isNotEqualTo(response2.getId());
			assertThat(placeInfoRepository.count()).isEqualTo(2);
		}

		@Test
		@DisplayName("ID는 자동으로 생성된다")
		void idIsAutomaticallyGenerated() {
			// Given
			PlaceRegisterRequest request = PlaceRequestFactory.createPlaceRegisterRequest();

			// When
			PlaceInfoResponse response = registerPlaceUseCase.execute(request);

			// Then
			assertThat(response.getId()).isNotNull();
			assertThat(response.getId()).isNotEmpty();
			assertThat(Long.parseLong(response.getId())).isPositive();
		}
	}
}
