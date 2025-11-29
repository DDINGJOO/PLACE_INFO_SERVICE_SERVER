package com.teambind.placeinfoserver.place.dto.response;

import com.teambind.placeinfoserver.place.domain.entity.PlaceImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 이미지 정보 DTO
 * imageId와 imageUrl은 항상 쌍으로 관리되며, 이미지의 고유 식별자와 접근 URL을 제공
 *
 * 응답 예시:
 * {
 *   "imageId": "img_12345",
 *   "imageUrl": "https://example.com/images/img_12345.jpg",
 *   "sequence": 1
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageInfoResponse {

	/**
	 * 이미지 고유 식별자
	 */
	private String imageId;

	/**
	 * 이미지 접근 URL
	 */
	private String imageUrl;

	/**
	 * 이미지 순서 (1부터 시작)
	 */
	private Long sequence;

	/**
	 * PlaceImage 엔티티로부터 ImageInfoResponse 생성
	 * imageId와 imageUrl이 쌍으로 존재하는지 확인
	 *
	 * @param placeImage PlaceImage 엔티티
	 * @return ImageInfoResponse 또는 null (유효하지 않은 경우)
	 */
	public static ImageInfoResponse fromEntity(PlaceImage placeImage) {
		if (placeImage == null) {
			return null;
		}

		// imageId와 imageUrl이 쌍으로 존재하는지 확인
		String imageId = placeImage.getId();
		String imageUrl = placeImage.getImageUrl();

		if (imageId == null || imageId.trim().isEmpty() ||
				imageUrl == null || imageUrl.trim().isEmpty()) {
			// 유효하지 않은 이미지는 null 반환
			return null;
		}

		return ImageInfoResponse.builder()
				.imageId(imageId)
				.imageUrl(imageUrl)
				.sequence(placeImage.getSequence())
				.build();
	}
}