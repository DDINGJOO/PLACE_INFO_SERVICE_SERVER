package com.teambind.placeinfoserver.place.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PlaceInfo 배치 상세조회 응답 DTO
 * Gateway 친화적인 구조로 설계 (단순 라우팅, 응답 조합 최적화)
 *
 * @JsonInclude를 사용하여 failed 필드는 실패가 있을 때만 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PlaceBatchDetailResponse {

    /**
     * 조회에 성공한 PlaceInfo 목록
     * 요청 순서와 관계없이 조회된 결과만 포함
     */
    private List<PlaceInfoResponse> results;

    /**
     * 조회에 실패한 placeId 목록
     * 존재하지 않거나 접근 권한이 없는 ID
     * 실패가 없으면 JSON 응답에서 제외됨
     */
    private List<Long> failed;

    /**
     * Builder 패턴을 위한 정적 팩토리 메서드
     * 성공한 결과만 있는 경우
     */
    public static PlaceBatchDetailResponse ofSuccess(List<PlaceInfoResponse> results) {
        return PlaceBatchDetailResponse.builder()
                .results(results)
                .build();
    }

    /**
     * Builder 패턴을 위한 정적 팩토리 메서드
     * 부분 실패가 있는 경우
     */
    public static PlaceBatchDetailResponse ofPartialSuccess(
            List<PlaceInfoResponse> results,
            List<Long> failedIds) {
        return PlaceBatchDetailResponse.builder()
                .results(results)
                .failed(failedIds.isEmpty() ? null : failedIds)
                .build();
    }

    /**
     * 전체 요청 개수 (성공 + 실패)
     */
    public int getTotalRequested() {
        int successCount = results != null ? results.size() : 0;
        int failedCount = failed != null ? failed.size() : 0;
        return successCount + failedCount;
    }

    /**
     * 성공한 조회 개수
     */
    public int getSuccessCount() {
        return results != null ? results.size() : 0;
    }
}