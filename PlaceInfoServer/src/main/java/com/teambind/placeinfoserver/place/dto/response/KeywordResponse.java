package com.teambind.placeinfoserver.place.dto.response;

import com.teambind.placeinfoserver.place.domain.enums.KeywordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordResponse {
	
	private Long id;
	private String name;
	private KeywordType type;
	private String description;
	private Integer displayOrder;
}
