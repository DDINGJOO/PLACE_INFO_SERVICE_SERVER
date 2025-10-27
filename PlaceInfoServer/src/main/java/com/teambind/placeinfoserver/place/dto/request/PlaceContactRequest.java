package com.teambind.placeinfoserver.place.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceContactRequest {
	
	private String contact;
	private String email;
	private List<String> websites;
	private List<String> socialLinks;
}
