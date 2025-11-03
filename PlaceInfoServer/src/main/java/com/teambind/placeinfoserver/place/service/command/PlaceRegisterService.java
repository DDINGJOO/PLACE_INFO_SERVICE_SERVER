package com.teambind.placeinfoserver.place.service.command;

import com.teambind.placeinfoserver.place.common.exception.CustomException;
import com.teambind.placeinfoserver.place.common.exception.ErrorCode;
import com.teambind.placeinfoserver.place.common.util.generator.PrimaryKeyGenerator;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.dto.request.PlaceRegisterRequest;
import com.teambind.placeinfoserver.place.dto.request.PlaceUpdateRequest;
import com.teambind.placeinfoserver.place.dto.response.PlaceInfoResponse;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.service.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceRegisterService {

	private final PlaceInfoRepository placeInfoRepository;
	private final PrimaryKeyGenerator pkeyGenerator;
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
			throw new CustomException(ErrorCode.INVALID_FORMAT);
		}
	}
	
	/**
	 * 업체 등록
	 *
	 * @param request 등록 요청 DTO
	 * @return 등록된 업체 정보
	 */
	@Transactional
	public PlaceInfoResponse registerPlace(PlaceRegisterRequest request) {
		// ID 생성 (Long 타입)
		Long generatedId = pkeyGenerator.generateLongKey();

		// DTO -> Entity 변환
		PlaceInfo placeInfo = placeMapper.toEntity(request, generatedId);

		// 저장
		PlaceInfo savedPlace = placeInfoRepository.save(placeInfo);

		// Entity -> Response DTO 변환
		return placeMapper.toResponse(savedPlace);
	}
	
	/**
	 * 업체 정보 수정
	 *
	 * @param placeId 업체 ID (String - API 통신용)
	 * @param request 수정 요청 DTO
	 * @return 수정된 업체 정보
	 */
	@Transactional
	public PlaceInfoResponse updatePlace(String placeId, PlaceUpdateRequest request) {
		// 업체 조회 (String → Long 변환)
		PlaceInfo placeInfo = placeInfoRepository.findById(parseId(placeId))
				.orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

		// 업데이트 (Mapper의 updateEntity 사용)
		placeMapper.updateEntity(placeInfo, request);

		// @Transactional이므로 자동으로 변경사항 반영 (더티 체킹)
		// Entity -> Response DTO 변환
		return placeMapper.toResponse(placeInfo);
	}
	
	
	/**
	 * 업체 삭제 (소프트 삭제)
	 *
	 * @param placeId   업체 ID (String - API 통신용)
	 * @param deletedBy 삭제한 사용자 ID
	 */
	@Transactional
	public void deletePlace(String placeId, String deletedBy) {
		PlaceInfo placeInfo = placeInfoRepository.findById(parseId(placeId))
				.orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

		placeInfo.softDelete(deletedBy);
		// @Transactional이므로 자동으로 변경사항 반영
	}

	/**
	 * 업체 활성화
	 *
	 * @param placeId 업체 ID (String - API 통신용)
	 * @return 업체 ID (String - API 응답용)
	 */
	@Transactional
	public String activatePlace(String placeId) {
		PlaceInfo placeInfo = placeInfoRepository.findById(parseId(placeId))
				.orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

		placeInfo.activate();
		return String.valueOf(placeInfo.getId());  // Long → String 변환
	}

	/**
	 * 업체 비활성화
	 *
	 * @param placeId 업체 ID (String - API 통신용)
	 * @return 업체 ID (String - API 응답용)
	 */
	@Transactional
	public String deactivatePlace(String placeId) {
		PlaceInfo placeInfo = placeInfoRepository.findById(parseId(placeId))
				.orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
		
		placeInfo.deactivate();
		return String.valueOf(placeInfo.getId());  // Long → String 변환
	}
	
	/**
	 * 업체 승인
	 *
	 * @param placeId 업체 ID (String - API 통신용)
	 * @return 업체 ID (String - API 응답용)
	 */
	@Transactional
	public String approvePlace(String placeId) {
		PlaceInfo placeInfo = placeInfoRepository.findById(parseId(placeId))
				.orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
		
		placeInfo.approve();
		return String.valueOf(placeInfo.getId());  // Long → String 변환 (더티체크)
	}
	
	/**
	 * 업체 거부
	 *
	 * @param placeId 업체 ID (String - API 통신용)
	 * @return 업체 ID (String - API 응답용)
	 */
	@Transactional
	public String rejectPlace(String placeId) {
		PlaceInfo placeInfo = placeInfoRepository.findById(parseId(placeId))
				.orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
		
		placeInfo.reject();
		return String.valueOf(placeInfo.getId());  // Long → String 변환 (더티체크)
	}
}
