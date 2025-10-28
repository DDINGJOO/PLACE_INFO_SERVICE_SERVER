package com.teambind.placeinfoserver.place.events.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 다중 이미지 변경 이벤트 래퍼 Value Object
 * <p>
 * 빈 배열 전송 시에도 referenceId를 포함하기 위한 래퍼 클래스입니다.
 * Consumer가 빈 배열을 받았을 때 어떤 referenceId의 이미지를 삭제해야 하는지 알 수 있도록 합니다.
 *
 * 불변 객체로 설계되어 이벤트의 무결성 보장
 *
 * @author Image Server Team
 * @since 2.1
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class ImagesChangeEventWrapper {

	/**
	 * 참조 ID (장소 ID, 게시글 ID 등)
	 * 빈 배열인 경우에도 이 필드를 통해 어떤 대상의 이미지를 삭제할지 알 수 있습니다.
	 */
	private final String referenceId;

	/**
	 * 이미지 변경 이벤트 리스트 (불변)
	 * - 비어있지 않으면: 해당 이미지들로 전체 교체
	 * - 비어있으면: 전체 삭제
	 */
	private final List<SequentialImageChangeEvent> images;

	/**
	 * Jackson 역직렬화를 위한 생성자
	 */
	@JsonCreator
	public ImagesChangeEventWrapper(
			@JsonProperty("referenceId") String referenceId,
			@JsonProperty("images") List<SequentialImageChangeEvent> images) {
		this.referenceId = referenceId;
		// 방어적 복사 및 불변 리스트로 변환
		this.images = images == null
				? Collections.emptyList()
				: Collections.unmodifiableList(new ArrayList<>(images));
	}

	/**
	 * 이미지가 비어있는지 확인
	 */
	public boolean isEmpty() {
		return images.isEmpty();
	}

	/**
	 * 이미지 개수 반환
	 */
	public int getImageCount() {
		return images.size();
	}
}
