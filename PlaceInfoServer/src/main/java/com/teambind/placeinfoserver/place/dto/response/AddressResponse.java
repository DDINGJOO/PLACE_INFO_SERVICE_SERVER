package com.teambind.placeinfoserver.place.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
	
	private String province;
	private String city;
	private String district;
	private String fullAddress;
	private String addressDetail;
	private String postalCode;
	private String shortAddress;
}
