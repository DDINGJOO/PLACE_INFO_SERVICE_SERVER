package com.teambind.placeinfoserver.place.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Keyword 엔티티 테스트")
class KeywordTest {
	
	@Nested
	@DisplayName("엔티티 생성 테스트")
	class CreateTest {
		
		@Test
		@DisplayName("정상: 빌더로 Keyword 생성")
		void createKeywordWithBuilder() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.description("그랜드 피아노가 구비된 공간")
					.displayOrder(1)
					.build();
			
			// then
			assertThat(keyword).isNotNull();
			assertThat(keyword.getName()).isEqualTo("그랜드 피아노");
			assertThat(keyword.getType()).isEqualTo(KeywordType.INSTRUMENT_EQUIPMENT);
			assertThat(keyword.getDescription()).isEqualTo("그랜드 피아노가 구비된 공간");
			assertThat(keyword.getDisplayOrder()).isEqualTo(1);
			assertThat(keyword.getIsActive()).isTrue();
		}
		
		@Test
		@DisplayName("정상: 기본값이 올바르게 설정됨 (활성화 상태)")
		void createKeywordWithDefaultValues() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("드럼 세트")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.build();
			
			// then
			assertThat(keyword.getIsActive()).isTrue();
		}
		
		@Test
		@DisplayName("정상: 최소한의 정보로 생성")
		void createKeywordWithMinimalInfo() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("주차 가능")
					.type(KeywordType.AMENITY)
					.build();
			
			// then
			assertThat(keyword).isNotNull();
			assertThat(keyword.getName()).isEqualTo("주차 가능");
			assertThat(keyword.getType()).isEqualTo(KeywordType.AMENITY);
		}
	}
	
	@Nested
	@DisplayName("KeywordType별 생성 테스트")
	class KeywordTypeTest {
		
		@Test
		@DisplayName("정상: SPACE_TYPE 키워드 생성")
		void createSpaceTypeKeyword() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("합주실")
					.type(KeywordType.SPACE_TYPE)
					.build();
			
			// then
			assertThat(keyword.getType()).isEqualTo(KeywordType.SPACE_TYPE);
			assertThat(keyword.getType().getDescription()).isEqualTo("공간 유형");
		}
		
		@Test
		@DisplayName("정상: INSTRUMENT_EQUIPMENT 키워드 생성")
		void createInstrumentEquipmentKeyword() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.build();
			
			// then
			assertThat(keyword.getType()).isEqualTo(KeywordType.INSTRUMENT_EQUIPMENT);
			assertThat(keyword.getType().getDescription()).isEqualTo("악기/장비");
		}
		
		@Test
		@DisplayName("정상: AMENITY 키워드 생성")
		void createAmenityKeyword() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("주차 가능")
					.type(KeywordType.AMENITY)
					.build();
			
			// then
			assertThat(keyword.getType()).isEqualTo(KeywordType.AMENITY);
			assertThat(keyword.getType().getDescription()).isEqualTo("편의시설");
		}
		
		@Test
		@DisplayName("정상: OTHER_FEATURE 키워드 생성")
		void createOtherFeatureKeyword() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("저렴한 가격")
					.type(KeywordType.OTHER_FEATURE)
					.build();
			
			// then
			assertThat(keyword.getType()).isEqualTo(KeywordType.OTHER_FEATURE);
			assertThat(keyword.getType().getDescription()).isEqualTo("기타 특성");
		}
	}
	
	@Nested
	@DisplayName("전체 이름 반환 테스트")
	class FullNameTest {
		
		@Test
		@DisplayName("정상: 공간 유형 키워드의 전체 이름")
		void getFullName_SpaceType() {
			// given
			Keyword keyword = Keyword.builder()
					.name("합주실")
					.type(KeywordType.SPACE_TYPE)
					.build();
			
			// when
			String fullName = keyword.getFullName();
			
			// then
			assertThat(fullName).isEqualTo("[공간 유형] 합주실");
		}
		
		@Test
		@DisplayName("정상: 악기/장비 키워드의 전체 이름")
		void getFullName_InstrumentEquipment() {
			// given
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.build();
			
			// when
			String fullName = keyword.getFullName();
			
			// then
			assertThat(fullName).isEqualTo("[악기/장비] 그랜드 피아노");
		}
		
		@Test
		@DisplayName("정상: 편의시설 키워드의 전체 이름")
		void getFullName_Amenity() {
			// given
			Keyword keyword = Keyword.builder()
					.name("주차 가능")
					.type(KeywordType.AMENITY)
					.build();
			
			// when
			String fullName = keyword.getFullName();
			
			// then
			assertThat(fullName).isEqualTo("[편의시설] 주차 가능");
		}
		
		@Test
		@DisplayName("정상: 기타 특성 키워드의 전체 이름")
		void getFullName_OtherFeature() {
			// given
			Keyword keyword = Keyword.builder()
					.name("저렴한 가격")
					.type(KeywordType.OTHER_FEATURE)
					.build();
			
			// when
			String fullName = keyword.getFullName();
			
			// then
			assertThat(fullName).isEqualTo("[기타 특성] 저렴한 가격");
		}
	}
	
	@Nested
	@DisplayName("활성화 상태 테스트")
	class ActiveStatusTest {
		
		@Test
		@DisplayName("정상: 활성화된 키워드 생성")
		void createActiveKeyword() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.isActive(true)
					.build();
			
			// then
			assertThat(keyword.getIsActive()).isTrue();
		}
		
		@Test
		@DisplayName("정상: 비활성화된 키워드 생성")
		void createInactiveKeyword() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.isActive(false)
					.build();
			
			// then
			assertThat(keyword.getIsActive()).isFalse();
		}
		
		@Test
		@DisplayName("정상: 활성화 상태 변경")
		void changeActiveStatus() {
			// given
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.isActive(true)
					.build();
			
			// when
			keyword.setIsActive(false);
			
			// then
			assertThat(keyword.getIsActive()).isFalse();
		}
	}
	
	@Nested
	@DisplayName("표시 순서 테스트")
	class DisplayOrderTest {
		
		@Test
		@DisplayName("정상: 표시 순서 설정")
		void setDisplayOrder() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.displayOrder(5)
					.build();
			
			// then
			assertThat(keyword.getDisplayOrder()).isEqualTo(5);
		}
		
		@Test
		@DisplayName("엣지: 표시 순서 0")
		void setDisplayOrderZero() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.displayOrder(0)
					.build();
			
			// then
			assertThat(keyword.getDisplayOrder()).isZero();
		}
		
		@Test
		@DisplayName("엣지: 표시 순서 음수")
		void setDisplayOrderNegative() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.displayOrder(-1)
					.build();
			
			// then
			assertThat(keyword.getDisplayOrder()).isEqualTo(-1);
		}
		
		@Test
		@DisplayName("정상: 여러 키워드의 표시 순서")
		void multipleKeywordsWithOrder() {
			// given & when
			Keyword keyword1 = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.displayOrder(1)
					.build();
			
			Keyword keyword2 = Keyword.builder()
					.name("드럼 세트")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.displayOrder(2)
					.build();
			
			Keyword keyword3 = Keyword.builder()
					.name("일렉기타 앰프")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.displayOrder(3)
					.build();
			
			// then
			assertThat(keyword1.getDisplayOrder()).isLessThan(keyword2.getDisplayOrder());
			assertThat(keyword2.getDisplayOrder()).isLessThan(keyword3.getDisplayOrder());
		}
	}
	
	@Nested
	@DisplayName("설명 테스트")
	class DescriptionTest {
		
		@Test
		@DisplayName("정상: 설명 설정")
		void setDescription() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.description("고급 그랜드 피아노가 구비되어 있습니다")
					.build();
			
			// then
			assertThat(keyword.getDescription()).isEqualTo("고급 그랜드 피아노가 구비되어 있습니다");
		}
		
		@Test
		@DisplayName("엣지: 설명 없이 생성")
		void createWithoutDescription() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.build();
			
			// then
			assertThat(keyword.getDescription()).isNull();
		}
		
		@Test
		@DisplayName("엣지: 빈 문자열 설명")
		void setEmptyDescription() {
			// given & when
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.description("")
					.build();
			
			// then
			assertThat(keyword.getDescription()).isEmpty();
		}
		
		@Test
		@DisplayName("엣지: 긴 설명 (200자)")
		void setLongDescription() {
			// given
			String longDescription = "가".repeat(200);
			
			// when
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.description(longDescription)
					.build();
			
			// then
			assertThat(keyword.getDescription()).hasSize(200);
			assertThat(keyword.getDescription()).isEqualTo(longDescription);
		}
	}
	
	@Nested
	@DisplayName("미리 정의된 키워드 테스트")
	class PredefinedKeywordsTest {
		
		@Test
		@DisplayName("정상: 공간 유형 미리 정의된 키워드 값 확인")
		void predefinedSpaceTypeKeywords() {
			// then
			assertThat(Keyword.PredefinedKeywords.ENSEMBLE_ROOM).isEqualTo("합주실");
			assertThat(Keyword.PredefinedKeywords.PRACTICE_ROOM).isEqualTo("연습실");
			assertThat(Keyword.PredefinedKeywords.LESSON_ROOM).isEqualTo("레슨실");
			assertThat(Keyword.PredefinedKeywords.RECORDING_ROOM).isEqualTo("녹음실");
			assertThat(Keyword.PredefinedKeywords.PERFORMANCE_PRACTICE_ROOM).isEqualTo("공연연습실");
			assertThat(Keyword.PredefinedKeywords.BUSKING_PREP_SPACE).isEqualTo("버스킹 준비 공간");
		}
		
		@Test
		@DisplayName("정상: 악기/장비 미리 정의된 키워드 값 확인")
		void predefinedInstrumentKeywords() {
			// then
			assertThat(Keyword.PredefinedKeywords.GRAND_PIANO).isEqualTo("그랜드 피아노");
			assertThat(Keyword.PredefinedKeywords.UPRIGHT_PIANO).isEqualTo("업라이트 피아노");
			assertThat(Keyword.PredefinedKeywords.DRUM_SET).isEqualTo("드럼 세트");
			assertThat(Keyword.PredefinedKeywords.ELECTRIC_GUITAR_AMP).isEqualTo("일렉기타 앰프");
			assertThat(Keyword.PredefinedKeywords.BASS_AMP).isEqualTo("베이스 앰프");
			assertThat(Keyword.PredefinedKeywords.PA_SYSTEM).isEqualTo("PA 시스템");
			assertThat(Keyword.PredefinedKeywords.AUDIO_INTERFACE).isEqualTo("오디오 인터페이스");
			assertThat(Keyword.PredefinedKeywords.VOCAL_MIC).isEqualTo("보컬 마이크");
		}
		
		@Test
		@DisplayName("정상: 편의시설 미리 정의된 키워드 값 확인")
		void predefinedAmenityKeywords() {
			// then
			assertThat(Keyword.PredefinedKeywords.PARKING_AVAILABLE).isEqualTo("주차 가능");
			assertThat(Keyword.PredefinedKeywords.RESTROOM_AVAILABLE).isEqualTo("화장실 있음");
			assertThat(Keyword.PredefinedKeywords.AIR_CONDITIONING).isEqualTo("냉난방 완비");
			assertThat(Keyword.PredefinedKeywords.SOUNDPROOF).isEqualTo("방음 시설");
			assertThat(Keyword.PredefinedKeywords.LOUNGE_AREA).isEqualTo("휴게 공간");
			assertThat(Keyword.PredefinedKeywords.WIFI_PROVIDED).isEqualTo("와이파이 제공");
		}
		
		@Test
		@DisplayName("정상: 기타 특성 미리 정의된 키워드 값 확인")
		void predefinedOtherFeatureKeywords() {
			// then
			assertThat(Keyword.PredefinedKeywords.AFFORDABLE_PRICE).isEqualTo("저렴한 가격");
			assertThat(Keyword.PredefinedKeywords.PRIVATE_SPACE).isEqualTo("프라이빗 공간");
			assertThat(Keyword.PredefinedKeywords.CONVENIENT_TRANSPORT).isEqualTo("교통 편리");
			assertThat(Keyword.PredefinedKeywords.NEW_FACILITY).isEqualTo("신축 시설");
			assertThat(Keyword.PredefinedKeywords.SPACIOUS).isEqualTo("넓은 공간");
			assertThat(Keyword.PredefinedKeywords.CLEAN_INTERIOR).isEqualTo("깔끔한 인테리어");
		}
	}
	
	@Nested
	@DisplayName("PlaceInfo와의 연관관계 테스트")
	class RelationshipWithPlaceInfoTest {
		
		@Test
		@DisplayName("정상: PlaceInfo에 키워드 추가")
		void addKeywordToPlaceInfo() {
			// given
			PlaceInfo placeInfo = PlaceInfo.builder()
					.userId("user123")
					.placeName("테스트 연습실")
					.build();
			
			Keyword keyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.build();
			
			// when
			placeInfo.addKeyword(keyword);
			
			// then
			assertThat(placeInfo.getKeywords()).contains(keyword);
		}
		
		@Test
		@DisplayName("정상: 여러 타입의 키워드를 PlaceInfo에 추가")
		void addMultipleTypesOfKeywords() {
			// given
			PlaceInfo placeInfo = PlaceInfo.builder()
					.userId("user123")
					.placeName("테스트 연습실")
					.build();
			
			Keyword spaceKeyword = Keyword.builder()
					.name("합주실")
					.type(KeywordType.SPACE_TYPE)
					.build();
			
			Keyword instrumentKeyword = Keyword.builder()
					.name("그랜드 피아노")
					.type(KeywordType.INSTRUMENT_EQUIPMENT)
					.build();
			
			Keyword amenityKeyword = Keyword.builder()
					.name("주차 가능")
					.type(KeywordType.AMENITY)
					.build();
			
			// when
			placeInfo.addKeyword(spaceKeyword);
			placeInfo.addKeyword(instrumentKeyword);
			placeInfo.addKeyword(amenityKeyword);
			
			// then
			assertThat(placeInfo.getKeywords()).hasSize(3);
			assertThat(placeInfo.getKeywords()).containsExactlyInAnyOrder(
					spaceKeyword, instrumentKeyword, amenityKeyword
			);
		}
	}
}
