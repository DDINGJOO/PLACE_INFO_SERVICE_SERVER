package com.teambind.placeinfoserver.place.service.usecase.query;

import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.entity.Room;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.repository.RoomRepository;
import com.teambind.placeinfoserver.place.service.mapper.PlaceMapper;
import com.teambind.placeinfoserver.place.service.usecase.common.IdParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 업체 상세 조회 UseCase
 * SRP: 업체 상세 정보 조회만을 담당
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPlaceDetailUseCase {
	
	private final PlaceInfoRepository placeInfoRepository;
	private final RoomRepository roomRepository;
	private final PlaceMapper placeMapper;
	
	/**
	 * 업체 상세 조회
	 *
	 * @param placeId 업체 ID
	 * @return 업체 상세 정보
	 */
	public PlaceInfoResponse execute(String placeId) {
		Long parsedPlaceId = IdParser.parsePlaceId(placeId);
		PlaceInfo placeInfo = placeInfoRepository.findById(parsedPlaceId)
				.orElseThrow(() -> new PlaceNotFoundException());
		
		PlaceInfoResponse response = placeMapper.toResponse(placeInfo);
		
		// Room 정보 추가
		List<Room> rooms = roomRepository.findByPlaceIdAndIsActiveTrue(parsedPlaceId);
		response.setRoomCount(rooms.size());
		response.setRoomIds(rooms.stream()
				.map(Room::getRoomId)
				.collect(Collectors.toList()));
		
		return response;
	}
}
