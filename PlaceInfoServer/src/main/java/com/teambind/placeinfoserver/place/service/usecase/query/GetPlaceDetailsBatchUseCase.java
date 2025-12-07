package com.teambind.placeinfoserver.place.service.usecase.query;

import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.entity.Room;
import com.teambind.placeinfoserver.place.dto.request.PlaceBatchDetailRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceBatchDetailResponse;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.repository.RoomRepository;
import com.teambind.placeinfoserver.place.service.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 업체 배치 상세 조회 UseCase
 *
 * 설계 원칙:
 * - SRP: 배치 상세 조회만을 담당
 * - OCP: 조회 전략 확장 가능
 * - DIP: Repository 인터페이스에 의존
 *
 * 성능 고려사항:
 * - N+1 문제 방지를 위한 배치 조회
 * - 트랜잭션 readOnly 설정으로 최적화
 * - 메모리 효율을 위한 스트림 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPlaceDetailsBatchUseCase {

    private final PlaceInfoRepository placeInfoRepository;
    private final RoomRepository roomRepository;
    private final PlaceMapper placeMapper;

    /**
     * 업체 배치 상세 조회 실행
     *
     * @param request 배치 조회 요청 (placeId 리스트)
     * @return 조회 결과 (성공한 결과 + 실패한 ID)
     */
    public PlaceBatchDetailResponse execute(PlaceBatchDetailRequest request) {
        List<Long> requestedIds = request.getPlaceIds();

        log.info("배치 상세 조회 시작 - 요청 개수: {}", requestedIds.size());

        // 중복 제거 및 null 체크
        Set<Long> uniqueIds = requestedIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 배치 조회 (N+1 문제 방지를 위한 Fetch Join 사용)
        List<PlaceInfo> foundPlaces = placeInfoRepository.findAllByIdWithDetails(uniqueIds);

        // 조회된 ID Set 생성 (빠른 검색을 위해)
        Set<Long> foundIds = foundPlaces.stream()
                .map(PlaceInfo::getId)
                .collect(Collectors.toSet());

        // 실패한 ID 찾기
        List<Long> failedIds = uniqueIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());

        // PlaceInfo를 Response로 변환
        List<PlaceInfoResponse> responses = convertToResponses(foundPlaces);

        // Room 정보 일괄 조회 및 매핑
        enrichWithRoomInfo(responses, foundIds);

        log.info("배치 상세 조회 완료 - 성공: {}, 실패: {}", responses.size(), failedIds.size());

        return PlaceBatchDetailResponse.ofPartialSuccess(responses, failedIds);
    }

    /**
     * PlaceInfo 엔티티를 Response DTO로 변환
     */
    private List<PlaceInfoResponse> convertToResponses(List<PlaceInfo> places) {
        return places.stream()
                .map(placeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Room 정보를 일괄 조회하여 Response에 추가
     * N+1 문제 방지를 위해 한 번의 쿼리로 모든 Room 정보 조회
     */
    private void enrichWithRoomInfo(List<PlaceInfoResponse> responses, Set<Long> placeIds) {
        // 모든 placeId에 대한 Room 정보를 한 번에 조회
        Map<Long, List<Room>> roomsByPlaceId = roomRepository
                .findByPlaceIdInAndIsActiveTrue(placeIds)
                .stream()
                .collect(Collectors.groupingBy(Room::getPlaceId));

        // Response에 Room 정보 매핑
        responses.forEach(response -> {
            Long placeId = Long.parseLong(response.getId());
            List<Room> rooms = roomsByPlaceId.getOrDefault(placeId, List.of());

            response.setRoomCount(rooms.size());
            response.setRoomIds(rooms.stream()
                    .map(Room::getRoomId)
                    .collect(Collectors.toList()));
        });
    }
}