package com.teambind.placeinfoserver.place.service.usecase.command;

import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.common.exception.application.InvalidRequestException;
import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
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
 * ActivatePlaceUseCase 및 DeactivatePlaceUseCase 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ActivatePlaceUseCase/DeactivatePlaceUseCase 통합 테스트")
class ActivatePlaceUseCaseTest extends BaseIntegrationTest {

	@Autowired
	private ActivatePlaceUseCase activatePlaceUseCase;

	@Autowired
	private DeactivatePlaceUseCase deactivatePlaceUseCase;

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
	@DisplayName("업체 활성화 테스트")
	class ActivationTests {

		@Test
		@DisplayName("비활성 상태의 업체를 활성화할 수 있다")
		void canActivateInactivePlace() {
			// Given
			PlaceInfo inactivePlace = PlaceTestFactory.createInactivePlaceInfo();
			placeInfoRepository.save(inactivePlace);
			entityManager.flush();
			entityManager.clear();

			// When
			activatePlaceUseCase.execute(String.valueOf(inactivePlace.getId()));
			entityManager.flush();
			entityManager.clear();

			// Then
			PlaceInfo activatedPlace = placeInfoRepository.findById(inactivePlace.getId()).orElseThrow();
			assertThat(activatedPlace.getIsActive()).isTrue();
		}

		@Test
		@DisplayName("이미 활성화된 업체를 다시 활성화해도 정상 동작한다")
		void canActivateAlreadyActivePlace() {
			// Given
			PlaceInfo activePlace = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(activePlace);
			entityManager.flush();
			entityManager.clear();

			// When
			activatePlaceUseCase.execute(String.valueOf(activePlace.getId()));
			entityManager.flush();
			entityManager.clear();

			// Then
			PlaceInfo stillActivePlace = placeInfoRepository.findById(activePlace.getId()).orElseThrow();
			assertThat(stillActivePlace.getIsActive()).isTrue();
		}

		@Test
		@DisplayName("존재하지 않는 업체 활성화 시 예외가 발생한다")
		void throwsExceptionWhenActivatingNonExistentPlace() {
			// When & Then
			assertThatThrownBy(() -> activatePlaceUseCase.execute("999999"))
					.isInstanceOf(PlaceNotFoundException.class);
		}

		@Test
		@DisplayName("잘못된 ID 형식으로 활성화 시도 시 예외가 발생한다")
		void throwsExceptionForInvalidIdFormat() {
			// When & Then
			assertThatThrownBy(() -> activatePlaceUseCase.execute("invalid_id"))
					.isInstanceOf(InvalidRequestException.class);
		}
	}

	@Nested
	@DisplayName("업체 비활성화 테스트")
	class DeactivationTests {

		@Test
		@DisplayName("활성 상태의 업체를 비활성화할 수 있다")
		void canDeactivateActivePlace() {
			// Given
			PlaceInfo activePlace = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(activePlace);
			entityManager.flush();
			entityManager.clear();

			// When
			deactivatePlaceUseCase.execute(String.valueOf(activePlace.getId()));
			entityManager.flush();
			entityManager.clear();

			// Then
			PlaceInfo deactivatedPlace = placeInfoRepository.findById(activePlace.getId()).orElseThrow();
			assertThat(deactivatedPlace.getIsActive()).isFalse();
		}

		@Test
		@DisplayName("이미 비활성화된 업체를 다시 비활성화해도 정상 동작한다")
		void canDeactivateAlreadyInactivePlace() {
			// Given
			PlaceInfo inactivePlace = PlaceTestFactory.createInactivePlaceInfo();
			placeInfoRepository.save(inactivePlace);
			entityManager.flush();
			entityManager.clear();

			// When
			deactivatePlaceUseCase.execute(String.valueOf(inactivePlace.getId()));
			entityManager.flush();
			entityManager.clear();

			// Then
			PlaceInfo stillInactivePlace = placeInfoRepository.findById(inactivePlace.getId()).orElseThrow();
			assertThat(stillInactivePlace.getIsActive()).isFalse();
		}

		@Test
		@DisplayName("존재하지 않는 업체 비활성화 시 예외가 발생한다")
		void throwsExceptionWhenDeactivatingNonExistentPlace() {
			// When & Then
			assertThatThrownBy(() -> deactivatePlaceUseCase.execute("999999"))
					.isInstanceOf(PlaceNotFoundException.class);
		}

		@Test
		@DisplayName("잘못된 ID 형식으로 비활성화 시도 시 예외가 발생한다")
		void throwsExceptionForInvalidIdFormat() {
			// When & Then
			assertThatThrownBy(() -> deactivatePlaceUseCase.execute("invalid_id"))
					.isInstanceOf(InvalidRequestException.class);
		}
	}

	@Nested
	@DisplayName("활성화/비활성화 연속 테스트")
	class ToggleTests {

		@Test
		@DisplayName("업체를 여러 번 활성화/비활성화할 수 있다")
		void canToggleMultipleTimes() {
			// Given
			PlaceInfo place = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(place);
			entityManager.flush();
			entityManager.clear();

			// When & Then
			deactivatePlaceUseCase.execute(String.valueOf(place.getId()));
			entityManager.flush();
			PlaceInfo deactivated = placeInfoRepository.findById(place.getId()).orElseThrow();
			assertThat(deactivated.getIsActive()).isFalse();

			activatePlaceUseCase.execute(String.valueOf(place.getId()));
			entityManager.flush();
			PlaceInfo reactivated = placeInfoRepository.findById(place.getId()).orElseThrow();
			assertThat(reactivated.getIsActive()).isTrue();

			deactivatePlaceUseCase.execute(String.valueOf(place.getId()));
			entityManager.flush();
			PlaceInfo deactivatedAgain = placeInfoRepository.findById(place.getId()).orElseThrow();
			assertThat(deactivatedAgain.getIsActive()).isFalse();
		}
	}
}
