package com.teambind.placeinfoserver.place.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin", description = "관리자 전용 API")
public interface AdminControllerSwagger {

    @Operation(summary = "업체 승인/거부", description = "업체의 승인 상태를 변경합니다")
    @ApiResponse(responseCode = "204", description = "상태 변경 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 업체")
    ResponseEntity<Void> approve(
            @Parameter(description = "작업 타입 (approve)", required = true) String type,
            @Parameter(description = "승인 여부 (true: 승인, false: 거부)", required = true) boolean contents,
            @Parameter(description = "공간 ID", required = true) String placeId);

    @Operation(summary = "업체 삭제 (관리자)", description = "관리자 권한으로 업체를 삭제합니다")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 업체")
    ResponseEntity<Void> delete(
            @Parameter(description = "공간 ID", required = true) String placeId);
}
