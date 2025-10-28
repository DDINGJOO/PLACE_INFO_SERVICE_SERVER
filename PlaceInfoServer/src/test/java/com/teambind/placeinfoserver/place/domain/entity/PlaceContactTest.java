package com.teambind.placeinfoserver.place.domain.entity;

import com.teambind.placeinfoserver.place.fixture.PlaceTestFactory;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PlaceContact 엔티티 단위 테스트
 * 업체 연락처 정보 도메인 로직 검증
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaceContactTest {

	@Nested
	@DisplayName("엔티티 생성 테스트")
	class CreationTest {

		@Test
		@Order(1)
		@DisplayName("PlaceContact 생성 - 성공")
		void create_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.email("test@example.com")
					.websites(new ArrayList<>())
					.socialLinks(new ArrayList<>())
					.build();

			// Then
			assertThat(contact).isNotNull();
			assertThat(contact.getPlaceInfo()).isEqualTo(placeInfo);
			assertThat(contact.getContact()).isEqualTo("02-1234-5678");
			assertThat(contact.getEmail()).isEqualTo("test@example.com");
		}

		@Test
		@Order(2)
		@DisplayName("PlaceContact 생성 - 최소 정보")
		void create_Minimal_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("010-1234-5678")
					.build();

			// Then
			assertThat(contact).isNotNull();
			assertThat(contact.getContact()).isEqualTo("010-1234-5678");
			assertThat(contact.getEmail()).isNull();
		}

		@Test
		@Order(3)
		@DisplayName("Builder 기본값 확인")
		void create_DefaultValues_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.build();

			// Then
			assertThat(contact.getWebsites()).isNotNull();
			assertThat(contact.getWebsites()).isEmpty();
			assertThat(contact.getSocialLinks()).isNotNull();
			assertThat(contact.getSocialLinks()).isEmpty();
		}
	}

	@Nested
	@DisplayName("홈페이지 URL 관리 테스트")
	class WebsiteManagementTest {

		@Test
		@Order(4)
		@DisplayName("홈페이지 URL 추가 - 성공")
		void addWebsite_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.build();

			// When
			contact.addWebsite("https://example.com");
			contact.addWebsite("https://blog.example.com");

			// Then
			assertThat(contact.getWebsites()).hasSize(2);
			assertThat(contact.getWebsites()).contains("https://example.com");
			assertThat(contact.getWebsites()).contains("https://blog.example.com");
		}

		@Test
		@Order(5)
		@DisplayName("홈페이지 URL 추가 - websites가 null일 때 초기화")
		void addWebsite_NullList_InitializeAndAdd() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.build();
			contact.setWebsites(null);

			// When
			contact.addWebsite("https://example.com");

			// Then
			assertThat(contact.getWebsites()).hasSize(1);
			assertThat(contact.getWebsites()).contains("https://example.com");
		}

		@Test
		@Order(6)
		@DisplayName("홈페이지 URL 10개까지 추가 - 성공")
		void addWebsite_UpTo10_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.build();

			// When
			for (int i = 1; i <= 10; i++) {
				contact.addWebsite("https://example" + i + ".com");
			}

			// Then
			assertThat(contact.getWebsites()).hasSize(10);
		}

		@Test
		@Order(7)
		@DisplayName("홈페이지 URL 11개 추가 - 예외 발생")
		void addWebsite_MoreThan10_ThrowsException() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.build();

			for (int i = 1; i <= 10; i++) {
				contact.addWebsite("https://example" + i + ".com");
			}

			// When & Then
			assertThatThrownBy(() -> contact.addWebsite("https://example11.com"))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("홈페이지 URL은 최대 10개까지만 등록 가능합니다.");
		}
	}

	@Nested
	@DisplayName("소셜 링크 관리 테스트")
	class SocialLinkManagementTest {

		@Test
		@Order(8)
		@DisplayName("소셜 링크 추가 - 성공")
		void addSocialLink_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.build();

			// When
			contact.addSocialLink("https://instagram.com/example");
			contact.addSocialLink("https://facebook.com/example");
			contact.addSocialLink("https://youtube.com/example");

			// Then
			assertThat(contact.getSocialLinks()).hasSize(3);
			assertThat(contact.getSocialLinks()).contains("https://instagram.com/example");
			assertThat(contact.getSocialLinks()).contains("https://facebook.com/example");
			assertThat(contact.getSocialLinks()).contains("https://youtube.com/example");
		}

		@Test
		@Order(9)
		@DisplayName("소셜 링크 추가 - socialLinks가 null일 때 초기화")
		void addSocialLink_NullList_InitializeAndAdd() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.build();
			contact.setSocialLinks(null);

			// When
			contact.addSocialLink("https://instagram.com/example");

			// Then
			assertThat(contact.getSocialLinks()).hasSize(1);
			assertThat(contact.getSocialLinks()).contains("https://instagram.com/example");
		}

		@Test
		@Order(10)
		@DisplayName("소셜 링크 10개까지 추가 - 성공")
		void addSocialLink_UpTo10_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.build();

			// When
			for (int i = 1; i <= 10; i++) {
				contact.addSocialLink("https://social" + i + ".com");
			}

			// Then
			assertThat(contact.getSocialLinks()).hasSize(10);
		}

		@Test
		@Order(11)
		@DisplayName("소셜 링크 11개 추가 - 예외 발생")
		void addSocialLink_MoreThan10_ThrowsException() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.build();

			for (int i = 1; i <= 10; i++) {
				contact.addSocialLink("https://social" + i + ".com");
			}

			// When & Then
			assertThatThrownBy(() -> contact.addSocialLink("https://social11.com"))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("소셜 링크는 최대 10개까지만 등록 가능합니다.");
		}
	}

	@Nested
	@DisplayName("연락처 정보 설정 테스트")
	class ContactInfoTest {

		@Test
		@Order(12)
		@DisplayName("전화번호 형식 - 하이픈 포함")
		void setContact_WithHyphen_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.build();

			// When
			contact.setContact("02-1234-5678");

			// Then
			assertThat(contact.getContact()).isEqualTo("02-1234-5678");
		}

		@Test
		@Order(13)
		@DisplayName("전화번호 형식 - 하이픈 없음")
		void setContact_WithoutHyphen_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.build();

			// When
			contact.setContact("01012345678");

			// Then
			assertThat(contact.getContact()).isEqualTo("01012345678");
		}

		@Test
		@Order(14)
		@DisplayName("이메일 주소 설정 - 성공")
		void setEmail_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.build();

			// When
			contact.setEmail("contact@example.com");

			// Then
			assertThat(contact.getEmail()).isEqualTo("contact@example.com");
		}
	}

	@Nested
	@DisplayName("연관관계 테스트")
	class RelationshipTest {

		@Test
		@Order(15)
		@DisplayName("PlaceInfo와의 연관관계 - 양방향")
		void relationship_WithPlaceInfo_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();

			// When
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.build();

			// Then
			assertThat(contact.getPlaceInfo()).isEqualTo(placeInfo);
			assertThat(placeInfo.getContact()).isNotNull();
		}
	}

	@Nested
	@DisplayName("복합 시나리오 테스트")
	class ComplexScenarioTest {

		@Test
		@Order(16)
		@DisplayName("모든 정보 설정 - 성공")
		void setAllInfo_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.email("info@example.com")
					.build();

			// When
			contact.addWebsite("https://example.com");
			contact.addWebsite("https://blog.example.com");
			contact.addSocialLink("https://instagram.com/example");
			contact.addSocialLink("https://facebook.com/example");

			// Then
			assertThat(contact.getContact()).isNotNull();
			assertThat(contact.getEmail()).isNotNull();
			assertThat(contact.getWebsites()).hasSize(2);
			assertThat(contact.getSocialLinks()).hasSize(2);
		}

		@Test
		@Order(17)
		@DisplayName("연락처 정보 수정 - 성공")
		void updateContactInfo_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.email("old@example.com")
					.build();

			// When
			contact.setContact("02-8765-4321");
			contact.setEmail("new@example.com");

			// Then
			assertThat(contact.getContact()).isEqualTo("02-8765-4321");
			assertThat(contact.getEmail()).isEqualTo("new@example.com");
		}
	}

	@Nested
	@DisplayName("실제 데이터 테스트")
	class RealDataTest {

		@Test
		@Order(18)
		@DisplayName("실제 웹사이트 URL - 성공")
		void addWebsite_RealUrls_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.build();

			// When
			contact.addWebsite("https://www.example.com");
			contact.addWebsite("https://blog.example.com");
			contact.addWebsite("https://shop.example.com");

			// Then
			assertThat(contact.getWebsites()).hasSize(3);
		}

		@Test
		@Order(19)
		@DisplayName("실제 소셜 미디어 링크 - 성공")
		void addSocialLink_RealLinks_Success() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createPlaceInfo();
			PlaceContact contact = PlaceContact.builder()
					.placeInfo(placeInfo)
					.contact("02-1234-5678")
					.build();

			// When
			contact.addSocialLink("https://www.instagram.com/teambind_official");
			contact.addSocialLink("https://www.facebook.com/teambind");
			contact.addSocialLink("https://www.youtube.com/c/teambind");
			contact.addSocialLink("https://twitter.com/teambind");

			// Then
			assertThat(contact.getSocialLinks()).hasSize(4);
		}
	}
}
