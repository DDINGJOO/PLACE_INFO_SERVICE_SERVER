package com.teambind.placeinfoserver.place.service.mapper;

import com.teambind.placeinfoserver.place.domain.entity.*;
import com.teambind.placeinfoserver.place.domain.vo.Address;
import com.teambind.placeinfoserver.place.dto.request.*;
import com.teambind.placeinfoserver.place.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlaceMapper {
	
	// ========== Entity -> Response DTO ==========
	
	/**
	 * PlaceInfo -> PlaceInfoResponse (전체 정보)
	 */
	public PlaceInfoResponse toResponse(PlaceInfo entity) {
		if (entity == null) {
			return null;
		}
		
		return PlaceInfoResponse.builder()
				.id(entity.getId())
				.userId(entity.getUserId())
				.placeName(entity.getPlaceName())
				.description(entity.getDescription())
				.category(entity.getCategory())
				.placeType(entity.getPlaceType())
				.contact(toContactResponse(entity.getContact()))
				.location(toLocationResponse(entity.getLocation()))
				.parking(toParkingResponse(entity.getParking()))
				.imageUrls(entity.getImages().stream()
						.map(PlaceImage::getImageUrl)
						.collect(Collectors.toList()))
				.keywords(entity.getKeywords().stream()
						.map(this::toKeywordResponse)
						.collect(Collectors.toList()))
				.isActive(entity.getIsActive())
				.approvalStatus(entity.getApprovalStatus())
				.ratingAverage(entity.getRatingAverage())
				.reviewCount(entity.getReviewCount())
				.createdAt(entity.getCreatedAt())
				.updatedAt(entity.getUpdatedAt())
				.build();
	}
	
	/**
	 * PlaceInfo -> PlaceInfoSummaryResponse (요약 정보)
	 */
	public PlaceInfoSummaryResponse toSummaryResponse(PlaceInfo entity) {
		if (entity == null) {
			return null;
		}
		
		String thumbnailUrl = null;
		if (!entity.getImages().isEmpty()) {
			thumbnailUrl = entity.getImages().get(0).getImageUrl();
		}
		
		String shortAddress = null;
		if (entity.getLocation() != null && entity.getLocation().getAddress() != null) {
			shortAddress = entity.getLocation().getAddress().getShortAddress();
		}
		
		Boolean parkingAvailable = null;
		if (entity.getParking() != null) {
			parkingAvailable = entity.getParking().getAvailable();
		}
		
		return PlaceInfoSummaryResponse.builder()
				.id(entity.getId())
				.placeName(entity.getPlaceName())
				.category(entity.getCategory())
				.placeType(entity.getPlaceType())
				.thumbnailUrl(thumbnailUrl)
				.shortAddress(shortAddress)
				.parkingAvailable(parkingAvailable)
				.ratingAverage(entity.getRatingAverage())
				.reviewCount(entity.getReviewCount())
				.approvalStatus(entity.getApprovalStatus())
				.isActive(entity.getIsActive())
				.build();
	}
	
	/**
	 * PlaceContact -> PlaceContactResponse
	 */
	public PlaceContactResponse toContactResponse(PlaceContact entity) {
		if (entity == null) {
			return null;
		}
		
		return PlaceContactResponse.builder()
				.contact(entity.getContact())
				.email(entity.getEmail())
				.websites(entity.getWebsites())
				.socialLinks(entity.getSocialLinks())
				.build();
	}
	
	/**
	 * PlaceLocation -> PlaceLocationResponse
	 */
	public PlaceLocationResponse toLocationResponse(PlaceLocation entity) {
		if (entity == null) {
			return null;
		}
		
		return PlaceLocationResponse.builder()
				.address(toAddressResponse(entity.getAddress()))
				.latitude(entity.getLatitude())
				.longitude(entity.getLongitude())
				.locationGuide(entity.getLocationGuide())
				.build();
	}
	
	/**
	 * Address -> AddressResponse
	 */
	public AddressResponse toAddressResponse(Address address) {
		if (address == null) {
			return null;
		}
		
		return AddressResponse.builder()
				.province(address.getProvince())
				.city(address.getCity())
				.district(address.getDistrict())
				.fullAddress(address.getFullAddress())
				.addressDetail(address.getAddressDetail())
				.postalCode(address.getPostalCode())
				.shortAddress(address.getShortAddress())
				.build();
	}
	
	/**
	 * PlaceParking -> PlaceParkingResponse
	 */
	public PlaceParkingResponse toParkingResponse(PlaceParking entity) {
		if (entity == null) {
			return null;
		}
		
		return PlaceParkingResponse.builder()
				.available(entity.getAvailable())
				.parkingType(entity.getParkingType())
				.description(entity.getDescription())
				.build();
	}
	
	/**
	 * Keyword -> KeywordResponse
	 */
	public KeywordResponse toKeywordResponse(Keyword entity) {
		if (entity == null) {
			return null;
		}
		
		return KeywordResponse.builder()
				.id(entity.getId())
				.name(entity.getName())
				.type(entity.getType())
				.description(entity.getDescription())
				.displayOrder(entity.getDisplayOrder())
				.build();
	}
	
	// ========== Request DTO -> Entity ==========
	
	/**
	 * PlaceRegisterRequest -> PlaceInfo (신규 등록)
	 */
	public PlaceInfo toEntity(PlaceRegisterRequest request, String generatedId) {
		if (request == null) {
			return null;
		}
		
		PlaceInfo placeInfo = PlaceInfo.builder()
				.id(generatedId)
				.userId(request.getPlaceOwnerId())
				.placeName(request.getPlaceName())
				.description(request.getDescription())
				.category(request.getCategory())
				.placeType(request.getPlaceType())
				.isActive(false)
				.build();
		
		// 연관관계 설정
		if (request.getContact() != null) {
			placeInfo.setContact(toContactEntity(request.getContact(), placeInfo));
		}
		
		if (request.getParking() != null) {
			placeInfo.setParking(toParkingEntity(request.getParking(), placeInfo));
		}
		
		return placeInfo;
	}
	
	/**
	 * PlaceContactRequest -> PlaceContact
	 */
	public PlaceContact toContactEntity(PlaceContactRequest request, PlaceInfo placeInfo) {
		if (request == null) {
			return null;
		}
		
		return PlaceContact.builder()
				.contact(request.getContact())
				.email(request.getEmail())
				.websites(request.getWebsites())
				.socialLinks(request.getSocialLinks())
				.placeInfo(placeInfo)
				.build();
	}
	
	/**
	 * PlaceLocationRequest -> PlaceLocation
	 */
	public PlaceLocation toLocationEntity(PlaceLocationRequest request, PlaceInfo placeInfo) {
		if (request == null) {
			return null;
		}
		
		PlaceLocation location = PlaceLocation.builder()
				.address(toAddressEntity(request.getAddress()))
				.locationGuide(request.getLocationGuide())
				.placeInfo(placeInfo)
				.build();
		
		// 좌표 설정 (GeometryUtil 사용)
		if (request.getLatitude() != null && request.getLongitude() != null) {
			location.setLatLng(request.getLatitude(), request.getLongitude());
		}
		
		return location;
	}
	
	/**
	 * AddressRequest -> Address (Value Object)
	 */
	public Address toAddressEntity(AddressRequest request) {
		if (request == null) {
			return null;
		}
		
		return Address.builder()
				.province(request.getProvince())
				.city(request.getCity())
				.district(request.getDistrict())
				.fullAddress(request.getFullAddress())
				.addressDetail(request.getAddressDetail())
				.postalCode(request.getPostalCode())
				.build();
	}
	
	/**
	 * PlaceParkingUpdateRequest -> PlaceParking
	 */
	public PlaceParking toParkingEntity(PlaceParkingUpdateRequest request, PlaceInfo placeInfo) {
		if (request == null) {
			return null;
		}
		
		return PlaceParking.builder()
				.available(request.getAvailable())
				.parkingType(request.getParkingType())
				.description(request.getDescription())
				.placeInfo(placeInfo)
				.build();
	}
	
	// ========== Update 메서드 (기존 엔티티 업데이트) ==========
	
	/**
	 * PlaceUpdateRequest로 PlaceInfo 업데이트
	 */
	public void updateEntity(PlaceInfo entity, PlaceUpdateRequest request) {
		if (request == null || entity == null) {
			return;
		}
		
		// 기본 정보 업데이트
		if (request.getPlaceName() != null) {
			entity.updatePlaceName(request.getPlaceName());
		}
		if (request.getDescription() != null) {
			entity.updateDescription(request.getDescription());
		}
		if (request.getCategory() != null) {
			entity.updateCategory(request.getCategory());
		}
		if (request.getPlaceType() != null) {
			entity.updatePlaceType(request.getPlaceType());
		}
		
		// Contact 업데이트
		if (request.getContact() != null) {
			updateContactEntity(entity.getContact(), request.getContact());
		}
		
		// Location 업데이트
		if (request.getLocation() != null) {
			updateLocationEntity(entity.getLocation(), request.getLocation());
		}
		
		// Parking 업데이트
		if (request.getParking() != null) {
			updateParkingEntity(entity.getParking(), request.getParking());
		}
	}
	
	/**
	 * PlaceContact 업데이트
	 */
	private void updateContactEntity(PlaceContact entity, PlaceContactRequest request) {
		if (entity == null || request == null) {
			return;
		}
		
		if (request.getContact() != null) {
			entity.setContact(request.getContact());
		}
		if (request.getEmail() != null) {
			entity.setEmail(request.getEmail());
		}
		if (request.getWebsites() != null) {
			entity.setWebsites(request.getWebsites());
		}
		if (request.getSocialLinks() != null) {
			entity.setSocialLinks(request.getSocialLinks());
		}
	}
	
	/**
	 * PlaceLocation 업데이트
	 */
	private void updateLocationEntity(PlaceLocation entity, PlaceLocationRequest request) {
		if (entity == null || request == null) {
			return;
		}
		
		if (request.getAddress() != null) {
			entity.setAddress(toAddressEntity(request.getAddress()));
		}
		
		if (request.getLatitude() != null && request.getLongitude() != null) {
			entity.setLatLng(request.getLatitude(), request.getLongitude());
		}
		
		if (request.getLocationGuide() != null) {
			entity.setLocationGuide(request.getLocationGuide());
		}
	}
	
	/**
	 * PlaceParking 업데이트
	 */
	private void updateParkingEntity(PlaceParking entity, PlaceParkingUpdateRequest request) {
		if (entity == null || request == null) {
			return;
		}
		
		if (request.getAvailable() != null) {
			entity.setAvailable(request.getAvailable());
		}
		if (request.getParkingType() != null) {
			entity.setParkingType(request.getParkingType());
		}
		if (request.getDescription() != null) {
			entity.setDescription(request.getDescription());
		}
	}
	
	// ========== List 변환 ==========
	
	/**
	 * List<PlaceInfo> -> List<PlaceInfoResponse>
	 */
	public List<PlaceInfoResponse> toResponseList(List<PlaceInfo> entities) {
		if (entities == null) {
			return List.of();
		}
		return entities.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}
	
	/**
	 * List<PlaceInfo> -> List<PlaceInfoSummaryResponse>
	 */
	public List<PlaceInfoSummaryResponse> toSummaryResponseList(List<PlaceInfo> entities) {
		if (entities == null) {
			return List.of();
		}
		return entities.stream()
				.map(this::toSummaryResponse)
				.collect(Collectors.toList());
	}
}
