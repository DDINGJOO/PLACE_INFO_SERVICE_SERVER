package com.teambind.placeinfoserver.place.service.usecase.command;

import com.teambind.placeinfoserver.place.common.exception.application.InvalidRequestException;
import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.Keyword;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.enums.KeywordType;
import com.teambind.placeinfoserver.place.dto.request.PlaceUpdateRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import com.teambind.placeinfoserver.place.repository.KeywordRepository;
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

import java.util.List;

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
	private KeywordRepository keywordRepository;
	
	@Autowired
	private EntityManager entityManager;
	
	@BeforeEach
	void setUp() {
		PlaceTestFactory.resetSequence();
		placeInfoRepository.deleteAll();
		keywordRepository.deleteAll();
	}
	
	private Keyword createKeyword(String name, KeywordType type) {
		return keywordRepository.save(Keyword.builder()
				.name(name)
				.type(type)
				.description(name + " 설명")
				.displayOrder(1)
				.isActive(true)
				.build());
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
	
	@Nested
	@DisplayName("키워드 수정 테스트")
	class UpdateKeywordTests {
		
		@Test
		@DisplayName("업체에 키워드를 추가할 수 있다")
		void canAddKeywordsToPlace() {
			// Given
			PlaceInfo existingPlace = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(existingPlace);
			
			Keyword keyword1 = createKeyword("연습실", KeywordType.SPACE_TYPE);
			Keyword keyword2 = createKeyword("드럼 세트", KeywordType.INSTRUMENT_EQUIPMENT);
			entityManager.flush();
			entityManager.clear();
			
			PlaceUpdateRequest updateRequest = PlaceUpdateRequest.builder()
					.keywordIds(List.of(keyword1.getId(), keyword2.getId()))
					.build();
			
			// When
			PlaceInfoResponse response = updatePlaceUseCase.execute(
					String.valueOf(existingPlace.getId()),
					updateRequest
			);
			
			// Then
			assertThat(response.getKeywords()).hasSize(2);
			assertThat(response.getKeywords())
					.extracting("name")
					.containsExactlyInAnyOrder("연습실", "드럼 세트");
		}
		
		@Test
		@DisplayName("기존 키워드를 새 키워드로 교체할 수 있다")
		void canReplaceKeywords() {
			// Given
			Keyword oldKeyword = createKeyword("기존 키워드", KeywordType.SPACE_TYPE);
			Keyword newKeyword = createKeyword("새 키워드", KeywordType.AMENITY);
			
			PlaceInfo existingPlace = PlaceTestFactory.createPlaceInfo();
			existingPlace.addKeyword(oldKeyword);
			placeInfoRepository.save(existingPlace);
			entityManager.flush();
			entityManager.clear();
			
			PlaceUpdateRequest updateRequest = PlaceUpdateRequest.builder()
					.keywordIds(List.of(newKeyword.getId()))
					.build();
			
			// When
			PlaceInfoResponse response = updatePlaceUseCase.execute(
					String.valueOf(existingPlace.getId()),
					updateRequest
			);
			
			// Then
			assertThat(response.getKeywords()).hasSize(1);
			assertThat(response.getKeywords().get(0).getName()).isEqualTo("새 키워드");
		}
		
		@Test
		@DisplayName("빈 키워드 목록으로 모든 키워드를 제거할 수 있다")
		void canRemoveAllKeywords() {
			// Given
			Keyword keyword = createKeyword("제거할 키워드", KeywordType.SPACE_TYPE);
			
			PlaceInfo existingPlace = PlaceTestFactory.createPlaceInfo();
			existingPlace.addKeyword(keyword);
			placeInfoRepository.save(existingPlace);
			entityManager.flush();
			entityManager.clear();
			
			PlaceUpdateRequest updateRequest = PlaceUpdateRequest.builder()
					.keywordIds(List.of())
					.build();
			
			// When
			PlaceInfoResponse response = updatePlaceUseCase.execute(
					String.valueOf(existingPlace.getId()),
					updateRequest
			);
			
			// Then
			assertThat(response.getKeywords()).isEmpty();
		}
		
		@Test
		@DisplayName("keywordIds가 null이면 기존 키워드를 유지한다")
		void keepsExistingKeywordsWhenKeywordIdsIsNull() {
			// Given
			Keyword keyword = createKeyword("유지할 키워드", KeywordType.SPACE_TYPE);
			
			PlaceInfo existingPlace = PlaceTestFactory.createPlaceInfo();
			existingPlace.addKeyword(keyword);
			placeInfoRepository.save(existingPlace);
			entityManager.flush();
			entityManager.clear();
			
			PlaceUpdateRequest updateRequest = PlaceUpdateRequest.builder()
					.placeName("이름만 수정")
					.keywordIds(null)
					.build();
			
			// When
			PlaceInfoResponse response = updatePlaceUseCase.execute(
					String.valueOf(existingPlace.getId()),
					updateRequest
			);
			
			// Then
			assertThat(response.getPlaceName()).isEqualTo("이름만 수정");
			assertThat(response.getKeywords()).hasSize(1);
			assertThat(response.getKeywords().get(0).getName()).isEqualTo("유지할 키워드");
		}
		
		@Test
		@DisplayName("11개 이상의 키워드 추가 시 예외가 발생한다")
		void throwsExceptionWhenExceedingMaxKeywords() {
			// Given
			PlaceInfo existingPlace = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(existingPlace);
			
			List<Long> keywordIds = new java.util.ArrayList<>();
			for (int i = 0; i < 11; i++) {
				Keyword keyword = createKeyword("키워드" + i, KeywordType.OTHER_FEATURE);
				keywordIds.add(keyword.getId());
			}
			entityManager.flush();
			entityManager.clear();
			
			PlaceUpdateRequest updateRequest = PlaceUpdateRequest.builder()
					.keywordIds(keywordIds)
					.build();
			
			// When & Then
			assertThatThrownBy(() -> updatePlaceUseCase.execute(
					String.valueOf(existingPlace.getId()),
					updateRequest
			)).isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("10개");
		}
		
		@Test
		@DisplayName("존재하지 않는 키워드 ID로 수정 시 예외가 발생한다")
		void throwsExceptionForInvalidKeywordId() {
			// Given
			PlaceInfo existingPlace = PlaceTestFactory.createPlaceInfo();
			placeInfoRepository.save(existingPlace);
			entityManager.flush();
			entityManager.clear();
			
			PlaceUpdateRequest updateRequest = PlaceUpdateRequest.builder()
					.keywordIds(List.of(999999L))
					.build();
			
			// When & Then
			assertThatThrownBy(() -> updatePlaceUseCase.execute(
					String.valueOf(existingPlace.getId()),
					updateRequest
			)).isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("유효하지 않은");
		}
	}
}
