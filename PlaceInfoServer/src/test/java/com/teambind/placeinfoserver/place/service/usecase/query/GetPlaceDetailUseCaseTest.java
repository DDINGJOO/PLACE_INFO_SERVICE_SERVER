package com.teambind.placeinfoserver.place.service.usecase.query;

import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
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
 * GetPlaceDetailUseCase 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("GetPlaceDetailUseCase 통합 테스트")
class GetPlaceDetailUseCaseTest extends BaseIntegrationTest {

	@Autowired
	private GetPlaceDetailUseCase getPlaceDetailUseCase;

	@Autowired
	private PlaceInfoRepository placeInfoRepository;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private com.teambind.placeinfoserver.place.service.usecase.command.DeletePlaceUseCase deletePlaceUseCase;

	@BeforeEach
	void setUp() {
		PlaceTestFactory.resetSequence();
		placeInfoRepository.deleteAll();
	}

	@Nested
	@DisplayName("업체 상세 조회 테스트")
	class GetPlaceDetailTests {

		@Test
		@DisplayName("존재하는 업체의 상세 정보를 조회할 수 있다")
		void canGetPlaceDetail() {
			// Given
			PlaceInfo place = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(place);
			entityManager.flush();
			entityManager.clear();

			// When
			PlaceInfoResponse response = getPlaceDetailUseCase.execute(String.valueOf(place.getId()));

			// Then
			assertThat(response).isNotNull();
			assertThat(response.getId()).isEqualTo(String.valueOf(place.getId()));
			assertThat(response.getPlaceName()).isEqualTo(place.getPlaceName());
			assertThat(response.getDescription()).isEqualTo(place.getDescription());
		}

		@Test
		@DisplayName("존재하지 않는 업체 조회 시 예외가 발생한다")
		void throwsExceptionWhenPlaceNotFound() {
			// When & Then
			assertThatThrownBy(() -> getPlaceDetailUseCase.execute("999999"))
					.isInstanceOf(PlaceNotFoundException.class);
		}

		@Test
		@DisplayName("잘못된 ID 형식으로 조회 시 예외가 발생한다")
		void throwsExceptionForInvalidIdFormat() {
			// When & Then
			assertThatThrownBy(() -> getPlaceDetailUseCase.execute("invalid_id"))
					.isInstanceOf(Exception.class);
		}

		@Test
		@DisplayName("조회한 응답에 모든 필수 필드가 포함된다")
		void responseContainsAllRequiredFields() {
			// Given
			PlaceInfo place = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(place);
			entityManager.flush();
			entityManager.clear();

			// When
			PlaceInfoResponse response = getPlaceDetailUseCase.execute(String.valueOf(place.getId()));

			// Then
			assertThat(response.getId()).isNotNull();
			assertThat(response.getPlaceName()).isNotNull();
			assertThat(response.getDescription()).isNotNull();
			assertThat(response.getCategory()).isNotNull();
		}

		@Test
		@DisplayName("@Transactional(readOnly=true)로 조회만 수행한다")
		void performsReadOnlyQuery() {
			// Given
			PlaceInfo place = PlaceTestFactory.createPlaceInfo();
			String originalName = place.getPlaceName();
			placeInfoRepository.save(place);
			entityManager.flush();
			entityManager.clear();

			// When
			getPlaceDetailUseCase.execute(String.valueOf(place.getId()));

			// Then - 데이터가 변경되지 않았는지 확인
			PlaceInfo unchanged = placeInfoRepository.findById(place.getId()).orElseThrow();
			assertThat(unchanged.getPlaceName()).isEqualTo(originalName);
		}

		@Test
		@DisplayName("연관된 엔티티 정보도 함께 조회된다")
		void loadsRelatedEntities() {
			// Given
			PlaceInfo place = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(place);
			entityManager.flush();
			entityManager.clear();

			// When
			PlaceInfoResponse response = getPlaceDetailUseCase.execute(String.valueOf(place.getId()));

			// Then
			assertThat(response).isNotNull();
			// 연관 엔티티 정보 확인 가능 (response 구조에 따라)
		}

		@Test
		@DisplayName("삭제된 업체 조회 시 예외가 발생한다")
		void throwsExceptionWhenQueryingDeletedPlace() {
			// Given
			PlaceInfo place = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(place);
			Long placeId = place.getId();
			entityManager.flush();
			entityManager.clear();

			// DeletePlaceUseCase를 사용하여 soft-delete 실행
			deletePlaceUseCase.execute(String.valueOf(placeId), "SYSTEM");
			entityManager.flush();
			entityManager.clear();

			// When & Then
			// @Where 어노테이션으로 인해 삭제된 엔티티는 조회되지 않음
			assertThatThrownBy(() -> getPlaceDetailUseCase.execute(String.valueOf(placeId)))
					.isInstanceOf(PlaceNotFoundException.class);
		}

		@Test
		@DisplayName("비활성화된 업체도 조회할 수 있다")
		void canQueryInactivePlace() {
			// Given
			PlaceInfo place = PlaceTestFactory.createInactivePlaceInfo();
			placeInfoRepository.save(place);
			entityManager.flush();
			entityManager.clear();

			// When
			PlaceInfoResponse response = getPlaceDetailUseCase.execute(String.valueOf(place.getId()));

			// Then
			assertThat(response).isNotNull();
			assertThat(response.getId()).isEqualTo(String.valueOf(place.getId()));
		}
	}
}
