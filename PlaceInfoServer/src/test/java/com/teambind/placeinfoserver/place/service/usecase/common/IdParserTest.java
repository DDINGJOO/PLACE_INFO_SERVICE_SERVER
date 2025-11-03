package com.teambind.placeinfoserver.place.service.usecase.common;

import com.teambind.placeinfoserver.place.common.exception.application.InvalidRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * IdParser 유틸리티 단위 테스트
 */
@DisplayName("IdParser 단위 테스트")
class IdParserTest {

	@Nested
	@DisplayName("parsePlaceId 메서드 테스트")
	class ParsePlaceIdTests {

		@Test
		@DisplayName("유효한 숫자 문자열을 Long으로 변환한다")
		void parsesValidNumericString() {
			// Given
			String validId = "123456789";

			// When
			Long result = IdParser.parsePlaceId(validId);

			// Then
			assertThat(result).isEqualTo(123456789L);
		}

		@Test
		@DisplayName("0을 올바르게 변환한다")
		void parsesZero() {
			// Given
			String zeroId = "0";

			// When
			Long result = IdParser.parsePlaceId(zeroId);

			// Then
			assertThat(result).isEqualTo(0L);
		}

		@Test
		@DisplayName("매우 큰 숫자를 올바르게 변환한다")
		void parsesLargeNumber() {
			// Given
			String largeId = String.valueOf(Long.MAX_VALUE);

			// When
			Long result = IdParser.parsePlaceId(largeId);

			// Then
			assertThat(result).isEqualTo(Long.MAX_VALUE);
		}

		@Test
		@DisplayName("null 입력 시 NumberFormatException이 발생한다")
		void throwsExceptionForNull() {
			// When & Then
			assertThatThrownBy(() -> IdParser.parsePlaceId(null))
					.isInstanceOf(InvalidRequestException.class)
					.hasMessageContaining("placeId");
		}

		@Test
		@DisplayName("빈 문자열 입력 시 예외가 발생한다")
		void throwsExceptionForEmptyString() {
			// Given
			String emptyId = "";

			// When & Then
			assertThatThrownBy(() -> IdParser.parsePlaceId(emptyId))
					.isInstanceOf(InvalidRequestException.class)
					.hasMessageContaining("placeId");
		}

		@Test
		@DisplayName("숫자가 아닌 문자열 입력 시 예외가 발생한다")
		void throwsExceptionForNonNumericString() {
			// Given
			String invalidId = "abc123";

			// When & Then
			assertThatThrownBy(() -> IdParser.parsePlaceId(invalidId))
					.isInstanceOf(InvalidRequestException.class)
					.hasMessageContaining("placeId");
		}

		@Test
		@DisplayName("공백이 포함된 문자열 입력 시 예외가 발생한다")
		void throwsExceptionForStringWithSpaces() {
			// Given
			String invalidId = "123 456";

			// When & Then
			assertThatThrownBy(() -> IdParser.parsePlaceId(invalidId))
					.isInstanceOf(InvalidRequestException.class)
					.hasMessageContaining("placeId");
		}

		@Test
		@DisplayName("소수점이 포함된 문자열 입력 시 예외가 발생한다")
		void throwsExceptionForDecimalString() {
			// Given
			String invalidId = "123.456";

			// When & Then
			assertThatThrownBy(() -> IdParser.parsePlaceId(invalidId))
					.isInstanceOf(InvalidRequestException.class)
					.hasMessageContaining("placeId");
		}

		@Test
		@DisplayName("Long 범위를 초과하는 숫자 입력 시 예외가 발생한다")
		void throwsExceptionForNumberOutOfRange() {
			// Given
			String outOfRangeId = "99999999999999999999";

			// When & Then
			assertThatThrownBy(() -> IdParser.parsePlaceId(outOfRangeId))
					.isInstanceOf(InvalidRequestException.class)
					.hasMessageContaining("placeId");
		}

		@Test
		@DisplayName("음수를 올바르게 변환한다")
		void parsesNegativeNumber() {
			// Given
			String negativeId = "-123";

			// When
			Long result = IdParser.parsePlaceId(negativeId);

			// Then
			assertThat(result).isEqualTo(-123L);
		}
	}
}
