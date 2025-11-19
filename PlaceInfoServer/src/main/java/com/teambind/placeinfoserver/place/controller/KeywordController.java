package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.domain.enums.KeywordType;
import com.teambind.placeinfoserver.place.dto.response.KeywordResponse;
import com.teambind.placeinfoserver.place.service.usecase.query.GetAllKeywordsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 키워드 조회 REST API 컨트롤러
 * 업체 등록시 사용할 수 있는 키워드 목록 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/keywords")
@RequiredArgsConstructor
@Tag(name = "Keyword", description = "키워드 조회 API")
public class KeywordController {
	
	private final GetAllKeywordsUseCase getAllKeywordsUseCase;
	
	/**
	 * 키워드 목록 조회 API
	 * 타입 필터링 옵션 제공
	 *
	 * @param type (선택) 키워드 타입으로 필터링
	 * @return 키워드 목록
	 */
	@GetMapping
	@Operation(
			summary = "키워드 목록 조회",
			description = "활성화된 키워드 목록을 조회합니다. 타입을 지정하면 해당 타입의 키워드만 조회됩니다."
	)
	@ApiResponse(responseCode = "200", description = "조회 성공")
	public ResponseEntity<List<KeywordResponse>> getKeywords(
			@Parameter(description = "키워드 타입 (SPACE_TYPE, INSTRUMENT_EQUIPMENT, AMENITY, OTHER_FEATURE)", required = false)
			@RequestParam(required = false) KeywordType type
	) {
		log.info("키워드 목록 조회 요청: type={}", type);
		
		List<KeywordResponse> keywords = type == null
				? getAllKeywordsUseCase.execute()
				: getAllKeywordsUseCase.executeByType(type);
		
		log.info("키워드 목록 조회 완료: count={}", keywords.size());
		return ResponseEntity.ok(keywords);
	}
}
