package com.teambind.placeinfoserver.place.controller;

import com.teambind.placeinfoserver.place.controller.swagger.KeywordControllerSwagger;
import com.teambind.placeinfoserver.place.domain.enums.KeywordType;
import com.teambind.placeinfoserver.place.dto.response.KeywordResponse;
import com.teambind.placeinfoserver.place.service.usecase.query.GetAllKeywordsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/keywords")
@RequiredArgsConstructor
public class KeywordController implements KeywordControllerSwagger {

	private final GetAllKeywordsUseCase getAllKeywordsUseCase;

	@Override
	@GetMapping
	public ResponseEntity<List<KeywordResponse>> getKeywords(
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
