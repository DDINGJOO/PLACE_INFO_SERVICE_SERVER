package com.teambind.placeinfoserver.place.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
	
	private String province;
	private String city;
	private String district;
	private String fullAddress;
	private String addressDetail;
	private String postalCode;
}
