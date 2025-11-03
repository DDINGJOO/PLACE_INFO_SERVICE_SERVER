package com.teambind.placeinfoserver.place.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.placeinfoserver.place.common.util.AddressParser;
import com.teambind.placeinfoserver.place.domain.entity.*;
import com.teambind.placeinfoserver.place.domain.enums.ParkingType;
import com.teambind.placeinfoserver.place.domain.factory.PlaceContactFactory;
import com.teambind.placeinfoserver.place.domain.factory.PlaceLocationFactory;
import com.teambind.placeinfoserver.place.domain.factory.PlaceParkingFactory;
import com.teambind.placeinfoserver.place.domain.vo.Address;
import com.teambind.placeinfoserver.place.dto.request.*;
import com.teambind.placeinfoserver.place.dto.response.*;
import com.teambind.placeinfoserver.place.fixture.PlaceRequestFactory;
import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PlaceMapper 단위 테스트
 * DTO <-> Entity 변환 로직 검증
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaceMapperTest {

	private PlaceMapper mapper;
	private AddressParser addressParser;
	private PlaceContactFactory contactFactory;
	private PlaceLocationFactory locationFactory;
	private PlaceParkingFactory parkingFactory;

	@BeforeEach
	void setUp() {
		addressParser = new AddressParser(new ObjectMapper());
		contactFactory = new PlaceContactFactory();
		locationFactory = new PlaceLocationFactory();
		parkingFactory = new PlaceParkingFactory();
		mapper = new PlaceMapper(addressParser, contactFactory, locationFactory, parkingFactory);
		PlaceTestFactory.resetSequence();
	}
	
	@Nested
	@DisplayName("Entity -> Response DTO 변환 테스트")
	class EntityToResponseTest {
		
		@Test
		@Order(1)
		@DisplayName("PlaceInfo -> PlaceInfoResponse 변환")
		void toResponse_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			
			// When
			PlaceInfoResponse response = mapper.toResponse(placeInfo);
			
			// Then
			assertThat(response).isNotNull();
			assertThat(response.getId()).isEqualTo(String.valueOf(placeInfo.getId()));
			assertThat(response.getPlaceName()).isEqualTo(placeInfo.getPlaceName());
			assertThat(response.getDescription()).isEqualTo(placeInfo.getDescription());
			assertThat(response.getCategory()).isEqualTo(placeInfo.getCategory());
			assertThat(response.getPlaceType()).isEqualTo(placeInfo.getPlaceType());
			assertThat(response.getIsActive()).isEqualTo(placeInfo.getIsActive());
			assertThat(response.getApprovalStatus()).isEqualTo(placeInfo.getApprovalStatus());
		}
		
		@Test
		@Order(2)
		@DisplayName("null PlaceInfo -> null 반환")
		void toResponse_Null_ReturnsNull() {
			// When
			PlaceInfoResponse response = mapper.toResponse(null);
			
			// Then
			assertThat(response).isNull();
		}
		
		@Test
		@Order(3)
		@DisplayName("PlaceInfo -> PlaceInfoSummaryResponse 변환")
		void toSummaryResponse_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			placeInfo.addImage(PlaceTestFactory.createPlaceImage(placeInfo, 1));
			
			// When
			PlaceInfoSummaryResponse response = mapper.toSummaryResponse(placeInfo);
			
			// Then
			assertThat(response).isNotNull();
			assertThat(response.getId()).isEqualTo(String.valueOf(placeInfo.getId()));
			assertThat(response.getPlaceName()).isEqualTo(placeInfo.getPlaceName());
			assertThat(response.getThumbnailUrl()).isNotNull();
			assertThat(response.getShortAddress()).isNotNull();
		}
		
		@Test
		@Order(4)
		@DisplayName("PlaceContact -> PlaceContactResponse 변환")
		void toContactResponse_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = placeInfo.getContact();
			
			// When
			PlaceContactResponse response = mapper.toContactResponse(contact);
			
			// Then
			assertThat(response).isNotNull();
			assertThat(response.getContact()).isEqualTo(contact.getContact());
		}
		
		@Test
		@Order(5)
		@DisplayName("PlaceLocation -> PlaceLocationResponse 변환")
		void toLocationResponse_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceLocation location = placeInfo.getLocation();
			
			// When
			PlaceLocationResponse response = mapper.toLocationResponse(location);
			
			// Then
			assertThat(response).isNotNull();
			assertThat(response.getLatitude()).isEqualTo(location.getLatitude());
			assertThat(response.getLongitude()).isEqualTo(location.getLongitude());
			assertThat(response.getAddress()).isNotNull();
		}
		
		@Test
		@Order(6)
		@DisplayName("PlaceParking -> PlaceParkingResponse 변환")
		void toParkingResponse_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParking parking = placeInfo.getParking();
			
			// When
			PlaceParkingResponse response = mapper.toParkingResponse(parking);
			
			// Then
			assertThat(response).isNotNull();
			assertThat(response.getAvailable()).isEqualTo(parking.getAvailable());
			assertThat(response.getParkingType()).isEqualTo(parking.getParkingType());
		}
		
		@Test
		@Order(7)
		@DisplayName("Keyword -> KeywordResponse 변환")
		void toKeywordResponse_Success() {
			// Given
			Keyword keyword = PlaceTestFactory.createKeyword("드럼");
			
			// When
			KeywordResponse response = mapper.toKeywordResponse(keyword);
			
			// Then
			assertThat(response).isNotNull();
			assertThat(response.getName()).isEqualTo(keyword.getName());
			assertThat(response.getType()).isEqualTo(keyword.getType());
		}
		
		@Test
		@Order(8)
		@DisplayName("Address -> AddressResponse 변환")
		void toAddressResponse_Success() {
			// Given
			Address address = Address.builder()
					.province("서울특별시")
					.city("강남구")
					.district("역삼동")
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.postalCode("06234")
					.build();
			
			// When
			AddressResponse response = mapper.toAddressResponse(address);
			
			// Then
			assertThat(response).isNotNull();
			assertThat(response.getProvince()).isEqualTo(address.getProvince());
			assertThat(response.getCity()).isEqualTo(address.getCity());
			assertThat(response.getDistrict()).isEqualTo(address.getDistrict());
		}
	}
	
	@Nested
	@DisplayName("Request DTO -> Entity 변환 테스트")
	class RequestToEntityTest {
		
		@Test
		@Order(9)
		@DisplayName("PlaceRegisterRequest -> PlaceInfo 변환")
		void toEntity_Success() {
			// Given
			PlaceRegisterRequest request = PlaceRequestFactory.createPlaceRegisterRequest();
			Long generatedId = 123456789L;  // Long 타입 ID
			
			// When
			PlaceInfo placeInfo = mapper.toEntity(request, generatedId);
			
			// Then
			assertThat(placeInfo).isNotNull();
			assertThat(placeInfo.getId()).isEqualTo(generatedId);
			assertThat(placeInfo.getPlaceName()).isEqualTo(request.getPlaceName());
			assertThat(placeInfo.getDescription()).isEqualTo(request.getDescription());
			assertThat(placeInfo.getCategory()).isEqualTo(request.getCategory());
			assertThat(placeInfo.getPlaceType()).isEqualTo(request.getPlaceType());
			assertThat(placeInfo.getContact()).isNotNull();
			assertThat(placeInfo.getLocation()).isNotNull();
			assertThat(placeInfo.getParking()).isNotNull();
		}
		
		@Test
		@Order(10)
		@DisplayName("PlaceContactRequest -> PlaceContact 변환")
		void toContactEntity_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContactRequest request = PlaceContactRequest.builder()
					.contact("02-1234-5678")
					.email("test@example.com")
					.build();
			
			// When
			PlaceContact contact = mapper.toContactEntity(request, placeInfo);
			
			// Then
			assertThat(contact).isNotNull();
			assertThat(contact.getContact()).isEqualTo(request.getContact());
			assertThat(contact.getEmail()).isEqualTo(request.getEmail());
			assertThat(contact.getPlaceInfo()).isEqualTo(placeInfo);
		}
		
		@Test
		@Order(11)
		@DisplayName("PlaceLocationRequest -> PlaceLocation 변환")
		void toLocationEntity_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			AddressRequest addressData = AddressRequest.builder()
					.province("서울특별시")
					.city("강남구")
					.district("역삼동")
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.postalCode("06234")
					.build();
			
			PlaceLocationRequest request = PlaceLocationRequest.builder()
					.from(com.teambind.placeinfoserver.place.domain.enums.AddressSource.MANUAL)
					.addressData(addressData)
					.latitude(37.4979)
					.longitude(127.0276)
					.locationGuide("지하철 2호선 역삼역 3번 출구")
					.build();
			
			// When
			PlaceLocation location = mapper.toLocationEntity(request, placeInfo);
			
			// Then
			assertThat(location).isNotNull();
			assertThat(location.getLatitude()).isEqualTo(request.getLatitude());
			assertThat(location.getLongitude()).isEqualTo(request.getLongitude());
			assertThat(location.getLocationGuide()).isEqualTo(request.getLocationGuide());
			assertThat(location.getPlaceInfo()).isEqualTo(placeInfo);
		}
		
		@Test
		@Order(12)
		@DisplayName("AddressRequest -> Address 변환")
		void toAddressEntity_Success() {
			// Given
			AddressRequest request = AddressRequest.builder()
					.province("서울특별시")
					.city("강남구")
					.district("역삼동")
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.addressDetail("테스트빌딩 5층")
					.postalCode("06234")
					.build();
			
			// When
			Address address = mapper.toAddressEntity(request);
			
			// Then
			assertThat(address).isNotNull();
			assertThat(address.getProvince()).isEqualTo(request.getProvince());
			assertThat(address.getCity()).isEqualTo(request.getCity());
			assertThat(address.getDistrict()).isEqualTo(request.getDistrict());
			assertThat(address.getFullAddress()).isEqualTo(request.getFullAddress());
			assertThat(address.getAddressDetail()).isEqualTo(request.getAddressDetail());
		}
		
		@Test
		@Order(13)
		@DisplayName("PlaceParkingUpdateRequest -> PlaceParking 변환")
		void toParkingEntity_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceParkingUpdateRequest request = PlaceParkingUpdateRequest.builder()
					.available(true)
					.parkingType(ParkingType.FREE)
					.description("건물 내 무료 주차 가능")
					.build();
			
			// When
			PlaceParking parking = mapper.toParkingEntity(request, placeInfo);
			
			// Then
			assertThat(parking).isNotNull();
			assertThat(parking.getAvailable()).isEqualTo(request.getAvailable());
			assertThat(parking.getParkingType()).isEqualTo(request.getParkingType());
			assertThat(parking.getDescription()).isEqualTo(request.getDescription());
			assertThat(parking.getPlaceInfo()).isEqualTo(placeInfo);
		}
	}
	
	@Nested
	@DisplayName("List 변환 테스트")
	class ListConversionTest {
		
		@Test
		@Order(14)
		@DisplayName("List<PlaceInfo> -> List<PlaceInfoResponse> 변환")
		void toResponseList_Success() {
			// Given
			PlaceInfo place1 = PlaceTestFactory.createPlaceInfo();
			PlaceInfo place2 = PlaceTestFactory.createPlaceInfo();
			List<PlaceInfo> places = List.of(place1, place2);
			
			// When
			List<PlaceInfoResponse> responses = mapper.toResponseList(places);
			
			// Then
			assertThat(responses).hasSize(2);
			assertThat(responses.get(0).getId()).isEqualTo(String.valueOf(place1.getId()));
			assertThat(responses.get(1).getId()).isEqualTo(String.valueOf(place2.getId()));
		}
		
		@Test
		@Order(15)
		@DisplayName("null 리스트 -> 빈 리스트 반환")
		void toResponseList_Null_ReturnsEmptyList() {
			// When
			List<PlaceInfoResponse> responses = mapper.toResponseList(null);
			
			// Then
			assertThat(responses).isEmpty();
		}
		
		@Test
		@Order(16)
		@DisplayName("List<PlaceInfo> -> List<PlaceInfoSummaryResponse> 변환")
		void toSummaryResponseList_Success() {
			// Given
			PlaceInfo place1 = PlaceTestFactory.createPlaceInfo();
			PlaceInfo place2 = PlaceTestFactory.createPlaceInfo();
			List<PlaceInfo> places = List.of(place1, place2);
			
			// When
			List<PlaceInfoSummaryResponse> responses = mapper.toSummaryResponseList(places);
			
			// Then
			assertThat(responses).hasSize(2);
			assertThat(responses.get(0).getId()).isEqualTo(String.valueOf(place1.getId()));
			assertThat(responses.get(1).getId()).isEqualTo(String.valueOf(place2.getId()));
		}
	}
	
	@Nested
	@DisplayName("Update 메서드 테스트")
	class UpdateMethodTest {
		
		@Test
		@Order(17)
		@DisplayName("PlaceInfo 업데이트 - 기본 정보")
		void updateEntity_BasicInfo_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceUpdateRequest request = PlaceUpdateRequest.builder()
					.placeName("변경된 이름")
					.description("변경된 설명")
					.category("변경된 카테고리")
					.placeType("변경된 타입")
					.build();
			
			// When
			mapper.updateEntity(placeInfo, request);
			
			// Then
			assertThat(placeInfo.getPlaceName()).isEqualTo("변경된 이름");
			assertThat(placeInfo.getDescription()).isEqualTo("변경된 설명");
			assertThat(placeInfo.getCategory()).isEqualTo("변경된 카테고리");
			assertThat(placeInfo.getPlaceType()).isEqualTo("변경된 타입");
		}
		
		@Test
		@Order(18)
		@DisplayName("null request로 업데이트 - 아무 변화 없음")
		void updateEntity_NullRequest_NoChange() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			String originalName = placeInfo.getPlaceName();
			
			// When
			mapper.updateEntity(placeInfo, null);
			
			// Then
			assertThat(placeInfo.getPlaceName()).isEqualTo(originalName);
		}
	}
	
	@Nested
	@DisplayName("null 처리 테스트")
	class NullHandlingTest {
		
		@Test
		@Order(19)
		@DisplayName("null PlaceContact -> null 반환")
		void toContactResponse_Null_ReturnsNull() {
			assertThat(mapper.toContactResponse(null)).isNull();
		}
		
		@Test
		@Order(20)
		@DisplayName("null PlaceLocation -> null 반환")
		void toLocationResponse_Null_ReturnsNull() {
			assertThat(mapper.toLocationResponse(null)).isNull();
		}
		
		@Test
		@Order(21)
		@DisplayName("null PlaceParking -> null 반환")
		void toParkingResponse_Null_ReturnsNull() {
			assertThat(mapper.toParkingResponse(null)).isNull();
		}
		
		@Test
		@Order(22)
		@DisplayName("null Keyword -> null 반환")
		void toKeywordResponse_Null_ReturnsNull() {
			assertThat(mapper.toKeywordResponse(null)).isNull();
		}
		
		@Test
		@Order(23)
		@DisplayName("null Address -> null 반환")
		void toAddressResponse_Null_ReturnsNull() {
			assertThat(mapper.toAddressResponse(null)).isNull();
		}
		
		@Test
		@Order(24)
		@DisplayName("null PlaceContactRequest -> null 반환")
		void toContactEntity_Null_ReturnsNull() {
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			assertThat(mapper.toContactEntity(null, placeInfo)).isNull();
		}
		
		@Test
		@Order(25)
		@DisplayName("null PlaceLocationRequest -> null 반환")
		void toLocationEntity_Null_ReturnsNull() {
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			assertThat(mapper.toLocationEntity(null, placeInfo)).isNull();
		}
		
		@Test
		@Order(26)
		@DisplayName("null PlaceParkingUpdateRequest -> null 반환")
		void toParkingEntity_Null_ReturnsNull() {
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			assertThat(mapper.toParkingEntity(null, placeInfo)).isNull();
		}
		
		@Test
		@Order(27)
		@DisplayName("null AddressRequest -> null 반환")
		void toAddressEntity_Null_ReturnsNull() {
			assertThat(mapper.toAddressEntity(null)).isNull();
		}
	}
	
	@Nested
	@DisplayName("복잡한 객체 변환 테스트")
	class ComplexObjectConversionTest {
		
		@Test
		@Order(28)
		@DisplayName("이미지가 있는 PlaceInfo -> Response 변환")
		void toResponse_WithImages_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			placeInfo.addImage(PlaceTestFactory.createPlaceImage(placeInfo, 1));
			placeInfo.addImage(PlaceTestFactory.createPlaceImage(placeInfo, 2));
			
			// When
			PlaceInfoResponse response = mapper.toResponse(placeInfo);
			
			// Then
			assertThat(response.getImageUrls()).hasSize(2);
		}
		
		@Test
		@Order(29)
		@DisplayName("키워드가 있는 PlaceInfo -> Response 변환")
		void toResponse_WithKeywords_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			placeInfo.addKeyword(PlaceTestFactory.createKeyword("드럼"));
			placeInfo.addKeyword(PlaceTestFactory.createKeyword("기타"));
			
			// When
			PlaceInfoResponse response = mapper.toResponse(placeInfo);
			
			// Then
			assertThat(response.getKeywords()).hasSizeGreaterThanOrEqualTo(2);
		}
		
		@Test
		@Order(30)
		@DisplayName("모든 정보가 있는 PlaceInfo -> Response 변환")
		void toResponse_Complete_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			placeInfo.addImage(PlaceTestFactory.createPlaceImage(placeInfo, 1));
			placeInfo.addKeyword(PlaceTestFactory.createKeyword("드럼"));
			
			// When
			PlaceInfoResponse response = mapper.toResponse(placeInfo);
			
			// Then
			assertThat(response).isNotNull();
			assertThat(response.getId()).isNotNull();
			assertThat(response.getContact()).isNotNull();
			assertThat(response.getLocation()).isNotNull();
			assertThat(response.getParking()).isNotNull();
			assertThat(response.getImageUrls()).isNotEmpty();
			assertThat(response.getKeywords()).isNotEmpty();
		}
	}
}
