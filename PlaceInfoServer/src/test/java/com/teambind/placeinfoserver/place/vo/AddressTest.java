package com.teambind.placeinfoserver.place.vo;

import com.teambind.placeinfoserver.place.entity.vo.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Address Value Object 테스트")
class AddressTest {
	
	@Nested
	@DisplayName("Value Object 생성 테스트")
	class CreateTest {
		
		@Test
		@DisplayName("정상: 빌더로 Address 생성 (모든 필드)")
		void createAddressWithAllFields() {
			// given & when
			Address address = Address.builder()
					.province("서울특별시")
					.city("강남구")
					.district("역삼동")
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.addressDetail("테헤란빌딩 10층 1001호")
					.postalCode("06234")
					.build();
			
			// then
			assertThat(address).isNotNull();
			assertThat(address.getProvince()).isEqualTo("서울특별시");
			assertThat(address.getCity()).isEqualTo("강남구");
			assertThat(address.getDistrict()).isEqualTo("역삼동");
			assertThat(address.getFullAddress()).isEqualTo("서울특별시 강남구 역삼동 123-45");
			assertThat(address.getAddressDetail()).isEqualTo("테헤란빌딩 10층 1001호");
			assertThat(address.getPostalCode()).isEqualTo("06234");
		}
		
		@Test
		@DisplayName("정상: 필수 필드만으로 생성")
		void createAddressWithRequiredFieldsOnly() {
			// given & when
			Address address = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.build();
			
			// then
			assertThat(address).isNotNull();
			assertThat(address.getFullAddress()).isEqualTo("서울특별시 강남구 역삼동 123-45");
			assertThat(address.getProvince()).isNull();
			assertThat(address.getCity()).isNull();
			assertThat(address.getDistrict()).isNull();
			assertThat(address.getAddressDetail()).isNull();
			assertThat(address.getPostalCode()).isNull();
		}
		
		@Test
		@DisplayName("엣지: 모든 필드가 null인 Address 생성")
		void createAddressWithAllNullFields() {
			// given & when
			Address address = Address.builder().build();
			
			// then
			assertThat(address).isNotNull();
			assertThat(address.getProvince()).isNull();
			assertThat(address.getCity()).isNull();
			assertThat(address.getDistrict()).isNull();
			assertThat(address.getFullAddress()).isNull();
			assertThat(address.getAddressDetail()).isNull();
			assertThat(address.getPostalCode()).isNull();
		}
	}
	
	@Nested
	@DisplayName("짧은 주소 반환 테스트")
	class ShortAddressTest {
		
		@Test
		@DisplayName("정상: 도/시/구/군/동이 모두 있는 경우")
		void getShortAddress_WithAllFields() {
			// given
			Address address = Address.builder()
					.province("서울특별시")
					.city("강남구")
					.district("역삼동")
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.build();
			
			// when
			String shortAddress = address.getShortAddress();
			
			// then
			assertThat(shortAddress).isEqualTo("서울특별시 강남구 역삼동");
		}
		
		@Test
		@DisplayName("정상: 도/시만 있는 경우")
		void getShortAddress_WithProvinceAndCityOnly() {
			// given
			Address address = Address.builder()
					.province("서울특별시")
					.city("강남구")
					.fullAddress("서울특별시 강남구")
					.build();
			
			// when
			String shortAddress = address.getShortAddress();
			
			// then
			assertThat(shortAddress).isEqualTo("서울특별시 강남구");
		}
		
		@Test
		@DisplayName("정상: 도만 있는 경우")
		void getShortAddress_WithProvinceOnly() {
			// given
			Address address = Address.builder()
					.province("서울특별시")
					.fullAddress("서울특별시")
					.build();
			
			// when
			String shortAddress = address.getShortAddress();
			
			// then
			assertThat(shortAddress).isEqualTo("서울특별시");
		}
		
		@Test
		@DisplayName("엣지: 모든 필드가 null인 경우")
		void getShortAddress_WithAllNullFields() {
			// given
			Address address = Address.builder().build();
			
			// when
			String shortAddress = address.getShortAddress();
			
			// then
			assertThat(shortAddress).isEmpty();
		}
		
		@Test
		@DisplayName("엣지: 빈 문자열들만 있는 경우")
		void getShortAddress_WithEmptyStrings() {
			// given
			Address address = Address.builder()
					.province("")
					.city("")
					.district("")
					.fullAddress("주소")
					.build();
			
			// when
			String shortAddress = address.getShortAddress();
			
			// then
			assertThat(shortAddress).isEmpty();
		}
		
		@Test
		@DisplayName("엣지: 공백만 있는 경우")
		void getShortAddress_WithBlankStrings() {
			// given
			Address address = Address.builder()
					.province("   ")
					.city("   ")
					.district("   ")
					.fullAddress("주소")
					.build();
			
			// when
			String shortAddress = address.getShortAddress();
			
			// then
			assertThat(shortAddress).isEmpty();
		}
		
