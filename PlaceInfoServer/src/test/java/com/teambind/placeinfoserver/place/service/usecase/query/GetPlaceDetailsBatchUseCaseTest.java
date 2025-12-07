package com.teambind.placeinfoserver.place.service.usecase.query;

import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.entity.Room;
import com.teambind.placeinfoserver.place.dto.request.PlaceBatchDetailRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceBatchDetailResponse;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.repository.RoomRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * GetPlaceDetailsBatchUseCase 통합 테스트
 *
 * 테스트 시나리오:
 * - 정상 배치 조회 (모든 ID 존재)
 * - 부분 실패 (일부 ID 존재하지 않음)
 * - 빈 요청 처리
 * - 최대 개수 제한 검증
 * - Room 정보 포함 검증
 * - 중복 ID 처리
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("GetPlaceDetailsBatchUseCase 통합 테스트")
class GetPlaceDetailsBatchUseCaseTest extends BaseIntegrationTest {

    @Autowired
    private GetPlaceDetailsBatchUseCase getPlaceDetailsBatchUseCase;

    @Autowired
    private PlaceInfoRepository placeInfoRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EntityManager entityManager;

    private List<PlaceInfo> testPlaces;
    private List<Long> testPlaceIds;
    private static long roomIdSequence = 1000L;  // Room ID 시퀀스

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        PlaceTestFactory.resetSequence();
        placeInfoRepository.deleteAll();
        roomRepository.deleteAll();

        // 테스트용 PlaceInfo 생성
        testPlaces = createTestPlaces(5);
        testPlaceIds = testPlaces.stream()
                .map(PlaceInfo::getId)
                .collect(Collectors.toList());

        // 일부 Place에 Room 추가
        createTestRooms(testPlaceIds.get(0), 3);  // 첫 번째 place에 3개 room
        createTestRooms(testPlaceIds.get(1), 2);  // 두 번째 place에 2개 room

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("정상 배치 조회")
    class SuccessfulBatchQuery {

        @Test
        @DisplayName("모든 ID가 존재하는 경우 전체 조회 성공")
        void should_return_all_places_when_all_ids_exist() {
            // Given
            PlaceBatchDetailRequest request = PlaceBatchDetailRequest.builder()
                    .placeIds(testPlaceIds)
                    .build();

            // When
            PlaceBatchDetailResponse response = getPlaceDetailsBatchUseCase.execute(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getResults()).hasSize(5);
            assertThat(response.getFailed()).isNullOrEmpty();
            assertThat(response.getSuccessCount()).isEqualTo(5);
            assertThat(response.getTotalRequested()).isEqualTo(5);
        }

        @Test
        @DisplayName("Room 정보가 올바르게 포함되어 조회")
        void should_include_room_information() {
            // Given
            List<Long> placeIdsWithRooms = Arrays.asList(testPlaceIds.get(0), testPlaceIds.get(1));
            PlaceBatchDetailRequest request = PlaceBatchDetailRequest.builder()
                    .placeIds(placeIdsWithRooms)
                    .build();

            // When
            PlaceBatchDetailResponse response = getPlaceDetailsBatchUseCase.execute(request);

            // Then
            assertThat(response.getResults()).hasSize(2);

            PlaceInfoResponse firstPlace = response.getResults().stream()
                    .filter(p -> p.getId().equals(String.valueOf(testPlaceIds.get(0))))
                    .findFirst()
                    .orElseThrow();

            PlaceInfoResponse secondPlace = response.getResults().stream()
                    .filter(p -> p.getId().equals(String.valueOf(testPlaceIds.get(1))))
                    .findFirst()
                    .orElseThrow();

            assertThat(firstPlace.getRoomCount()).isEqualTo(3);
            assertThat(firstPlace.getRoomIds()).hasSize(3);

            assertThat(secondPlace.getRoomCount()).isEqualTo(2);
            assertThat(secondPlace.getRoomIds()).hasSize(2);
        }

