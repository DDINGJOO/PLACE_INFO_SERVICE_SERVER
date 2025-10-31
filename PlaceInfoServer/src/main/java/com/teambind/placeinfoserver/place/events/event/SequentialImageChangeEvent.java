package com.teambind.placeinfoserver.place.events.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 순차적 이미지 변경 이벤트 Value Object
 * 단일 이미지의 변경 정보를 담는 불변 객체
 * <p>
 * 불변 객체로 설계되어 이벤트의 무결성 보장
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class SequentialImageChangeEvent {
	
	/**
	 * 이미지 ID
	 */
	private final String imageId;
	
	/**
	 * 이미지 URL
	 */
	private final String imageUrl;
	
	/**
	 * 참조 ID (장소 ID 등)
	 */
	private final String referenceId;
	
	/**
	 * 이미지 순서
	 */
	private final Integer sequence;
	
	/**
	 * Jackson 역직렬화를 위한 생성자
	 */
	@JsonCreator
	public SequentialImageChangeEvent(
			@JsonProperty("imageId") String imageId,
			@JsonProperty("imageUrl") String imageUrl,
			@JsonProperty("referenceId") String referenceId,
			@JsonProperty("sequence") Integer sequence) {
		this.imageId = imageId;
		this.imageUrl = imageUrl;
		this.referenceId = referenceId;
		this.sequence = sequence;
	}
}
