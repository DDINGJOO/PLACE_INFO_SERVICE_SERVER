package com.teambind.placeinfoserver.place.domain.entity;

import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PlaceImage 엔티티 단위 테스트
 * 업체 이미지 정보 도메인 로직 검증
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaceImageTest {

	@Nested
	@DisplayName("엔티티 생성 테스트")
	class CreationTest {

		@Test
		@Order(1)
		@DisplayName("PlaceImage 생성 - 성공")
		void create_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image = PlaceImage.builder()
					.id("img_test_123")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/images/test.jpg")
					.build();

			// Then
			assertThat(image).isNotNull();
			assertThat(image.getId()).isEqualTo("img_test_123");
			assertThat(image.getPlaceInfo()).isEqualTo(placeInfo);
			assertThat(image.getImageUrl()).isEqualTo("https://example.com/images/test.jpg");
		}

		@Test
		@Order(2)
		@DisplayName("PlaceImage 생성 - 최소 정보")
		void create_Minimal_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image = PlaceImage.builder()
					.id("img_minimal")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/image.jpg")
					.build();

			// Then
			assertThat(image).isNotNull();
			assertThat(image.getImageUrl()).isNotNull();
		}

		@Test
		@Order(3)
		@DisplayName("다양한 ID 형식 - 성공")
		void create_VariousIdFormats_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When & Then
			PlaceImage image1 = PlaceImage.builder()
					.id("img_uuid_12345")
					.placeInfo(placeInfo)
					.imageUrl("url1")
					.build();
			assertThat(image1.getId()).isEqualTo("img_uuid_12345");

			PlaceImage image2 = PlaceImage.builder()
					.id("IMAGE-001")
					.placeInfo(placeInfo)
					.imageUrl("url2")
					.build();
			assertThat(image2.getId()).isEqualTo("IMAGE-001");

			PlaceImage image3 = PlaceImage.builder()
					.id("12345")
					.placeInfo(placeInfo)
					.imageUrl("url3")
					.build();
			assertThat(image3.getId()).isEqualTo("12345");
		}
	}

	@Nested
	@DisplayName("이미지 URL 테스트")
	class ImageUrlTest {

		@Test
		@Order(4)
		@DisplayName("HTTPS URL - 성공")
		void setImageUrl_Https_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image = PlaceImage.builder()
					.id("img_https")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/secure/image.jpg")
					.build();

			// Then
			assertThat(image.getImageUrl()).startsWith("https://");
		}

		@Test
		@Order(5)
		@DisplayName("HTTP URL - 성공")
		void setImageUrl_Http_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image = PlaceImage.builder()
					.id("img_http")
					.placeInfo(placeInfo)
					.imageUrl("http://example.com/image.jpg")
					.build();

			// Then
			assertThat(image.getImageUrl()).startsWith("http://");
		}

		@Test
		@Order(6)
		@DisplayName("다양한 이미지 파일 확장자 - 성공")
		void setImageUrl_VariousExtensions_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When & Then
			PlaceImage jpg = PlaceImage.builder()
					.id("img_jpg")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/image.jpg")
					.build();
			assertThat(jpg.getImageUrl()).endsWith(".jpg");

			PlaceImage png = PlaceImage.builder()
					.id("img_png")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/image.png")
					.build();
			assertThat(png.getImageUrl()).endsWith(".png");

			PlaceImage webp = PlaceImage.builder()
					.id("img_webp")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/image.webp")
					.build();
			assertThat(webp.getImageUrl()).endsWith(".webp");
		}

		@Test
		@Order(7)
		@DisplayName("쿼리 파라미터가 있는 URL - 성공")
		void setImageUrl_WithQueryParams_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image = PlaceImage.builder()
					.id("img_query")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/image.jpg?size=large&quality=high")
					.build();

			// Then
			assertThat(image.getImageUrl()).contains("?");
			assertThat(image.getImageUrl()).contains("size=large");
		}

		@Test
		@Order(8)
		@DisplayName("CDN URL - 성공")
		void setImageUrl_Cdn_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image = PlaceImage.builder()
					.id("img_cdn")
					.placeInfo(placeInfo)
					.imageUrl("https://cdn.example.com/images/places/12345.jpg")
					.build();

			// Then
			assertThat(image.getImageUrl()).contains("cdn");
		}

		@Test
		@Order(9)
		@DisplayName("이미지 ID 참조 형식 - 성공")
		void setImageUrl_IdReference_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image = PlaceImage.builder()
					.id("img_ref")
					.placeInfo(placeInfo)
					.imageUrl("image://service-id/12345")
					.build();

			// Then
			assertThat(image.getImageUrl()).startsWith("image://");
		}
	}

	@Nested
	@DisplayName("연관관계 테스트")
	class RelationshipTest {

		@Test
		@Order(10)
		@DisplayName("PlaceInfo와의 연관관계 - 다대일")
		void relationship_WithPlaceInfo_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image = PlaceImage.builder()
					.id("img_rel")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/image.jpg")
					.build();

			// Then
			assertThat(image.getPlaceInfo()).isEqualTo(placeInfo);
			assertThat(image.getPlaceInfo().getId()).isEqualTo(placeInfo.getId());
		}

		@Test
		@Order(11)
		@DisplayName("여러 이미지가 하나의 PlaceInfo에 속함")
		void relationship_MultipleImages_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image1 = PlaceImage.builder()
					.id("img_1")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/image1.jpg")
					.build();

			PlaceImage image2 = PlaceImage.builder()
					.id("img_2")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/image2.jpg")
					.build();

			PlaceImage image3 = PlaceImage.builder()
					.id("img_3")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/image3.jpg")
					.build();

			// Then
			assertThat(image1.getPlaceInfo()).isEqualTo(placeInfo);
			assertThat(image2.getPlaceInfo()).isEqualTo(placeInfo);
			assertThat(image3.getPlaceInfo()).isEqualTo(placeInfo);
		}
	}

	@Nested
	@DisplayName("Setter 메서드 테스트")
	class SetterTest {

		@Test
		@Order(12)
		@DisplayName("이미지 URL 변경 - 성공")
		void setImageUrl_Change_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceImage image = PlaceImage.builder()
					.id("img_change")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/old.jpg")
					.build();

			// When
			image.setImageUrl("https://example.com/new.jpg");

			// Then
			assertThat(image.getImageUrl()).isEqualTo("https://example.com/new.jpg");
		}

		@Test
		@Order(13)
		@DisplayName("PlaceInfo 변경 - 성공")
		void setPlaceInfo_Change_Success() {
			// Given
			PlaceInfo placeInfo1 = PlaceTestFactory.createPlaceInfo();
			PlaceInfo placeInfo2 = PlaceTestFactory.createPlaceInfo();
			PlaceImage image = PlaceImage.builder()
					.id("img_move")
					.placeInfo(placeInfo1)
					.imageUrl("https://example.com/image.jpg")
					.build();

			// When
			image.setPlaceInfo(placeInfo2);

			// Then
			assertThat(image.getPlaceInfo()).isEqualTo(placeInfo2);
		}
	}

	@Nested
	@DisplayName("팩토리 메서드 테스트")
	class FactoryMethodTest {

		@Test
		@Order(14)
		@DisplayName("PlaceTestFactory로 생성 - 성공")
		void createWithFactory_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image = PlaceTestFactory.createPlaceImage(placeInfo, 1);

			// Then
			assertThat(image).isNotNull();
			assertThat(image.getId()).isNotNull();
			assertThat(image.getPlaceInfo()).isEqualTo(placeInfo);
			assertThat(image.getImageUrl()).contains("test_1.jpg");
		}

		@Test
		@Order(15)
		@DisplayName("여러 이미지를 순서대로 생성 - 성공")
		void createMultipleWithFactory_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image1 = PlaceTestFactory.createPlaceImage(placeInfo, 1);
			PlaceImage image2 = PlaceTestFactory.createPlaceImage(placeInfo, 2);
			PlaceImage image3 = PlaceTestFactory.createPlaceImage(placeInfo, 3);

			// Then
			assertThat(image1.getImageUrl()).contains("test_1.jpg");
			assertThat(image2.getImageUrl()).contains("test_2.jpg");
			assertThat(image3.getImageUrl()).contains("test_3.jpg");
		}
	}

	@Nested
	@DisplayName("실제 데이터 테스트")
	class RealDataTest {

		@Test
		@Order(16)
		@DisplayName("AWS S3 URL - 성공")
		void create_AwsS3Url_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image = PlaceImage.builder()
					.id("img_s3")
					.placeInfo(placeInfo)
					.imageUrl("https://s3.ap-northeast-2.amazonaws.com/bucket-name/images/place123.jpg")
					.build();

			// Then
			assertThat(image.getImageUrl()).contains("s3");
			assertThat(image.getImageUrl()).contains("amazonaws.com");
		}

		@Test
		@Order(17)
		@DisplayName("CloudFront CDN URL - 성공")
		void create_CloudFrontUrl_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image = PlaceImage.builder()
					.id("img_cf")
					.placeInfo(placeInfo)
					.imageUrl("https://d1234567.cloudfront.net/images/place/123.jpg")
					.build();

			// Then
			assertThat(image.getImageUrl()).contains("cloudfront.net");
		}

		@Test
		@Order(18)
		@DisplayName("긴 파일명 - 성공")
		void create_LongFilename_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image = PlaceImage.builder()
					.id("img_long")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/images/places/very-long-descriptive-filename-with-many-words-2024-01-01.jpg")
					.build();

			// Then
			assertThat(image.getImageUrl()).hasSizeGreaterThan(50);
		}

		@Test
		@Order(19)
		@DisplayName("UUID 기반 파일명 - 성공")
		void create_UuidFilename_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image = PlaceImage.builder()
					.id("img_uuid")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/images/550e8400-e29b-41d4-a716-446655440000.jpg")
					.build();

			// Then
			assertThat(image.getImageUrl()).matches(".*[a-f0-9-]{36}\\.jpg");
		}
	}

	@Nested
	@DisplayName("통합 시나리오 테스트")
	class IntegrationScenarioTest {

		@Test
		@Order(20)
		@DisplayName("PlaceInfo에 이미지 추가 및 조회")
		void addImageToPlaceInfo_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceImage image1 = PlaceImage.builder()
					.id("img_scenario_1")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/image1.jpg")
					.build();

			PlaceImage image2 = PlaceImage.builder()
					.id("img_scenario_2")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/image2.jpg")
					.build();

			placeInfo.addImage(image1);
			placeInfo.addImage(image2);

			// Then
			assertThat(placeInfo.getImages()).hasSize(2);
			assertThat(placeInfo.getImages()).contains(image1, image2);
		}

		@Test
		@Order(21)
		@DisplayName("썸네일 이미지 (첫 번째 이미지)")
		void getThumbnailImage_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceImage thumbnail = PlaceImage.builder()
					.id("img_thumb")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/thumbnail.jpg")
					.build();

			PlaceImage detail = PlaceImage.builder()
					.id("img_detail")
					.placeInfo(placeInfo)
					.imageUrl("https://example.com/detail.jpg")
					.build();

			// When
			placeInfo.addImage(thumbnail);
			placeInfo.addImage(detail);

			// Then
			assertThat(placeInfo.getImages()).isNotEmpty();
			assertThat(placeInfo.getImages().get(0)).isEqualTo(thumbnail);
		}
	}
}
