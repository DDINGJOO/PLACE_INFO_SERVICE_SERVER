package com.teambind.placeinfoserver.place.service.command;

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
	 * String ID를 Long으로 안전하게 변환
	 *
	 * @param placeId String 형태의 업체 ID
	 * @return Long 형태의 업체 ID
	 * @throws CustomException ID 형식이 잘못된 경우
	 */
	private Long parseId(String placeId) {
		try {
			return Long.parseLong(placeId);
		} catch (NumberFormatException e) {
			throw InvalidRequestException.invalidFormat("placeId");
		}
	}
	
	
	/**
	 * 업체 위치 정보 업데이트
	 *
	 * @param placeId 업체 ID (String - API 통신용)
	 * @param req     위치 정보 요청 DTO
	 * @return 업데이트된 업체 ID (String - API 응답용)
	 */
	@Transactional
	public String updateLocation(String placeId, PlaceLocationRequest req) {
		PlaceInfo placeInfo = placeInfoRepository.findById(parseId(placeId))
				.orElseThrow(() -> new PlaceNotFoundException());
		
		// 기존 Location이 없으면 새로 생성, 있으면 업데이트
		if (placeInfo.getLocation() == null) {
			placeInfo.setLocation(placeMapper.toLocationEntity(req, placeInfo));
		} else {
			// 부분 업데이트: 요청에 포함된 필드만 업데이트
			updateExistingLocation(placeInfo.getLocation(), req);
		}
		
		// @Transactional이므로 자동으로 변경사항 반영 (더티 체킹)
		return placeId;
	}
	
	/**
	 * 기존 Location 엔티티 부분 업데이트
	 *
	 * @param location 업데이트할 Location 엔티티
	 * @param req      업데이트 요청 DTO
	 */
	private void updateExistingLocation(PlaceLocation location, PlaceLocationRequest req) {
		// 좌표 업데이트
		if (req.getLatitude() != null && req.getLongitude() != null) {
			location.setLatLng(req.getLatitude(), req.getLongitude());
		}
		
		// 주소 업데이트
		if (req.getFrom() != null && req.getAddressData() != null) {
			AddressRequest addressRequest = placeMapper.getAddressParser().parse(req.getFrom(), req.getAddressData());
			Address address = placeMapper.toAddressEntity(addressRequest);
			location.updateAddress(address);
		}
		
		// 위치 안내 업데이트
		if (req.getLocationGuide() != null) {
			location.updateLocationGuide(req.getLocationGuide());
		}
	}
	
	
}
