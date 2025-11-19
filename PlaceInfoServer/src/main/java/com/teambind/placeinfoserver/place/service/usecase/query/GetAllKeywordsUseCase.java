package com.teambind.placeinfoserver.place.service.usecase.query;

import com.teambind.placeinfoserver.place.domain.entity.Keyword;
import com.teambind.placeinfoserver.place.domain.enums.KeywordType;
import com.teambind.placeinfoserver.place.dto.response.KeywordResponse;
import com.teambind.placeinfoserver.place.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 키워드 전체 조회 UseCase
 * SRP: 활성화된 키워드 목록 조회만을 담당
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllKeywordsUseCase {
	
	private final KeywordRepository keywordRepository;
	
	/**
	 * 활성화된 모든 키워드 조회
	 *
	 * @return 활성화된 키워드 목록 (표시 순서대로 정렬)
	 */
	public List<KeywordResponse> execute() {
		List<Keyword> keywords = keywordRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
		return keywords.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}
	
	/**
	 * 특정 타입의 활성화된 키워드 조회
	 *
	 * @param type 키워드 타입
	 * @return 해당 타입의 활성화된 키워드 목록 (표시 순서대로 정렬)
	 */
	public List<KeywordResponse> executeByType(KeywordType type) {
		List<Keyword> keywords = keywordRepository.findByTypeAndIsActiveTrueOrderByDisplayOrderAsc(type);
		return keywords.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}
	
	/**
	 * Keyword 엔티티를 KeywordResponse DTO로 변환
	 */
	private KeywordResponse toResponse(Keyword keyword) {
		return KeywordResponse.builder()
				.id(keyword.getId())
				.name(keyword.getName())
				.type(keyword.getType())
				.description(keyword.getDescription())
				.displayOrder(keyword.getDisplayOrder())
				.build();
	}
}
