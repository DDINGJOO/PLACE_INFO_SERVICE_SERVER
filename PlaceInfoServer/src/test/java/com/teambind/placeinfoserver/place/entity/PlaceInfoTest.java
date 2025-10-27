package com.teambind.placeinfoserver.place.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PlaceInfo 엔티티 테스트")
class PlaceInfoTest {
	
	@Nested
	@DisplayName("엔티티 생성 테스트")
	class CreateTest {
		
		@Test
		@DisplayName("정상: 빌더로 PlaceInfo 생성")
		void createPlaceInfoWithBuilder() {
			// given & when
			PlaceInfo placeInfo = PlaceInfo.builder()
					.userId("user123")
					.placeName("테스트 연습실")
					.description("조용한 연습실입니다")
					.category("연습실")
					.placeType("음악")
					.build();
			
			// then
			assertThat(placeInfo).isNotNull();
			assertThat(placeInfo.getUserId()).isEqualTo("user123");
			assertThat(placeInfo.getPlaceName()).isEqualTo("테스트 연습실");
			assertThat(placeInfo.getDescription()).isEqualTo("조용한 연습실입니다");
			assertThat(placeInfo.getCategory()).isEqualTo("연습실");
			assertThat(placeInfo.getPlaceType()).isEqualTo("음악");
			assertThat(placeInfo.getIsActive()).isTrue();
			assertThat(placeInfo.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
			assertThat(placeInfo.getReviewCount()).isZero();
		}
		
		@Test
		@DisplayName("정상: 기본값이 올바르게 설정됨")
		void createPlaceInfoWithDefaultValues() {
			// given & when
			PlaceInfo placeInfo = PlaceInfo.builder()
					.userId("user123")
					.placeName("테스트 연습실")
					.build();
			
			// then
			assertThat(placeInfo.getIsActive()).isTrue();
			assertThat(placeInfo.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
			assertThat(placeInfo.getReviewCount()).isZero();
			assertThat(placeInfo.getImages()).isEmpty();
			assertThat(placeInfo.getKeywords()).isEmpty();
		}
	}
	
	@Nested
	@DisplayName("키워드 관리 테스트")
	class KeywordManagementTest {
		
		private PlaceInfo placeInfo;
		
		@BeforeEach
		void setUp() {
			placeInfo = PlaceInfo.builder()
					.userId("user123")
					.placeName("테스트 연습실")
					.build();
		}
		
		@Test
		@DisplayName("정상: 키워드 추가")
		void addKeyword() {
			// given
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.build();
			
			// when
			placeInfo.addKeyword(keyword);
			
			// then
			assertThat(placeInfo.getKeywords()).hasSize(1);
			assertThat(placeInfo.getKeywords()).contains(keyword);
		}
		
		@Test
		@DisplayName("정상: 여러 키워드 추가")
		void addMultipleKeywords() {
			// given
			for (int i = 0; i < 5; i++) {
				Keyword keyword = Keyword.builder()
						.name("키워드" + i)
						.type(KeywordType.AMENITY)
						.build();
				
				// when
				placeInfo.addKeyword(keyword);
			}
			
			// then
			assertThat(placeInfo.getKeywords()).hasSize(5);
		}
		
		@Test
		@DisplayName("엣지: 키워드 정확히 10개 추가")
		void addExactlyTenKeywords() {
			// given
			for (int i = 0; i < 10; i++) {
				Keyword keyword = Keyword.builder()
						.name("키워드" + i)
						.type(KeywordType.AMENITY)
						.build();
				placeInfo.addKeyword(keyword);
			}
			
			// then
			assertThat(placeInfo.getKeywords()).hasSize(10);
		}
		
		@Test
		@DisplayName("예외: 키워드 10개 초과 시 예외 발생")
		void addMoreThanTenKeywords() {
			// given
			for (int i = 0; i < 10; i++) {
				Keyword keyword = Keyword.builder()
						.name("키워드" + i)
						.type(KeywordType.AMENITY)
						.build();
				placeInfo.addKeyword(keyword);
			}
			
			Keyword eleventhKeyword = Keyword.builder()
					.name("11번째 키워드")
					.type(KeywordType.AMENITY)
					.build();
			
			// when & then
			assertThatThrownBy(() -> placeInfo.addKeyword(eleventhKeyword))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("키워드는 최대 10개까지만 선택 가능합니다");
		}
		
		@Test
		@DisplayName("정상: 키워드 제거")
		void removeKeyword() {
			// given
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.build();
			placeInfo.addKeyword(keyword);
			
			// when
			placeInfo.removeKeyword(keyword);
			
			// then
			assertThat(placeInfo.getKeywords()).isEmpty();
		}
	}
	
	@Nested
	@DisplayName("이미지 관리 테스트")
	class ImageManagementTest {
		
		private PlaceInfo placeInfo;
		
		@BeforeEach
		void setUp() {
			placeInfo = PlaceInfo.builder()
					.userId("user123")
					.placeName("테스트 연습실")
					.build();
		}
		
		@Test
		@DisplayName("정상: 이미지 추가")
		void addImage() {
			// given
			PlaceImage image = PlaceImage.builder()
					.id("img-001")
					.imageUrl("https://example.com/image1.jpg")
					.build();
			
			// when
			placeInfo.addImage(image);
			
			// then
			assertThat(placeInfo.getImages()).hasSize(1);
			assertThat(placeInfo.getImages()).contains(image);
			assertThat(image.getPlaceInfo()).isEqualTo(placeInfo);
		}
		
		@Test
		@DisplayName("정상: 여러 이미지 추가")
		void addMultipleImages() {
			// given & when
			for (int i = 0; i < 5; i++) {
				PlaceImage image = PlaceImage.builder()
						.id("img-00" + i)
						.imageUrl("https://example.com/image" + i + ".jpg")
						.build();
				placeInfo.addImage(image);
			}
			
			// then
			assertThat(placeInfo.getImages()).hasSize(5);
		}
		
		@Test
		@DisplayName("엣지: 이미지 정확히 10장 추가")
		void addExactlyTenImages() {
			// given & when
			for (int i = 0; i < 10; i++) {
				PlaceImage image = PlaceImage.builder()
						.id("img-00" + i)
						.imageUrl("https://example.com/image" + i + ".jpg")
						.build();
				placeInfo.addImage(image);
			}
			
			// then
			assertThat(placeInfo.getImages()).hasSize(10);
		}
		
		@Test
		@DisplayName("예외: 이미지 10장 초과 시 예외 발생")
		void addMoreThanTenImages() {
			// given
			for (int i = 0; i < 10; i++) {
				PlaceImage image = PlaceImage.builder()
						.id("img-00" + i)
						.imageUrl("https://example.com/image" + i + ".jpg")
						.build();
				placeInfo.addImage(image);
			}
			
			PlaceImage eleventhImage = PlaceImage.builder()
					.id("img-011")
					.imageUrl("https://example.com/image11.jpg")
					.build();
			
			// when & then
			assertThatThrownBy(() -> placeInfo.addImage(eleventhImage))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("이미지는 최대 10장까지만 등록 가능합니다");
		}
		
		@Test
		@DisplayName("정상: 이미지 제거")
		void removeImage() {
			// given
			PlaceImage image = PlaceImage.builder()
					.id("img-001")
					.imageUrl("https://example.com/image1.jpg")
					.build();
			placeInfo.addImage(image);
			
			// when
			placeInfo.removeImage(image);
			
			// then
			assertThat(placeInfo.getImages()).isEmpty();
			assertThat(image.getPlaceInfo()).isNull();
		}
	}
	
	@Nested
	@DisplayName("연관관계 설정 테스트")
	class RelationshipTest {
		
		private PlaceInfo placeInfo;
		
		@BeforeEach
		void setUp() {
			placeInfo = PlaceInfo.builder()
					.userId("user123")
					.placeName("테스트 연습실")
					.build();
		}
		
		@Test
		@DisplayName("정상: 연락처 정보 설정")
		void setContact() {
			// given
			PlaceContact contact = PlaceContact.builder()
					.contact("02-1234-5678")
					.build();
			
			// when
			placeInfo.setContact(contact);
			
			// then
			assertThat(placeInfo.getContact()).isEqualTo(contact);
			assertThat(contact.getPlaceInfo()).isEqualTo(placeInfo);
		}
		
		@Test
		@DisplayName("정상: 연락처 정보 변경 (양방향 관계 유지)")
		void changeContact() {
			// given
			PlaceContact oldContact = PlaceContact.builder()
					.contact("02-1234-5678")
					.build();
			placeInfo.setContact(oldContact);
			
			PlaceContact newContact = PlaceContact.builder()
					.contact("02-9876-5432")
					.build();
			
			// when
			placeInfo.setContact(newContact);
			
			// then
			assertThat(placeInfo.getContact()).isEqualTo(newContact);
			assertThat(newContact.getPlaceInfo()).isEqualTo(placeInfo);
			assertThat(oldContact.getPlaceInfo()).isNull();
		}
		
		@Test
		@DisplayName("정상: 위치 정보 설정")
		void setLocation() {
			// given
			PlaceLocation location = PlaceLocation.builder()
					.latitude(37.5665)
					.longitude(126.9780)
					.build();
			
			// when
			placeInfo.setLocation(location);
			
			// then
			assertThat(placeInfo.getLocation()).isEqualTo(location);
			assertThat(location.getPlaceInfo()).isEqualTo(placeInfo);
		}
		
		@Test
		@DisplayName("정상: 위치 정보 변경 (양방향 관계 유지)")
		void changeLocation() {
			// given
			PlaceLocation oldLocation = PlaceLocation.builder()
					.latitude(37.5665)
					.longitude(126.9780)
					.build();
			placeInfo.setLocation(oldLocation);
			
			PlaceLocation newLocation = PlaceLocation.builder()
					.latitude(37.4979)
					.longitude(127.0276)
					.build();
			
			// when
			placeInfo.setLocation(newLocation);
			
			// then
			assertThat(placeInfo.getLocation()).isEqualTo(newLocation);
			assertThat(newLocation.getPlaceInfo()).isEqualTo(placeInfo);
			assertThat(oldLocation.getPlaceInfo()).isNull();
		}
		
		@Test
		@DisplayName("정상: 주차 정보 설정")
		void setParking() {
			// given
			PlaceParking parking = PlaceParking.builder()
					.available(true)
					.parkingType(ParkingType.FREE)
					.description("건물 지하 주차장 이용 가능")
					.build();
			
			// when
			placeInfo.setParking(parking);
			
			// then
			assertThat(placeInfo.getParking()).isEqualTo(parking);
			assertThat(parking.getPlaceInfo()).isEqualTo(placeInfo);
		}
		
		@Test
		@DisplayName("정상: 주차 정보 변경 (양방향 관계 유지)")
		void changeParking() {
			// given
			PlaceParking oldParking = PlaceParking.builder()
					.available(true)
					.parkingType(ParkingType.FREE)
					.description("건물 지하 주차장 이용 가능")
					.build();
			placeInfo.setParking(oldParking);
			
			PlaceParking newParking = PlaceParking.builder()
					.available(false)
					.description("주차 불가")
					.build();
			
			// when
			placeInfo.setParking(newParking);
			
			// then
			assertThat(placeInfo.getParking()).isEqualTo(newParking);
			assertThat(newParking.getPlaceInfo()).isEqualTo(placeInfo);
			assertThat(oldParking.getPlaceInfo()).isNull();
		}
		
		@Test
		@DisplayName("엣지: null로 연락처 제거")
		void removeContactWithNull() {
			// given
			PlaceContact contact = PlaceContact.builder()
					.contact("02-1234-5678")
					.build();
			placeInfo.setContact(contact);
			
			// when
			placeInfo.setContact(null);
			
			// then
			assertThat(placeInfo.getContact()).isNull();
			assertThat(contact.getPlaceInfo()).isNull();
		}
	}
	
	@Nested
	@DisplayName("비즈니스 로직 테스트")
	class BusinessLogicTest {
		
		private PlaceInfo placeInfo;
		
		@BeforeEach
		void setUp() {
			placeInfo = PlaceInfo.builder()
					.userId("user123")
					.placeName("테스트 연습실")
					.build();
		}
		
		@Test
		@DisplayName("정상: 업체 활성화")
		void activate() {
			// given
			placeInfo.deactivate();
			
			// when
			placeInfo.activate();
			
			// then
			assertThat(placeInfo.getIsActive()).isTrue();
		}
		
		@Test
		@DisplayName("정상: 업체 비활성화")
		void deactivate() {
			// when
			placeInfo.deactivate();
			
			// then
			assertThat(placeInfo.getIsActive()).isFalse();
		}
		
		@Test
		@DisplayName("정상: 업체 승인")
		void approve() {
			// when
			placeInfo.approve();
			
			// then
			assertThat(placeInfo.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		}
		
		@Test
		@DisplayName("정상: 업체 거부")
		void reject() {
			// when
			placeInfo.reject();
			
			// then
			assertThat(placeInfo.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
		}
		
		@Test
		@DisplayName("정상: 평점 업데이트")
		void updateRating() {
			// when
			placeInfo.updateRating(4.5, 100);
			
			// then
			assertThat(placeInfo.getRatingAverage()).isEqualTo(4.5);
			assertThat(placeInfo.getReviewCount()).isEqualTo(100);
		}
		
		@Test
		@DisplayName("엣지: 평점 0.0 업데이트")
		void updateRatingWithZero() {
			// when
			placeInfo.updateRating(0.0, 0);
			
			// then
			assertThat(placeInfo.getRatingAverage()).isZero();
			assertThat(placeInfo.getReviewCount()).isZero();
		}
		
		@Test
		@DisplayName("정상: Aggregate 완전성 검증 - 완전한 경우")
		void isComplete_WhenComplete() {
			// given
			PlaceContact contact = PlaceContact.builder()
					.contact("02-1234-5678")
					.build();
			PlaceLocation location = PlaceLocation.builder()
					.latitude(37.5665)
					.longitude(126.9780)
					.build();
			
			placeInfo.setContact(contact);
			placeInfo.setLocation(location);
			
			// when
			boolean isComplete = placeInfo.isComplete();
			
			// then
			assertThat(isComplete).isTrue();
		}
		
		@Test
		@DisplayName("정상: Aggregate 완전성 검증 - 불완전한 경우 (연락처 없음)")
		void isComplete_WhenNoContact() {
			// given
			PlaceLocation location = PlaceLocation.builder()
					.latitude(37.5665)
					.longitude(126.9780)
					.build();
			placeInfo.setLocation(location);
			
			// when
			boolean isComplete = placeInfo.isComplete();
			
			// then
			assertThat(isComplete).isFalse();
		}
		
		@Test
		@DisplayName("정상: Aggregate 완전성 검증 - 불완전한 경우 (위치 없음)")
		void isComplete_WhenNoLocation() {
			// given
			PlaceContact contact = PlaceContact.builder()
					.contact("02-1234-5678")
					.build();
			placeInfo.setContact(contact);
			
			// when
			boolean isComplete = placeInfo.isComplete();
			
			// then
			assertThat(isComplete).isFalse();
		}
		
		@Test
		@DisplayName("정상: Aggregate 완전성 검증 - 불완전한 경우 (업체명 null)")
		void isComplete_WhenPlaceNameIsNull() {
			// given
			placeInfo.setPlaceName(null);
			PlaceContact contact = PlaceContact.builder()
					.contact("02-1234-5678")
					.build();
			PlaceLocation location = PlaceLocation.builder()
					.latitude(37.5665)
					.longitude(126.9780)
					.build();
			placeInfo.setContact(contact);
			placeInfo.setLocation(location);
			
			// when
			boolean isComplete = placeInfo.isComplete();
			
			// then
			assertThat(isComplete).isFalse();
		}
		
		@Test
		@DisplayName("엣지: Aggregate 완전성 검증 - 불완전한 경우 (업체명 공백)")
		void isComplete_WhenPlaceNameIsBlank() {
			// given
			placeInfo.setPlaceName("   ");
			PlaceContact contact = PlaceContact.builder()
					.contact("02-1234-5678")
					.build();
			PlaceLocation location = PlaceLocation.builder()
					.latitude(37.5665)
					.longitude(126.9780)
					.build();
			placeInfo.setContact(contact);
			placeInfo.setLocation(location);
			
			// when
			boolean isComplete = placeInfo.isComplete();
			
			// then
			assertThat(isComplete).isFalse();
		}
	}
	
	@Nested
	@DisplayName("소프트 삭제 테스트")
	class SoftDeleteTest {
		
		private PlaceInfo placeInfo;
		
		@BeforeEach
		void setUp() {
			placeInfo = PlaceInfo.builder()
					.userId("user123")
					.placeName("테스트 연습실")
					.build();
		}
		
		@Test
		@DisplayName("정상: 소프트 삭제")
		void softDelete() {
			// when
			placeInfo.softDelete("admin123");
			
			// then
			assertThat(placeInfo.isDeleted()).isTrue();
			assertThat(placeInfo.getDeletedAt()).isNotNull();
			assertThat(placeInfo.getDeletedBy()).isEqualTo("admin123");
			assertThat(placeInfo.getIsActive()).isFalse();
		}
		
		@Test
		@DisplayName("정상: 삭제 복구")
		void restore() {
			// given
			placeInfo.softDelete("admin123");
			
			// when
			placeInfo.restore();
			
			// then
			assertThat(placeInfo.isDeleted()).isFalse();
			assertThat(placeInfo.getDeletedAt()).isNull();
			assertThat(placeInfo.getDeletedBy()).isNull();
			assertThat(placeInfo.getIsActive()).isTrue();
		}
		
		@Test
		@DisplayName("정상: 삭제되지 않은 상태 확인")
		void isDeleted_WhenNotDeleted() {
			// when
			boolean deleted = placeInfo.isDeleted();
			
			// then
			assertThat(deleted).isFalse();
		}
	}
	
	@Nested
	@DisplayName("업체 정보 변경 테스트")
	class UpdatePlaceInfoTest {
		
		private PlaceInfo placeInfo;
		
		@BeforeEach
		void setUp() {
			placeInfo = PlaceInfo.builder()
					.userId("user123")
					.placeName("테스트 연습실")
					.description("조용한 연습실")
					.category("연습실")
					.placeType("음악")
					.build();
		}
		
		@Test
		@DisplayName("정상: 업체명 변경")
		void updatePlaceName() {
			// when
			placeInfo.updatePlaceName("새로운 연습실");
			
			// then
			assertThat(placeInfo.getPlaceName()).isEqualTo("새로운 연습실");
		}
		
		@Test
		@DisplayName("예외: 업체명을 null로 변경")
		void updatePlaceName_WithNull() {
			// when & then
			assertThatThrownBy(() -> placeInfo.updatePlaceName(null))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("업체명은 필수입니다");
		}
		
		@Test
		@DisplayName("예외: 업체명을 공백으로 변경")
		void updatePlaceName_WithBlank() {
			// when & then
			assertThatThrownBy(() -> placeInfo.updatePlaceName("   "))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("업체명은 필수입니다");
		}
		
		@Test
		@DisplayName("예외: 업체명을 빈 문자열로 변경")
		void updatePlaceName_WithEmpty() {
			// when & then
			assertThatThrownBy(() -> placeInfo.updatePlaceName(""))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("업체명은 필수입니다");
		}
		
		@Test
		@DisplayName("정상: 소개글 변경")
		void updateDescription() {
			// when
			placeInfo.updateDescription("새로운 소개글입니다");
			
			// then
			assertThat(placeInfo.getDescription()).isEqualTo("새로운 소개글입니다");
		}
		
		@Test
		@DisplayName("엣지: 소개글을 null로 변경")
		void updateDescription_WithNull() {
			// when
			placeInfo.updateDescription(null);
			
			// then
			assertThat(placeInfo.getDescription()).isNull();
		}
		
		@Test
		@DisplayName("정상: 카테고리 변경")
		void updateCategory() {
			// when
			placeInfo.updateCategory("공연장");
			
			// then
			assertThat(placeInfo.getCategory()).isEqualTo("공연장");
		}
		
		@Test
		@DisplayName("엣지: 카테고리를 null로 변경")
		void updateCategory_WithNull() {
			// when
			placeInfo.updateCategory(null);
			
			// then
			assertThat(placeInfo.getCategory()).isNull();
		}
		
		@Test
		@DisplayName("정상: 업체 유형 변경")
		void updatePlaceType() {
			// when
			placeInfo.updatePlaceType("댄스");
			
			// then
			assertThat(placeInfo.getPlaceType()).isEqualTo("댄스");
		}
		
		@Test
		@DisplayName("엣지: 업체 유형을 null로 변경")
		void updatePlaceType_WithNull() {
			// when
			placeInfo.updatePlaceType(null);
			
			// then
			assertThat(placeInfo.getPlaceType()).isNull();
		}
	}
}