        @Test
        @DisplayName("단일 ID 조회")
        void should_handle_single_id_request() {
            // Given
            PlaceBatchDetailRequest request = PlaceBatchDetailRequest.builder()
                    .placeIds(Arrays.asList(testPlaceIds.get(0)))
                    .build();

            // When
            PlaceBatchDetailResponse response = getPlaceDetailsBatchUseCase.execute(request);

            // Then
            assertThat(response.getResults()).hasSize(1);
            assertThat(response.getFailed()).isNullOrEmpty();
            assertThat(response.getSuccessCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("부분 실패 처리")
    class PartialFailureHandling {

        @Test
        @DisplayName("일부 ID가 존재하지 않는 경우 부분 성공 반환")
        void should_return_partial_success_when_some_ids_not_exist() {
            // Given
            List<Long> mixedIds = new ArrayList<>(testPlaceIds);
            mixedIds.add(999999L);  // 존재하지 않는 ID
            mixedIds.add(888888L);  // 존재하지 않는 ID

            PlaceBatchDetailRequest request = PlaceBatchDetailRequest.builder()
                    .placeIds(mixedIds)
                    .build();

            // When
            PlaceBatchDetailResponse response = getPlaceDetailsBatchUseCase.execute(request);

            // Then
            assertThat(response.getResults()).hasSize(5);
            assertThat(response.getFailed()).containsExactlyInAnyOrder(999999L, 888888L);
            assertThat(response.getSuccessCount()).isEqualTo(5);
            assertThat(response.getTotalRequested()).isEqualTo(7);
        }

        @Test
        @DisplayName("모든 ID가 존재하지 않는 경우")
        void should_return_empty_results_when_no_ids_exist() {
            // Given
            List<Long> nonExistentIds = Arrays.asList(999999L, 888888L, 777777L);
            PlaceBatchDetailRequest request = PlaceBatchDetailRequest.builder()
                    .placeIds(nonExistentIds)
                    .build();

            // When
            PlaceBatchDetailResponse response = getPlaceDetailsBatchUseCase.execute(request);

            // Then
            assertThat(response.getResults()).isEmpty();
            assertThat(response.getFailed()).containsExactlyInAnyOrder(999999L, 888888L, 777777L);
            assertThat(response.getSuccessCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("중복 ID 처리")
    class DuplicateIdHandling {

        @Test
        @DisplayName("중복된 ID는 한 번만 조회")
        void should_return_unique_results_for_duplicate_ids() {
            // Given
            List<Long> duplicateIds = Arrays.asList(
                    testPlaceIds.get(0),
                    testPlaceIds.get(0),  // 중복
                    testPlaceIds.get(1),
                    testPlaceIds.get(1),  // 중복
                    testPlaceIds.get(0)   // 중복
            );

            PlaceBatchDetailRequest request = PlaceBatchDetailRequest.builder()
                    .placeIds(duplicateIds)
                    .build();

            // When
            PlaceBatchDetailResponse response = getPlaceDetailsBatchUseCase.execute(request);

            // Then
            assertThat(response.getResults()).hasSize(2);
            assertThat(response.getFailed()).isNullOrEmpty();

            List<String> resultIds = response.getResults().stream()
                    .map(PlaceInfoResponse::getId)
                    .collect(Collectors.toList());

            assertThat(resultIds).containsExactlyInAnyOrder(
                    String.valueOf(testPlaceIds.get(0)),
                    String.valueOf(testPlaceIds.get(1))
            );
        }
    }

    @Nested
    @DisplayName("비활성화된 Place 처리")
    class InactivePlaceHandling {

        @Test
        @DisplayName("비활성화된 Place는 조회되지 않음")
        void should_not_return_inactive_places() {
            // Given
            PlaceInfo inactivePlace = testPlaces.get(0);
            inactivePlace.setIsActive(false);
            placeInfoRepository.save(inactivePlace);
            entityManager.flush();
            entityManager.clear();

            PlaceBatchDetailRequest request = PlaceBatchDetailRequest.builder()
                    .placeIds(Arrays.asList(inactivePlace.getId()))
                    .build();

            // When
            PlaceBatchDetailResponse response = getPlaceDetailsBatchUseCase.execute(request);

            // Then
            assertThat(response.getResults()).isEmpty();
            assertThat(response.getFailed()).contains(inactivePlace.getId());
        }
    }

    // Helper methods
    private List<PlaceInfo> createTestPlaces(int count) {
        List<PlaceInfo> places = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            PlaceInfo place = PlaceTestFactory.createPlaceInfo();
            place.setPlaceName("Test Place " + i);
            places.add(placeInfoRepository.save(place));
        }
        return places;
    }

    private void createTestRooms(Long placeId, int count) {
        for (int i = 1; i <= count; i++) {
            Room room = Room.builder()
                    .placeId(placeId)
                    .roomId(roomIdSequence++)  // 고유한 roomId 시퀀스 사용
                    .isActive(true)
                    .build();
            roomRepository.save(room);
        }
    }
}