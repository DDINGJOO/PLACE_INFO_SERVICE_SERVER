package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.Keyword;
import com.teambind.placeinfoserver.place.domain.enums.KeywordType;
import com.teambind.placeinfoserver.place.repository.KeywordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * KeywordController 통합 테스트
 * MockMvc를 이용한 REST API 엔드포인트 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("KeywordController 통합 테스트")
class KeywordControllerTest extends BaseIntegrationTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private KeywordRepository keywordRepository;
	
	@BeforeEach
	void setUp() {
		keywordRepository.deleteAll();
		
		// 테스트 데이터 준비
		keywordRepository.save(createKeyword("합주실", KeywordType.SPACE_TYPE, "밴드 합주 공간", 1, true));
		keywordRepository.save(createKeyword("연습실", KeywordType.SPACE_TYPE, "개인 연습 공간", 2, true));
		keywordRepository.save(createKeyword("드럼 세트", KeywordType.INSTRUMENT_EQUIPMENT, "드럼 장비", 1, true));
		keywordRepository.save(createKeyword("피아노", KeywordType.INSTRUMENT_EQUIPMENT, "피아노", 2, true));
		keywordRepository.save(createKeyword("주차 가능", KeywordType.AMENITY, "주차장 제공", 1, true));
		keywordRepository.save(createKeyword("비활성 키워드", KeywordType.SPACE_TYPE, "비활성화된 키워드", 99, false));
	}
	
	/**
	 * 테스트용 Keyword 생성 헬퍼 메서드
	 */
	private Keyword createKeyword(String name, KeywordType type, String description, Integer displayOrder, Boolean isActive) {
		return Keyword.builder()
				.name(name)
				.type(type)
				.description(description)
				.displayOrder(displayOrder)
				.isActive(isActive)
				.build();
	}
	
	@Nested
	@DisplayName("GET /api/v1/keywords - 전체 키워드 조회")
	class GetAllKeywordsTests {
		
		@Test
		@DisplayName("모든 활성화된 키워드를 조회할 수 있다")
		void getAllKeywords_Success() throws Exception {
			mockMvc.perform(get("/api/v1/keywords")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$", hasSize(5)))  // 비활성 키워드 제외
					.andExpect(jsonPath("$[*].name").exists())
					.andExpect(jsonPath("$[*].type").exists())
					.andExpect(jsonPath("$[*].displayOrder").exists());
		}
		
		@Test
		@DisplayName("키워드가 displayOrder 순서대로 정렬된다")
		void getAllKeywords_OrderedByDisplayOrder() throws Exception {
			mockMvc.perform(get("/api/v1/keywords")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].displayOrder").value(1))
					.andExpect(jsonPath("$[1].displayOrder").value(1))
					.andExpect(jsonPath("$[2].displayOrder").value(1));
		}
		
		@Test
		@DisplayName("비활성화된 키워드는 조회되지 않는다")
		void getAllKeywords_ExcludesInactive() throws Exception {
			mockMvc.perform(get("/api/v1/keywords")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[*].name", not(hasItem("비활성 키워드"))));
		}
		
		@Test
		@DisplayName("응답에 모든 필수 필드가 포함된다")
		void getAllKeywords_ContainsRequiredFields() throws Exception {
			mockMvc.perform(get("/api/v1/keywords")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].id").exists())
					.andExpect(jsonPath("$[0].name").exists())
					.andExpect(jsonPath("$[0].type").exists())
					.andExpect(jsonPath("$[0].description").exists())
					.andExpect(jsonPath("$[0].displayOrder").exists());
		}
	}
	
	@Nested
	@DisplayName("GET /api/v1/keywords?type={type} - 타입별 키워드 조회")
	class GetKeywordsByTypeTests {
		
		@Test
		@DisplayName("SPACE_TYPE 키워드만 조회할 수 있다")
		void getKeywordsByType_SpaceType() throws Exception {
			mockMvc.perform(get("/api/v1/keywords")
							.param("type", "SPACE_TYPE")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$", hasSize(2)))  // 합주실, 연습실 (비활성 제외)
					.andExpect(jsonPath("$[0].type").value("SPACE_TYPE"))
					.andExpect(jsonPath("$[1].type").value("SPACE_TYPE"));
		}
		
		@Test
		@DisplayName("INSTRUMENT_EQUIPMENT 키워드만 조회할 수 있다")
		void getKeywordsByType_InstrumentEquipment() throws Exception {
			mockMvc.perform(get("/api/v1/keywords")
							.param("type", "INSTRUMENT_EQUIPMENT")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$", hasSize(2)))  // 드럼 세트, 피아노
					.andExpect(jsonPath("$[*].type", everyItem(is("INSTRUMENT_EQUIPMENT"))));
		}
		
		@Test
		@DisplayName("AMENITY 키워드만 조회할 수 있다")
		void getKeywordsByType_Amenity() throws Exception {
			mockMvc.perform(get("/api/v1/keywords")
							.param("type", "AMENITY")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$", hasSize(1)))  // 주차 가능
					.andExpect(jsonPath("$[0].name").value("주차 가능"));
		}
		
		@Test
		@DisplayName("해당 타입의 키워드가 없으면 빈 배열을 반환한다")
		void getKeywordsByType_EmptyResult() throws Exception {
			mockMvc.perform(get("/api/v1/keywords")
							.param("type", "OTHER_FEATURE")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$", hasSize(0)));
		}
		
		@Test
		@DisplayName("타입별 조회시에도 displayOrder 순서대로 정렬된다")
		void getKeywordsByType_OrderedByDisplayOrder() throws Exception {
			mockMvc.perform(get("/api/v1/keywords")
							.param("type", "SPACE_TYPE")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].displayOrder").value(1))
					.andExpect(jsonPath("$[1].displayOrder").value(2));
		}
		
		@Test
		@DisplayName("타입별 조회시에도 비활성화된 키워드는 제외된다")
		void getKeywordsByType_ExcludesInactive() throws Exception {
			mockMvc.perform(get("/api/v1/keywords")
							.param("type", "SPACE_TYPE")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$", hasSize(2)))  // 비활성 키워드 제외
					.andExpect(jsonPath("$[*].name", not(hasItem("비활성 키워드"))));
		}
	}
	
	@Nested
	@DisplayName("엣지 케이스 테스트")
	class EdgeCaseTests {
		
		@Test
		@DisplayName("키워드가 없을 때 빈 배열을 반환한다")
		void getKeywords_EmptyDatabase() throws Exception {
			// Given
			keywordRepository.deleteAll();
			
			// When & Then
			mockMvc.perform(get("/api/v1/keywords")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$", hasSize(0)));
		}
		
		@Test
		@DisplayName("잘못된 타입 파라미터에 대해 400 에러를 반환한다")
		void getKeywords_InvalidType() throws Exception {
			mockMvc.perform(get("/api/v1/keywords")
							.param("type", "INVALID_TYPE")
							.contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}
	}
}
