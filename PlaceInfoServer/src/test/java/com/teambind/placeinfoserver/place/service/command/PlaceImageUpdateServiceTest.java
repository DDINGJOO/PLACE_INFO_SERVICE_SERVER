package com.teambind.placeinfoserver.place.service.command;

import com.teambind.placeinfoserver.place.common.exception.CustomException;
import com.teambind.placeinfoserver.place.config.BaseIntegrationTest;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.events.event.ImagesChangeEventWrapper;
import com.teambind.placeinfoserver.place.events.event.SequentialImageChangeEvent;
import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PlaceImageUpdateService 통합 테스트
 * 업체 이미지 업데이트 서비스 검증
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaceImageUpdateServiceTest extends BaseIntegrationTest {
	
	@Autowired
	private PlaceImageUpdateService imageUpdateService;
	
	@Autowired
	private PlaceInfoRepository placeInfoRepository;
	
	private PlaceInfo testPlace;
	
	@BeforeEach
	void setUp() {
		PlaceTestFactory.resetSequence();
		
		// 테스트 데이터 준비
		testPlace = PlaceTestFactory.createPlaceInfo();
		testPlace = placeInfoRepository.save(testPlace);
	}
	
	// 헬퍼 메서드
	private SequentialImageChangeEvent createImageEvent(String imageUrl) {
		return new SequentialImageChangeEvent(null, imageUrl, null, null);
	}
	
	@Nested
	@DisplayName("이미지 업데이트 테스트")
	class UpdateImageTest {
		
		@Test
		@Order(1)
		@DisplayName("새 이미지 추가 - 성공")
		void updateImage_AddNew_Success() {
			// Given
			List<SequentialImageChangeEvent> images = new ArrayList<>();
			images.add(createImageEvent("https://example.com/image1.jpg"));
			images.add(createImageEvent("https://example.com/image2.jpg"));
			images.add(createImageEvent("https://example.com/image3.jpg"));
			
			ImagesChangeEventWrapper event = ImagesChangeEventWrapper.builder()
					.referenceId(String.valueOf(testPlace.getId()))
					.images(images)
					.build();
			
			// When
			String resultId = imageUpdateService.updateImage(event);
			
			// Then
			assertThat(resultId).isEqualTo(testPlace.getId());
			
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getImages()).hasSize(3);
			assertThat(updatedPlace.getImages().get(0).getImageUrl()).isEqualTo("https://example.com/image1.jpg");
			assertThat(updatedPlace.getImages().get(1).getImageUrl()).isEqualTo("https://example.com/image2.jpg");
			assertThat(updatedPlace.getImages().get(2).getImageUrl()).isEqualTo("https://example.com/image3.jpg");
		}
		
		@Test
		@Order(2)
		@DisplayName("기존 이미지 교체 - 성공")
		void updateImage_Replace_Success() {
			// Given - 먼저 이미지 추가
			testPlace.addImage(PlaceTestFactory.createPlaceImage(testPlace, 1));
			testPlace.addImage(PlaceTestFactory.createPlaceImage(testPlace, 2));
			testPlace = placeInfoRepository.save(testPlace);
			
			assertThat(testPlace.getImages()).hasSize(2);
			
			// 새로운 이미지로 교체
			List<SequentialImageChangeEvent> newImages = new ArrayList<>();
			newImages.add(createImageEvent("https://example.com/new1.jpg"));
			newImages.add(createImageEvent("https://example.com/new2.jpg"));
			newImages.add(createImageEvent("https://example.com/new3.jpg"));
			
			ImagesChangeEventWrapper event = ImagesChangeEventWrapper.builder()
					.referenceId(String.valueOf(testPlace.getId()))
					.images(newImages)
					.build();
			
			// When
			String resultId = imageUpdateService.updateImage(event);
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getImages()).hasSize(3);
			assertThat(updatedPlace.getImages().get(0).getImageUrl()).isEqualTo("https://example.com/new1.jpg");
		}
		
		@Test
		@Order(3)
		@DisplayName("모든 이미지 삭제 - 성공")
		void updateImage_RemoveAll_Success() {
			// Given - 먼저 이미지 추가
			testPlace.addImage(PlaceTestFactory.createPlaceImage(testPlace, 1));
			testPlace.addImage(PlaceTestFactory.createPlaceImage(testPlace, 2));
			testPlace = placeInfoRepository.save(testPlace);
			
			assertThat(testPlace.getImages()).hasSize(2);
			
			// 빈 리스트로 업데이트
			ImagesChangeEventWrapper event = ImagesChangeEventWrapper.builder()
					.referenceId(String.valueOf(testPlace.getId()))
					.images(new ArrayList<>())
					.build();
			
			// When
			String resultId = imageUpdateService.updateImage(event);
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getImages()).isEmpty();
		}
		
		@Test
		@Order(4)
		@DisplayName("이미지 순서 유지 - 성공")
		void updateImage_MaintainOrder_Success() {
			// Given
			List<SequentialImageChangeEvent> images = new ArrayList<>();
			images.add(createImageEvent("https://example.com/first.jpg"));
			images.add(createImageEvent("https://example.com/second.jpg"));
			images.add(createImageEvent("https://example.com/third.jpg"));
			images.add(createImageEvent("https://example.com/fourth.jpg"));
			
			ImagesChangeEventWrapper event = ImagesChangeEventWrapper.builder()
					.referenceId(String.valueOf(testPlace.getId()))
					.images(images)
					.build();
			
			// When
			imageUpdateService.updateImage(event);
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getImages()).hasSize(4);
			assertThat(updatedPlace.getImages().get(0).getImageUrl()).isEqualTo("https://example.com/first.jpg");
			assertThat(updatedPlace.getImages().get(1).getImageUrl()).isEqualTo("https://example.com/second.jpg");
			assertThat(updatedPlace.getImages().get(2).getImageUrl()).isEqualTo("https://example.com/third.jpg");
			assertThat(updatedPlace.getImages().get(3).getImageUrl()).isEqualTo("https://example.com/fourth.jpg");
		}
	}
	
	@Nested
	@DisplayName("예외 처리 테스트")
	class ExceptionTest {
		
		@Test
		@Order(5)
		@DisplayName("존재하지 않는 업체 - 예외 발생")
		void updateImage_PlaceNotFound_ThrowsException() {
			// Given
			ImagesChangeEventWrapper event = ImagesChangeEventWrapper.builder()
					.referenceId("invalid_place_id")
					.images(List.of(createImageEvent("https://example.com/image.jpg")))
					.build();
			
			// When & Then
			assertThatThrownBy(() -> imageUpdateService.updateImage(event))
					.isInstanceOf(CustomException.class);
		}
		
		@Test
		@Order(6)
		@DisplayName("null 이미지 리스트 처리 - 성공")
		void updateImage_NullImageList_Success() {
			// Given
			ImagesChangeEventWrapper event = ImagesChangeEventWrapper.builder()
					.referenceId(String.valueOf(testPlace.getId()))
					.images(null)
					.build();
			
			// When
			String resultId = imageUpdateService.updateImage(event);
			
			// Then
			assertThat(resultId).isEqualTo(testPlace.getId());
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getImages()).isEmpty();
		}
	}
	
	@Nested
	@DisplayName("트랜잭션 및 영속성 테스트")
	class TransactionTest {
		
		@Test
		@Order(7)
		@DisplayName("더티 체킹으로 변경사항 자동 반영")
		void updateImage_DirtyChecking_Success() {
			// Given
			List<SequentialImageChangeEvent> images = new ArrayList<>();
			images.add(createImageEvent("https://example.com/img1.jpg"));
			
			ImagesChangeEventWrapper event = ImagesChangeEventWrapper.builder()
					.referenceId(String.valueOf(testPlace.getId()))
					.images(images)
					.build();
			
			// When
			imageUpdateService.updateImage(event);
			
			// Then - 트랜잭션 커밋 후 조회하여 확인
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getImages()).hasSize(1);
			assertThat(updatedPlace.getImages().get(0).getImageUrl()).isEqualTo("https://example.com/img1.jpg");
		}
		
		@Test
		@Order(8)
		@DisplayName("여러 번 업데이트 - 마지막 상태 유지")
		void updateImage_MultipleUpdates_LastStatePreserved() {
			// Given & When
			// 첫 번째 업데이트
			ImagesChangeEventWrapper event1 = ImagesChangeEventWrapper.builder()
					.referenceId(String.valueOf(testPlace.getId()))
					.images(List.of(createImageEvent("https://example.com/v1.jpg")))
					.build();
			imageUpdateService.updateImage(event1);
			
			// 두 번째 업데이트
			ImagesChangeEventWrapper event2 = ImagesChangeEventWrapper.builder()
					.referenceId(String.valueOf(testPlace.getId()))
					.images(List.of(
							createImageEvent("https://example.com/v2-1.jpg"),
							createImageEvent("https://example.com/v2-2.jpg")
					))
					.build();
			imageUpdateService.updateImage(event2);
			
			// 세 번째 업데이트
			ImagesChangeEventWrapper event3 = ImagesChangeEventWrapper.builder()
					.referenceId(String.valueOf(testPlace.getId()))
					.images(List.of(createImageEvent("https://example.com/v3.jpg")))
					.build();
			imageUpdateService.updateImage(event3);
			
			// Then
			PlaceInfo finalPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(finalPlace.getImages()).hasSize(1);
			assertThat(finalPlace.getImages().get(0).getImageUrl()).isEqualTo("https://example.com/v3.jpg");
		}
	}
	
	@Nested
	@DisplayName("대량 이미지 업데이트 테스트")
	class BulkImageUpdateTest {
		
		@Test
		@Order(9)
		@DisplayName("대량 이미지 추가 (10개) - 성공")
		void updateImage_BulkAdd_Success() {
			// Given
			List<SequentialImageChangeEvent> images = new ArrayList<>();
			for (int i = 1; i <= 10; i++) {
				images.add(createImageEvent("https://example.com/image" + i + ".jpg"));
			}
			
			ImagesChangeEventWrapper event = ImagesChangeEventWrapper.builder()
					.referenceId(String.valueOf(testPlace.getId()))
					.images(images)
					.build();
			
			// When
			imageUpdateService.updateImage(event);
			
			// Then
			PlaceInfo updatedPlace = placeInfoRepository.findById(testPlace.getId()).orElseThrow();
			assertThat(updatedPlace.getImages()).hasSize(10);
			assertThat(updatedPlace.getImages().get(0).getImageUrl()).isEqualTo("https://example.com/image1.jpg");
			assertThat(updatedPlace.getImages().get(9).getImageUrl()).isEqualTo("https://example.com/image10.jpg");
		}
	}
}
