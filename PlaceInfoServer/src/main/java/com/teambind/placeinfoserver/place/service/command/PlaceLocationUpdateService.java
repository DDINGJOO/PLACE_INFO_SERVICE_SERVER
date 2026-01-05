package com.teambind.placeinfoserver.place.service.command;

import com.teambind.placeinfoserver.place.common.exception.application.ForbiddenException;
import com.teambind.placeinfoserver.place.common.exception.application.InvalidRequestException;
import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.entity.PlaceLocation;
import com.teambind.placeinfoserver.place.domain.vo.Address;
import com.teambind.placeinfoserver.place.dto.request.AddressRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceLocationRequest;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.service.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceLocationUpdateService {

	private final PlaceInfoRepository placeInfoRepository;
	private final PlaceMapper placeMapper;

	/**
	 * 업체 위치 정보 업데이트
	 *
	 * @param placeId 업체 ID (String - API 통신용)
	 * @param userId  요청 사용자 ID
	 * @param req     위치 정보 요청 DTO
	 * @return 업데이트된 업체 ID (String - API 응답용)
	 */
	@Transactional
	public String updateLocation(String placeId, String userId, PlaceLocationRequest req) {
		PlaceInfo placeInfo = placeInfoRepository.findById(parseId(placeId))
				.orElseThrow(PlaceNotFoundException::new);

		validateOwnership(placeInfo, userId);

		if (placeInfo.getLocation() == null) {
			placeInfo.setLocation(placeMapper.toLocationEntity(req, placeInfo));
		} else {
			updateExistingLocation(placeInfo.getLocation(), req);
		}

		return placeId;
	}

	private Long parseId(String placeId) {
		try {
			return Long.parseLong(placeId);
		} catch (NumberFormatException e) {
			throw InvalidRequestException.invalidFormat("placeId");
		}
	}

	private void validateOwnership(PlaceInfo placeInfo, String userId) {
		if (!placeInfo.getUserId().equals(userId)) {
			throw ForbiddenException.notOwner();
		}
	}

	private void updateExistingLocation(PlaceLocation location, PlaceLocationRequest req) {
		if (req.getLatitude() != null && req.getLongitude() != null) {
			location.setLatLng(req.getLatitude(), req.getLongitude());
		}

		if (req.getFrom() != null && req.getAddressData() != null) {
			AddressRequest addressRequest = placeMapper.getAddressParser().parse(req.getFrom(), req.getAddressData());
			Address address = placeMapper.toAddressEntity(addressRequest);
			location.updateAddress(address);
		}

		if (req.getLocationGuide() != null) {
			location.updateLocationGuide(req.getLocationGuide());
		}
	}
}
