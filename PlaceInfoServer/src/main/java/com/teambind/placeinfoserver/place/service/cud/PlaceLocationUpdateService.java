package com.teambind.placeinfoserver.place.service.cud;

import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.dto.request.PlaceLocationRequest;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.service.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceLocationUpdateService {
	
	private final PlaceInfoRepository placeInfoRepository;
	private final PlaceMapper placeMapper;
	
	
	public String updateLocation(String placeId, PlaceLocationRequest req) {
		PlaceInfo placeInfo = placeInfoRepository.findById(placeId)
				.orElseThrow(() -> new RuntimeException("Place Not Found"));
		
		placeInfo.setLocation(placeMapper.toLocationEntity(req, placeInfo));
		placeInfoRepository.save(placeInfo);
		return placeId;
	}
	
	
}
