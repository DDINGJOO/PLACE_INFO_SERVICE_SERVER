package com.teambind.placeinfoserver.place.service.usecase.command;

import com.teambind.placeinfoserver.place.common.exception.application.InvalidRequestException;
import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
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
 * DeletePlaceUseCase 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("DeletePlaceUseCase 통합 테스트")
class DeletePlaceUseCaseTest extends BaseIntegrationTest {
	
	@Autowired
	private DeletePlaceUseCase deletePlaceUseCase;
	
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
	@DisplayName("업체 삭제 테스트")
	class DeletePlaceTests {
		
		@Test
		@DisplayName("존재하는 업체를 소프트 삭제할 수 있다")
		void canSoftDeletePlace() {
			// Given
			PlaceInfo existingPlace = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(existingPlace);
			Long placeId = existingPlace.getId();
			String ownerId = existingPlace.getUserId();
			entityManager.flush();
			entityManager.clear();

			// When
			deletePlaceUseCase.execute(String.valueOf(placeId), ownerId);
			entityManager.flush();
			entityManager.clear();

			// Then - @Where 어노테이션으로 인해 soft-deleted 엔티티는 조회되지 않음
			// 삭제된 업체는 findById로 조회할 수 없음을 검증
			assertThat(placeInfoRepository.findById(placeId)).isEmpty();
		}
		
		@Test
		@DisplayName("존재하지 않는 업체 삭제 시 예외가 발생한다")
		void throwsExceptionWhenDeletingNonExistentPlace() {
			// Given
			String nonExistentId = "999999";
			
			// When & Then
			assertThatThrownBy(() -> deletePlaceUseCase.execute(nonExistentId, "OWNER"))
					.isInstanceOf(PlaceNotFoundException.class);
		}
		
		@Test
		@DisplayName("관리자 권한으로 업체를 삭제할 수 있다")
		void adminCanDeletePlace() {
			// Given
			PlaceInfo existingPlace = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(existingPlace);
			Long placeId = existingPlace.getId();
			entityManager.flush();
			entityManager.clear();

			// When
			deletePlaceUseCase.executeAsAdmin(String.valueOf(placeId), "ADMIN");
			entityManager.flush();
			entityManager.clear();

			// Then - 삭제된 업체는 조회할 수 없음
			assertThat(placeInfoRepository.findById(placeId)).isEmpty();
		}
		
		@Test
		@DisplayName("소유자 권한으로 업체를 삭제할 수 있다")
		void ownerCanDeletePlace() {
			// Given
			PlaceInfo existingPlace = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(existingPlace);
			Long placeId = existingPlace.getId();
			String ownerId = existingPlace.getUserId();
			entityManager.flush();
			entityManager.clear();

			// When
			deletePlaceUseCase.execute(String.valueOf(placeId), ownerId);
			entityManager.flush();
			entityManager.clear();

			// Then - 삭제된 업체는 조회할 수 없음
			assertThat(placeInfoRepository.findById(placeId)).isEmpty();
		}
		
		@Test
		@DisplayName("잘못된 ID 형식으로 삭제 시도 시 예외가 발생한다")
		void throwsExceptionForInvalidIdFormat() {
			// Given
			String invalidId = "invalid_id";
			
			// When & Then
			assertThatThrownBy(() -> deletePlaceUseCase.execute(invalidId, "OWNER"))
					.isInstanceOf(InvalidRequestException.class);
		}
		
		@Test
		@DisplayName("이미 삭제된 업체를 다시 삭제하려고 하면 예외가 발생한다")
		void throwsExceptionWhenDeletingAlreadyDeletedPlace() {
			// Given
			PlaceInfo existingPlace = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(existingPlace);
			Long placeId = existingPlace.getId();
			String ownerId = existingPlace.getUserId();
			entityManager.flush();
			entityManager.clear();

			deletePlaceUseCase.execute(String.valueOf(placeId), ownerId);
			entityManager.flush();
			entityManager.clear();

			// When & Then - @Where 어노테이션으로 인해 삭제된 엔티티는 조회되지 않음
			assertThatThrownBy(() -> deletePlaceUseCase.execute(String.valueOf(placeId), ownerId))
					.isInstanceOf(PlaceNotFoundException.class);
		}
	}
}
