package com.teambind.placeinfoserver.place.service.usecase.query;

import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.Keyword;
import com.teambind.placeinfoserver.place.domain.enums.KeywordType;
import com.teambind.placeinfoserver.place.dto.response.KeywordResponse;
import com.teambind.placeinfoserver.place.repository.KeywordRepository;
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

/**
 * GetAllKeywordsUseCase 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("GetAllKeywordsUseCase 통합 테스트")
class GetAllKeywordsUseCaseTest extends BaseIntegrationTest {
	
	@Autowired
	private GetAllKeywordsUseCase getAllKeywordsUseCase;
	
	@Autowired
	private KeywordRepository keywordRepository;
	
	@Autowired
	private EntityManager entityManager;
	
	@BeforeEach
	void setUp() {
		keywordRepository.deleteAll();
		entityManager.flush();
		entityManager.clear();
	}
	
	/**
	 * 테스트용 Keyword 생성 헬퍼 메서드
	 */
	private Keyword createKeyword(String name, KeywordType type, Integer displayOrder, Boolean isActive) {
		return Keyword.builder()
				.name(name)
				.type(type)
				.displayOrder(displayOrder)
				.isActive(isActive)
				.build();
	}
	
	@Nested
	@DisplayName("전체 키워드 조회 테스트")
	class GetAllKeywordsTests {
		
		@Test
		@DisplayName("활성화된 모든 키워드를 조회할 수 있다")
		void canGetAllActiveKeywords() {
			// Given
			Keyword keyword1 = createKeyword("합주실", KeywordType.SPACE_TYPE, 1, true);
			Keyword keyword2 = createKeyword("드럼 세트", KeywordType.INSTRUMENT_EQUIPMENT, 2, true);
			Keyword keyword3 = createKeyword("주차 가능", KeywordType.AMENITY, 3, true);
			
			keywordRepository.save(keyword1);
			keywordRepository.save(keyword2);
			keywordRepository.save(keyword3);
			entityManager.flush();
			entityManager.clear();
			
			// When
			List<KeywordResponse> keywords = getAllKeywordsUseCase.execute();
			
			// Then
			assertThat(keywords).hasSize(3);
			assertThat(keywords).extracting("name")
					.containsExactly("합주실", "드럼 세트", "주차 가능");
		}
		
		@Test
		@DisplayName("비활성화된 키워드는 조회되지 않는다")
		void excludesInactiveKeywords() {
			// Given
			Keyword activeKeyword = createKeyword("합주실", KeywordType.SPACE_TYPE, 1, true);
			Keyword inactiveKeyword = createKeyword("비활성 키워드", KeywordType.SPACE_TYPE, 2, false);
			
			keywordRepository.save(activeKeyword);
			keywordRepository.save(inactiveKeyword);
			entityManager.flush();
			entityManager.clear();
			
			// When
			List<KeywordResponse> keywords = getAllKeywordsUseCase.execute();
			
			// Then
			assertThat(keywords).hasSize(1);
			assertThat(keywords.get(0).getName()).isEqualTo("합주실");
		}
		
		@Test
		@DisplayName("키워드가 displayOrder 순서대로 정렬된다")
		void sortsKeywordsByDisplayOrder() {
			// Given
			Keyword keyword1 = createKeyword("세번째", KeywordType.SPACE_TYPE, 3, true);
			Keyword keyword2 = createKeyword("첫번째", KeywordType.SPACE_TYPE, 1, true);
			Keyword keyword3 = createKeyword("두번째", KeywordType.SPACE_TYPE, 2, true);
			
			keywordRepository.save(keyword1);
			keywordRepository.save(keyword2);
			keywordRepository.save(keyword3);
			entityManager.flush();
			entityManager.clear();
			
			// When
			List<KeywordResponse> keywords = getAllKeywordsUseCase.execute();
			
			// Then
			assertThat(keywords).hasSize(3);
			assertThat(keywords).extracting("name")
					.containsExactly("첫번째", "두번째", "세번째");
		}
		
		@Test
		@DisplayName("키워드가 없을 경우 빈 리스트를 반환한다")
		void returnsEmptyListWhenNoKeywords() {
			// When
			List<KeywordResponse> keywords = getAllKeywordsUseCase.execute();
			
			// Then
			assertThat(keywords).isEmpty();
		}
		
		@Test
		@DisplayName("응답에 모든 필수 필드가 포함된다")
		void responseContainsAllRequiredFields() {
			// Given
			Keyword keyword = createKeyword("합주실", KeywordType.SPACE_TYPE, 1, true);
			keyword.setDescription("밴드 합주를 위한 공간");
			keywordRepository.save(keyword);
			entityManager.flush();
			entityManager.clear();
			
			// When
			List<KeywordResponse> keywords = getAllKeywordsUseCase.execute();
			
			// Then
			assertThat(keywords).hasSize(1);
			KeywordResponse response = keywords.get(0);
			assertThat(response.getId()).isNotNull();
			assertThat(response.getName()).isEqualTo("합주실");
			assertThat(response.getType()).isEqualTo(KeywordType.SPACE_TYPE);
			assertThat(response.getDescription()).isEqualTo("밴드 합주를 위한 공간");
			assertThat(response.getDisplayOrder()).isEqualTo(1);
		}
	}
	
	@Nested
	@DisplayName("타입별 키워드 조회 테스트")
	class GetKeywordsByTypeTests {
		
		@Test
		@DisplayName("특정 타입의 키워드만 조회할 수 있다")
		void canGetKeywordsByType() {
			// Given
			Keyword spaceKeyword = createKeyword("합주실", KeywordType.SPACE_TYPE, 1, true);
			Keyword instrumentKeyword = createKeyword("드럼 세트", KeywordType.INSTRUMENT_EQUIPMENT, 2, true);
			Keyword amenityKeyword = createKeyword("주차 가능", KeywordType.AMENITY, 3, true);
			
			keywordRepository.save(spaceKeyword);
			keywordRepository.save(instrumentKeyword);
			keywordRepository.save(amenityKeyword);
			entityManager.flush();
			entityManager.clear();
			
			// When
			List<KeywordResponse> keywords = getAllKeywordsUseCase.executeByType(KeywordType.SPACE_TYPE);
			
			// Then
			assertThat(keywords).hasSize(1);
			assertThat(keywords.get(0).getName()).isEqualTo("합주실");
			assertThat(keywords.get(0).getType()).isEqualTo(KeywordType.SPACE_TYPE);
		}
		
		@Test
		@DisplayName("특정 타입의 비활성화된 키워드는 조회되지 않는다")
		void excludesInactiveKeywordsForSpecificType() {
			// Given
			Keyword activeKeyword = createKeyword("합주실", KeywordType.SPACE_TYPE, 1, true);
			Keyword inactiveKeyword = createKeyword("비활성 공간", KeywordType.SPACE_TYPE, 2, false);
			
			keywordRepository.save(activeKeyword);
			keywordRepository.save(inactiveKeyword);
			entityManager.flush();
			entityManager.clear();
			
			// When
			List<KeywordResponse> keywords = getAllKeywordsUseCase.executeByType(KeywordType.SPACE_TYPE);
			
			// Then
			assertThat(keywords).hasSize(1);
			assertThat(keywords.get(0).getName()).isEqualTo("합주실");
		}
		
		@Test
		@DisplayName("타입별 조회시에도 displayOrder 순서대로 정렬된다")
		void sortsKeywordsByDisplayOrderForSpecificType() {
			// Given
			Keyword keyword1 = createKeyword("세번째", KeywordType.SPACE_TYPE, 3, true);
			Keyword keyword2 = createKeyword("첫번째", KeywordType.SPACE_TYPE, 1, true);
			Keyword keyword3 = createKeyword("두번째", KeywordType.SPACE_TYPE, 2, true);
			Keyword otherType = createKeyword("다른 타입", KeywordType.AMENITY, 1, true);
			
			keywordRepository.save(keyword1);
			keywordRepository.save(keyword2);
			keywordRepository.save(keyword3);
			keywordRepository.save(otherType);
			entityManager.flush();
			entityManager.clear();
			
			// When
			List<KeywordResponse> keywords = getAllKeywordsUseCase.executeByType(KeywordType.SPACE_TYPE);
			
			// Then
			assertThat(keywords).hasSize(3);
			assertThat(keywords).extracting("name")
					.containsExactly("첫번째", "두번째", "세번째");
		}
		
		@Test
		@DisplayName("해당 타입의 키워드가 없을 경우 빈 리스트를 반환한다")
		void returnsEmptyListWhenNoKeywordsOfType() {
			// Given
			Keyword keyword = createKeyword("합주실", KeywordType.SPACE_TYPE, 1, true);
			keywordRepository.save(keyword);
			entityManager.flush();
			entityManager.clear();
			
			// When
			List<KeywordResponse> keywords = getAllKeywordsUseCase.executeByType(KeywordType.INSTRUMENT_EQUIPMENT);
			
			// Then
			assertThat(keywords).isEmpty();
		}
	}
}
