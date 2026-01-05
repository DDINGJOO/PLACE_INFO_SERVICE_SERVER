package com.teambind.placeinfoserver.place.controller.swagger;

import com.teambind.placeinfoserver.place.domain.enums.KeywordType;
import com.teambind.placeinfoserver.place.dto.response.KeywordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Keyword", description = "키워드 조회 API")
public interface KeywordControllerSwagger {

    @Operation(
            summary = "키워드 목록 조회",
            description = "활성화된 키워드 목록을 조회합니다. 타입을 지정하면 해당 타입의 키워드만 조회됩니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<List<KeywordResponse>> getKeywords(
            @Parameter(description = "키워드 타입 (SPACE_TYPE, INSTRUMENT_EQUIPMENT, AMENITY, OTHER_FEATURE)", required = false)
            KeywordType type);
}