		@Test
		@DisplayName("정상: 일부 필드만 있는 경우 (도와 동만)")
		void getShortAddress_WithProvinceAndDistrictOnly() {
			// given
			Address address = Address.builder()
					.province("서울특별시")
					.district("역삼동")
					.fullAddress("서울특별시 역삼동")
					.build();
			
			// when
			String shortAddress = address.getShortAddress();
			
			// then
			assertThat(shortAddress).isEqualTo("서울특별시 역삼동");
		}
	}
	
	@Nested
	@DisplayName("상세 주소 포함 전체 주소 반환 테스트")
	class FullAddressWithDetailTest {
		
		@Test
		@DisplayName("정상: 상세 주소가 있는 경우")
		void getFullAddressWithDetail_WithDetail() {
			// given
			Address address = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.addressDetail("테헤란빌딩 10층 1001호")
					.build();
			
			// when
			String fullAddressWithDetail = address.getFullAddressWithDetail();
			
			// then
			assertThat(fullAddressWithDetail).isEqualTo("서울특별시 강남구 역삼동 123-45 테헤란빌딩 10층 1001호");
		}
		
		@Test
		@DisplayName("정상: 상세 주소가 없는 경우")
		void getFullAddressWithDetail_WithoutDetail() {
			// given
			Address address = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.build();
			
			// when
			String fullAddressWithDetail = address.getFullAddressWithDetail();
			
			// then
			assertThat(fullAddressWithDetail).isEqualTo("서울특별시 강남구 역삼동 123-45");
		}
		
		@Test
		@DisplayName("엣지: 상세 주소가 null인 경우")
		void getFullAddressWithDetail_WithNullDetail() {
			// given
			Address address = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.addressDetail(null)
					.build();
			
			// when
			String fullAddressWithDetail = address.getFullAddressWithDetail();
			
			// then
			assertThat(fullAddressWithDetail).isEqualTo("서울특별시 강남구 역삼동 123-45");
		}
		
		@Test
		@DisplayName("엣지: 상세 주소가 빈 문자열인 경우")
		void getFullAddressWithDetail_WithEmptyDetail() {
			// given
			Address address = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.addressDetail("")
					.build();
			
			// when
			String fullAddressWithDetail = address.getFullAddressWithDetail();
			
			// then
			assertThat(fullAddressWithDetail).isEqualTo("서울특별시 강남구 역삼동 123-45");
		}
		
		@Test
		@DisplayName("엣지: 상세 주소가 공백만 있는 경우")
		void getFullAddressWithDetail_WithBlankDetail() {
			// given
			Address address = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.addressDetail("   ")
					.build();
			
			// when
			String fullAddressWithDetail = address.getFullAddressWithDetail();
			
			// then
			assertThat(fullAddressWithDetail).isEqualTo("서울특별시 강남구 역삼동 123-45");
		}
	}
	
	@Nested
	@DisplayName("주소 유효성 검증 테스트")
	class ValidityTest {
		
		@Test
		@DisplayName("정상: 전체 주소가 있는 경우 유효함")
		void isValid_WithFullAddress() {
			// given
			Address address = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.build();
			
			// when
			boolean valid = address.isValid();
			
			// then
			assertThat(valid).isTrue();
		}
		
		@Test
		@DisplayName("정상: 전체 주소와 다른 필드들이 모두 있는 경우 유효함")
		void isValid_WithAllFields() {
			// given
			Address address = Address.builder()
					.province("서울특별시")
					.city("강남구")
					.district("역삼동")
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.addressDetail("테헤란빌딩 10층")
					.postalCode("06234")
					.build();
			
			// when
			boolean valid = address.isValid();
			
			// then
			assertThat(valid).isTrue();
		}
		
		@Test
		@DisplayName("엣지: 전체 주소가 null인 경우 유효하지 않음")
		void isValid_WithNullFullAddress() {
			// given
			Address address = Address.builder()
					.province("서울특별시")
					.city("강남구")
					.district("역삼동")
					.build();
			
			// when
			boolean valid = address.isValid();
			
			// then
			assertThat(valid).isFalse();
		}
		
		@Test
		@DisplayName("엣지: 전체 주소가 빈 문자열인 경우 유효하지 않음")
		void isValid_WithEmptyFullAddress() {
			// given
			Address address = Address.builder()
					.fullAddress("")
					.build();
			
			// when
			boolean valid = address.isValid();
			
			// then
			assertThat(valid).isFalse();
		}
		
		@Test
		@DisplayName("엣지: 전체 주소가 공백만 있는 경우 유효하지 않음")
		void isValid_WithBlankFullAddress() {
			// given
			Address address = Address.builder()
					.fullAddress("   ")
					.build();
			
			// when
			boolean valid = address.isValid();
			
			// then
			assertThat(valid).isFalse();
		}
		
