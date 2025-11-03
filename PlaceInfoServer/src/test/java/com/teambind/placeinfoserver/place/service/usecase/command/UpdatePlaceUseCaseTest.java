package com.teambind.placeinfoserver.place.service.usecase.command;

import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.common.exception.application.InvalidRequestException;
import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.dto.request.PlaceUpdateRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
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
 * UpdatePlaceUseCase 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UpdatePlaceUseCase 통합 테스트")
class UpdatePlaceUseCaseTest extends BaseIntegrationTest {

	@Autowired
	private UpdatePlaceUseCase updatePlaceUseCase;

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
			PlaceInfoResponse response = updatePlaceUseCase.execute(
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
			String nonExistentId = "999999";
			PlaceUpdateRequest updateRequest = PlaceUpdateRequest.builder()
					.placeName("수정 시도")
					.build();

			// When & Then
			assertThatThrownBy(() -> updatePlaceUseCase.execute(nonExistentId, updateRequest))
					.isInstanceOf(PlaceNotFoundException.class);
		}

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
			updatePlaceUseCase.execute(String.valueOf(place.getId()), updateRequest);
			// flush 없이 조회

			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(place.getId()).orElseThrow();
			assertThat(updatedPlace.getPlaceName()).isEqualTo("더티 체킹 테스트");
		}

		@Test
		@DisplayName("부분 업데이트가 가능하다")
		void canPartiallyUpdate() {
			// Given
			PlaceInfo existingPlace = PlaceTestFactory.createPlaceInfo();
			String originalDescription = existingPlace.getDescription();
			placeInfoRepository.save(existingPlace);
			entityManager.flush();
			entityManager.clear();

			PlaceUpdateRequest updateRequest = PlaceUpdateRequest.builder()
					.placeName("변경된 이름만")
					// description은 변경하지 않음
					.build();

			// When
			PlaceInfoResponse response = updatePlaceUseCase.execute(
					String.valueOf(existingPlace.getId()),
					updateRequest
			);

			// Then
			assertThat(response.getPlaceName()).isEqualTo("변경된 이름만");
			assertThat(response.getDescription()).isEqualTo(originalDescription); // 원래 값 유지
		}

		@Test
		@DisplayName("잘못된 ID 형식으로 수정 시도 시 예외가 발생한다")
		void throwsExceptionForInvalidIdFormat() {
			// Given
			String invalidId = "invalid_id";
			PlaceUpdateRequest updateRequest = PlaceUpdateRequest.builder()
					.placeName("수정 시도")
					.build();

			// When & Then
			assertThatThrownBy(() -> updatePlaceUseCase.execute(invalidId, updateRequest))
					.isInstanceOf(InvalidRequestException.class);
		}

		@Test
		@DisplayName("같은 업체를 여러 번 수정할 수 있다")
		void canUpdateSamePlaceMultipleTimes() {
			// Given
			PlaceInfo existingPlace = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(existingPlace);
			entityManager.flush();
			entityManager.clear();

			// When
			PlaceUpdateRequest update1 = PlaceUpdateRequest.builder()
					.placeName("첫 번째 수정")
					.build();
			updatePlaceUseCase.execute(String.valueOf(existingPlace.getId()), update1);

			PlaceUpdateRequest update2 = PlaceUpdateRequest.builder()
					.placeName("두 번째 수정")
					.build();
			PlaceInfoResponse response = updatePlaceUseCase.execute(String.valueOf(existingPlace.getId()), update2);

			// Then
			assertThat(response.getPlaceName()).isEqualTo("두 번째 수정");
		}
	}
}
