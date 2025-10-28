package com.teambind.placeinfoserver.place.domain.entity;

import com.teambind.placeinfoserver.place.domain.enums.ParkingType;
import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PlaceParking 엔티티 단위 테스트
 * 업체 주차 정보 도메인 로직 검증
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaceParkingTest {

	@Nested
	@DisplayName("엔티티 생성 테스트")
	class CreationTest {

		@Test
		@Order(1)
		@DisplayName("PlaceParking 생성 - 주차 가능")
		void create_Available_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.FREE)
					.description("건물 내 무료 주차 가능")
					.build();

			// Then
			assertThat(parking).isNotNull();
			assertThat(parking.getAvailable()).isTrue();
			assertThat(parking.getParkingType()).isEqualTo(ParkingType.FREE);
			assertThat(parking.getDescription()).isEqualTo("건물 내 무료 주차 가능");
		}

		@Test
		@Order(2)
		@DisplayName("PlaceParking 생성 - 주차 불가")
		void create_NotAvailable_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(false)
					.build();

			// Then
			assertThat(parking).isNotNull();
			assertThat(parking.getAvailable()).isFalse();
			assertThat(parking.getParkingType()).isNull();
		}

		@Test
		@Order(3)
		@DisplayName("Builder 기본값 확인 - available은 false")
		void create_DefaultValues_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.build();

			// Then
			assertThat(parking.getAvailable()).isFalse();
		}
	}

	@Nested
	@DisplayName("주차 타입 테스트")
	class ParkingTypeTest {

		@Test
		@Order(4)
		@DisplayName("무료 주차 - 성공")
		void setParkingType_Free_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.FREE)
					.description("무료 주차 2시간")
					.build();

			// Then
			assertThat(parking.getParkingType()).isEqualTo(ParkingType.FREE);
			assertThat(parking.isFreeParking()).isTrue();
		}

		@Test
		@Order(5)
		@DisplayName("유료 주차 - 성공")
		void setParkingType_Paid_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.PAID)
					.description("유료 주차 - 시간당 3,000원")
					.build();

			// Then
			assertThat(parking.getParkingType()).isEqualTo(ParkingType.PAID);
			assertThat(parking.isFreeParking()).isFalse();
		}
	}

	@Nested
	@DisplayName("주차 가능 설정 메서드 테스트")
	class EnableParkingTest {

		@Test
		@Order(6)
		@DisplayName("주차 가능으로 설정 - 무료")
		void enableParking_Free_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(false)
					.build();

			// When
			parking.enableParking(ParkingType.FREE);

			// Then
			assertThat(parking.getAvailable()).isTrue();
			assertThat(parking.getParkingType()).isEqualTo(ParkingType.FREE);
		}

		@Test
		@Order(7)
		@DisplayName("주차 가능으로 설정 - 유료")
		void enableParking_Paid_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(false)
					.build();

			// When
			parking.enableParking(ParkingType.PAID);

			// Then
			assertThat(parking.getAvailable()).isTrue();
			assertThat(parking.getParkingType()).isEqualTo(ParkingType.PAID);
		}

		@Test
		@Order(8)
		@DisplayName("이미 가능한 주차를 다시 설정 - 타입 변경")
		void enableParking_ChangeType_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.FREE)
					.build();

			// When
			parking.enableParking(ParkingType.PAID);

			// Then
			assertThat(parking.getAvailable()).isTrue();
			assertThat(parking.getParkingType()).isEqualTo(ParkingType.PAID);
		}
	}

	@Nested
	@DisplayName("주차 불가 설정 메서드 테스트")
	class DisableParkingTest {

		@Test
		@Order(9)
		@DisplayName("주차 불가로 설정 - 성공")
		void disableParking_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.FREE)
					.build();

			// When
			parking.disableParking();

			// Then
			assertThat(parking.getAvailable()).isFalse();
			assertThat(parking.getParkingType()).isNull();
		}

		@Test
		@Order(10)
		@DisplayName("이미 불가능한 주차를 다시 설정 - 멱등성")
		void disableParking_Idempotent_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(false)
					.build();

			// When
			parking.disableParking();

			// Then
			assertThat(parking.getAvailable()).isFalse();
			assertThat(parking.getParkingType()).isNull();
		}
	}

	@Nested
	@DisplayName("무료 주차 확인 메서드 테스트")
	class IsFreeTest {

		@Test
		@Order(11)
		@DisplayName("무료 주차 확인 - true")
		void isFreeParking_True() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.FREE)
					.build();

			// When
			boolean isFree = parking.isFreeParking();

			// Then
			assertThat(isFree).isTrue();
		}

		@Test
		@Order(12)
		@DisplayName("유료 주차 확인 - false")
		void isFreeParking_Paid_False() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.PAID)
					.build();

			// When
			boolean isFree = parking.isFreeParking();

			// Then
			assertThat(isFree).isFalse();
		}

		@Test
		@Order(13)
		@DisplayName("주차 불가 - false")
		void isFreeParking_NotAvailable_False() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(false)
					.build();

			// When
			boolean isFree = parking.isFreeParking();

			// Then
			assertThat(isFree).isFalse();
		}
	}

	@Nested
	@DisplayName("주차 설명 테스트")
	class DescriptionTest {

		@Test
		@Order(14)
		@DisplayName("상세 설명 설정 - 성공")
		void setDescription_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.FREE)
					.build();

			// When
			parking.setDescription("건물 지하 1층, 2시간 무료 주차 가능");

			// Then
			assertThat(parking.getDescription()).isEqualTo("건물 지하 1층, 2시간 무료 주차 가능");
		}

		@Test
		@Order(15)
		@DisplayName("긴 설명 설정 - 성공")
		void setDescription_Long_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.PAID)
					.build();

			String longDescription = "건물 지하 1층과 2층에 주차 가능합니다. " +
					"주차 요금은 최초 30분 무료, 이후 10분당 1,000원입니다. " +
					"1일 최대 요금은 20,000원입니다. " +
					"건물 방문객은 3시간 무료 주차권을 제공받을 수 있습니다.";

			// When
			parking.setDescription(longDescription);

			// Then
			assertThat(parking.getDescription()).hasSize(longDescription.length());
			assertThat(parking.getDescription()).contains("주차 요금");
		}
	}

	@Nested
	@DisplayName("연관관계 테스트")
	class RelationshipTest {

		@Test
		@Order(16)
		@DisplayName("PlaceInfo와의 연관관계 - 일대일")
		void relationship_WithPlaceInfo_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.FREE)
					.build();

			// Then
			assertThat(parking.getPlaceInfo()).isEqualTo(placeInfo);
			assertThat(placeInfo.getParking()).isNotNull();
		}
	}

	@Nested
	@DisplayName("Setter 메서드 테스트")
	class SetterTest {

		@Test
		@Order(17)
		@DisplayName("주차 가능 여부 변경 - 성공")
		void setAvailable_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(false)
					.build();

			// When
			parking.setAvailable(true);

			// Then
			assertThat(parking.getAvailable()).isTrue();
		}

		@Test
		@Order(18)
		@DisplayName("주차 타입 변경 - 성공")
		void setParkingType_Change_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.FREE)
					.build();

			// When
			parking.setParkingType(ParkingType.PAID);

			// Then
			assertThat(parking.getParkingType()).isEqualTo(ParkingType.PAID);
		}
	}

	@Nested
	@DisplayName("실제 데이터 시나리오 테스트")
	class RealDataScenarioTest {

		@Test
		@Order(19)
		@DisplayName("일반 건물 - 무료 주차 2시간")
		void scenario_FreeParking2Hours_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.FREE)
					.description("건물 방문객 2시간 무료 주차 가능, 이후 30분당 2,000원")
					.build();

			// Then
			assertThat(parking.getAvailable()).isTrue();
			assertThat(parking.isFreeParking()).isTrue();
			assertThat(parking.getDescription()).contains("2시간");
		}

		@Test
		@Order(20)
		@DisplayName("백화점 - 구매 금액에 따른 무료 주차")
		void scenario_ShoppingMallParking_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.FREE)
					.description("3만원 이상 구매 시 2시간 무료, 5만원 이상 구매 시 3시간 무료")
					.build();

			// Then
			assertThat(parking.getDescription()).contains("구매");
			assertThat(parking.isFreeParking()).isTrue();
		}

		@Test
		@Order(21)
		@DisplayName("노상 주차장 - 유료")
		void scenario_StreetParking_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.PAID)
					.description("노상 공영주차장, 10분당 500원")
					.build();

			// Then
			assertThat(parking.getParkingType()).isEqualTo(ParkingType.PAID);
			assertThat(parking.isFreeParking()).isFalse();
		}

		@Test
		@Order(22)
		@DisplayName("주차 불가 - 대중교통 이용 권장")
		void scenario_NoParking_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(false)
					.description("주차 공간 없음, 대중교통 이용을 권장합니다.")
					.build();

			// Then
			assertThat(parking.getAvailable()).isFalse();
			assertThat(parking.getDescription()).contains("대중교통");
		}
	}

	@Nested
	@DisplayName("비즈니스 로직 시나리오 테스트")
	class BusinessLogicScenarioTest {

		@Test
		@Order(23)
		@DisplayName("주차 정보 업데이트 시나리오")
		void scenario_UpdateParking_Success() {
			// Given - 초기에는 주차 불가
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(false)
					.build();

			// When - 주차 시설 마련 후 무료 주차로 변경
			parking.enableParking(ParkingType.FREE);
			parking.setDescription("신규 주차장 오픈! 방문객 무료 주차");

			// Then
			assertThat(parking.getAvailable()).isTrue();
			assertThat(parking.getParkingType()).isEqualTo(ParkingType.FREE);
		}

		@Test
		@Order(24)
		@DisplayName("무료에서 유료로 전환 시나리오")
		void scenario_FreeToPaid_Success() {
			// Given - 무료 주차
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.FREE)
					.description("무료 주차 가능")
					.build();

			// When - 유료로 전환
			parking.enableParking(ParkingType.PAID);
			parking.setDescription("유료 주차로 전환 - 30분당 1,500원");

			// Then
			assertThat(parking.getParkingType()).isEqualTo(ParkingType.PAID);
			assertThat(parking.isFreeParking()).isFalse();
		}

		@Test
		@Order(25)
		@DisplayName("주차장 폐쇄 시나리오")
		void scenario_CloseParking_Success() {
			// Given - 유료 주차 운영 중
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = PlaceParking.builder()
					.placeInfo(placeInfo)
					.available(true)
					.parkingType(ParkingType.PAID)
					.description("유료 주차 운영")
					.build();

			// When - 주차장 폐쇄
			parking.disableParking();
			parking.setDescription("주차장 공사로 인한 임시 폐쇄");

			// Then
			assertThat(parking.getAvailable()).isFalse();
			assertThat(parking.getParkingType()).isNull();
		}
	}

	@Nested
	@DisplayName("팩토리 메서드 테스트")
	class FactoryMethodTest {

		@Test
		@Order(26)
		@DisplayName("PlaceTestFactory로 생성 - 주차 가능")
		void createWithFactory_Available_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceParking parking = PlaceTestFactory.createPlaceParking(placeInfo);

			// Then
			assertThat(parking).isNotNull();
			assertThat(parking.getAvailable()).isTrue();
			assertThat(parking.getParkingType()).isEqualTo(ParkingType.FREE);
		}

		@Test
		@Order(27)
		@DisplayName("PlaceTestFactory로 생성 - 주차 불가")
		void createWithFactory_NotAvailable_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceParking parking = PlaceTestFactory.createNoParkingParking(placeInfo);

			// Then
			assertThat(parking).isNotNull();
			assertThat(parking.getAvailable()).isFalse();
		}
	}
}