		@Test
		@DisplayName("엣지: 모든 필드가 null인 경우 유효하지 않음")
		void isValid_WithAllNullFields() {
			// given
			Address address = Address.builder().build();
			
			// when
			boolean valid = address.isValid();
			
			// then
			assertThat(valid).isFalse();
		}
	}
	
	@Nested
	@DisplayName("Value Object 동등성 테스트")
	class EqualityTest {
		
		@Test
		@DisplayName("정상: 모든 필드가 같으면 동등함")
		void equals_WithSameValues() {
			// given
			Address address1 = Address.builder()
					.province("서울특별시")
					.city("강남구")
					.district("역삼동")
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.addressDetail("테헤란빌딩 10층")
					.postalCode("06234")
					.build();
			
			Address address2 = Address.builder()
					.province("서울특별시")
					.city("강남구")
					.district("역삼동")
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.addressDetail("테헤란빌딩 10층")
					.postalCode("06234")
					.build();
			
			// when & then
			assertThat(address1).isEqualTo(address2);
			assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
		}
		
		@Test
		@DisplayName("정상: 필드 값이 다르면 동등하지 않음")
		void equals_WithDifferentValues() {
			// given
			Address address1 = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.build();
			
			Address address2 = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 678-90")
					.build();
			
			// when & then
			assertThat(address1).isNotEqualTo(address2);
		}
		
		@Test
		@DisplayName("정상: 자기 자신과 동등함")
		void equals_WithSelf() {
			// given
			Address address = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.build();
			
			// when & then
			assertThat(address).isEqualTo(address);
		}
		
		@Test
		@DisplayName("정상: null과 비교 시 동등하지 않음")
		void equals_WithNull() {
			// given
			Address address = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.build();
			
			// when & then
			assertThat(address).isNotEqualTo(null);
		}
		
		@Test
		@DisplayName("정상: 다른 타입과 비교 시 동등하지 않음")
		void equals_WithDifferentType() {
			// given
			Address address = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.build();
			
			// when & then
			assertThat(address).isNotEqualTo("서울특별시 강남구 역삼동 123-45");
		}
	}
	
	@Nested
	@DisplayName("실제 주소 테스트")
	class RealWorldAddressTest {
		
		@Test
		@DisplayName("정상: 서울 강남구 주소")
		void seoulGangnamAddress() {
			// given & when
			Address address = Address.builder()
					.province("서울특별시")
					.city("강남구")
					.district("역삼동")
					.fullAddress("서울특별시 강남구 테헤란로 152")
					.addressDetail("강남파이낸스센터 27층")
					.postalCode("06236")
					.build();
			
			// then
			assertThat(address.getShortAddress()).isEqualTo("서울특별시 강남구 역삼동");
			assertThat(address.getFullAddressWithDetail())
					.isEqualTo("서울특별시 강남구 테헤란로 152 강남파이낸스센터 27층");
			assertThat(address.isValid()).isTrue();
		}
		
		@Test
		@DisplayName("정상: 제주도 주소")
		void jejuAddress() {
			// given & when
			Address address = Address.builder()
					.province("제주특별자치도")
					.city("제주시")
					.district("이도2동")
					.fullAddress("제주특별자치도 제주시 첨단로 213")
					.postalCode("63309")
					.build();
			
			// then
			assertThat(address.getShortAddress()).isEqualTo("제주특별자치도 제주시 이도2동");
			assertThat(address.getFullAddressWithDetail()).isEqualTo("제주특별자치도 제주시 첨단로 213");
			assertThat(address.isValid()).isTrue();
		}
		
		@Test
		@DisplayName("정상: 부산 해운대구 주소")
		void busanAddress() {
			// given & when
			Address address = Address.builder()
					.province("부산광역시")
					.city("해운대구")
					.district("우동")
					.fullAddress("부산광역시 해운대구 해운대해변로 264")
					.addressDetail("그랜드조선호텔")
					.postalCode("48099")
					.build();
			
			// then
			assertThat(address.getShortAddress()).isEqualTo("부산광역시 해운대구 우동");
			assertThat(address.getFullAddressWithDetail())
					.isEqualTo("부산광역시 해운대구 해운대해변로 264 그랜드조선호텔");
			assertThat(address.isValid()).isTrue();
		}
	}
	
	@Nested
	@DisplayName("우편번호 테스트")
	class PostalCodeTest {
		
		@Test
		@DisplayName("정상: 5자리 우편번호")
		void postalCode_FiveDigits() {
			// given & when
			Address address = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.postalCode("06234")
					.build();
			
			// then
			assertThat(address.getPostalCode()).isEqualTo("06234");
			assertThat(address.getPostalCode()).hasSize(5);
		}
		
		@Test
		@DisplayName("엣지: 우편번호가 null인 경우")
		void postalCode_Null() {
			// given & when
			Address address = Address.builder()
					.fullAddress("서울특별시 강남구 역삼동 123-45")
					.build();
			
			// then
			assertThat(address.getPostalCode()).isNull();
		}
	}
}
