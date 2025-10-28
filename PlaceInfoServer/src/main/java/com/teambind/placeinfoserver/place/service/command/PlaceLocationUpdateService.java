package com.teambind.placeinfoserver.place.service.command;

import com.teambind.placeinfoserver.place.common.exception.CustomException;
import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
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
	 * @param placeId 업체 ID
	 * @param req     위치 정보 요청 DTO
	 * @return 업데이트된 업체 ID
	 */
	@Transactional
	public String updateLocation(String placeId, PlaceLocationRequest req) {
		PlaceInfo placeInfo = placeInfoRepository.findById(placeId)
				.orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
		
		placeInfo.setLocation(placeMapper.toLocationEntity(req, placeInfo));
		// @Transactional이므로 자동으로 변경사항 반영 (더티 체킹)
		return placeId;
	}
	
	
}
