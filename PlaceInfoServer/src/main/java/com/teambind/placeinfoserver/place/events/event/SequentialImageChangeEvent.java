package com.teambind.placeinfoserver.place.events.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SequentialImageChangeEvent {
	private String imageId;
	private String imageUrl;
	private String referenceId;
	private Integer sequence;
}
