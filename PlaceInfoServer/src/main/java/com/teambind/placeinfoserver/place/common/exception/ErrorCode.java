package com.teambind.placeinfoserver.place.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
	// Place 관련 에러 (PLACE_0XX)
	PLACE_ALREADY_EXISTS("PLACE_001", "Place already exists", HttpStatus.CONFLICT),
	PLACE_NOT_FOUND("PLACE_002", "Place not found", HttpStatus.NOT_FOUND),
	PLACE_ALREADY_DELETED("PLACE_003", "Place is already deleted", HttpStatus.BAD_REQUEST),
	PLACE_NOT_ACTIVE("PLACE_004", "Place is not active", HttpStatus.BAD_REQUEST),
	PLACE_ALREADY_ACTIVE("PLACE_005", "Place is already active", HttpStatus.BAD_REQUEST),
	PLACE_NOT_APPROVED("PLACE_006", "Place is not approved yet", HttpStatus.FORBIDDEN),
	PLACE_ALREADY_APPROVED("PLACE_007", "Place is already approved", HttpStatus.BAD_REQUEST),
	PLACE_REJECTED("PLACE_008", "Place has been rejected", HttpStatus.FORBIDDEN),
	
	// 장소 정보 관련 에러 (PLACE_1XX)
	PLACE_NAME_REQUIRED("PLACE_101", "Place name is required", HttpStatus.BAD_REQUEST),
	PLACE_DESCRIPTION_TOO_LONG("PLACE_102", "Place description is too long", HttpStatus.BAD_REQUEST),
	PLACE_INVALID_CAPACITY("PLACE_103", "Invalid place capacity", HttpStatus.BAD_REQUEST),
	
	// 위치 관련 에러 (LOCATION_0XX)
	LOCATION_NOT_FOUND("LOCATION_001", "Location not found", HttpStatus.NOT_FOUND),
	LOCATION_INVALID_COORDINATES("LOCATION_002", "Invalid coordinates", HttpStatus.BAD_REQUEST),
	LOCATION_LATITUDE_OUT_OF_RANGE("LOCATION_003", "Latitude must be between -90 and 90", HttpStatus.BAD_REQUEST),
	LOCATION_LONGITUDE_OUT_OF_RANGE("LOCATION_004", "Longitude must be between -180 and 180", HttpStatus.BAD_REQUEST),
	LOCATION_ADDRESS_REQUIRED("LOCATION_005", "Address is required", HttpStatus.BAD_REQUEST),
	
	// 검색 관련 에러 (SEARCH_0XX)
	SEARCH_INVALID_RADIUS("SEARCH_001", "Invalid search radius", HttpStatus.BAD_REQUEST),
	SEARCH_RADIUS_TOO_LARGE("SEARCH_002", "Search radius is too large (max 50km)", HttpStatus.BAD_REQUEST),
	SEARCH_INVALID_PAGE_SIZE("SEARCH_003", "Invalid page size", HttpStatus.BAD_REQUEST),
	SEARCH_PAGE_SIZE_TOO_LARGE("SEARCH_004", "Page size is too large (max 100)", HttpStatus.BAD_REQUEST),
	SEARCH_INVALID_CURSOR("SEARCH_005", "Invalid cursor format", HttpStatus.BAD_REQUEST),
	SEARCH_MISSING_COORDINATES("SEARCH_006", "Coordinates are required for location-based search", HttpStatus.BAD_REQUEST),
	SEARCH_MISSING_REGION("SEARCH_007", "Province is required for region-based search", HttpStatus.BAD_REQUEST),
	
	// 키워드 관련 에러 (KEYWORD_0XX)
	KEYWORD_NOT_FOUND("KEYWORD_001", "Keyword not found", HttpStatus.NOT_FOUND),
	KEYWORD_LIMIT_EXCEEDED("KEYWORD_002", "Keyword limit exceeded (max 20)", HttpStatus.BAD_REQUEST),
	KEYWORD_INVALID_TYPE("KEYWORD_003", "Invalid keyword type", HttpStatus.BAD_REQUEST),
	
	// 이미지 관련 에러 (IMAGE_0XX)
	IMAGE_NOT_FOUND("IMAGE_001", "Image not found", HttpStatus.NOT_FOUND),
	IMAGE_LIMIT_EXCEEDED("IMAGE_002", "Image limit exceeded (max 10)", HttpStatus.BAD_REQUEST),
	IMAGE_INVALID_URL("IMAGE_003", "Invalid image URL", HttpStatus.BAD_REQUEST),
	IMAGE_INVALID_ORDER("IMAGE_004", "Invalid image order", HttpStatus.BAD_REQUEST),
	
	// 연락처 관련 에러 (CONTACT_0XX)
	CONTACT_NOT_FOUND("CONTACT_001", "Contact not found", HttpStatus.NOT_FOUND),
	CONTACT_INVALID_PHONE("CONTACT_002", "Invalid phone number format", HttpStatus.BAD_REQUEST),
	CONTACT_INVALID_EMAIL("CONTACT_003", "Invalid email format", HttpStatus.BAD_REQUEST),
	CONTACT_INVALID_URL("CONTACT_004", "Invalid URL format", HttpStatus.BAD_REQUEST),
	
	// 주차 관련 에러 (PARKING_0XX)
	PARKING_NOT_FOUND("PARKING_001", "Parking information not found", HttpStatus.NOT_FOUND),
	PARKING_INVALID_TYPE("PARKING_002", "Invalid parking type", HttpStatus.BAD_REQUEST),
	
	// 권한 관련 에러 (AUTH_0XX)
	UNAUTHORIZED("AUTH_001", "Unauthorized access", HttpStatus.UNAUTHORIZED),
	FORBIDDEN("AUTH_002", "Access forbidden", HttpStatus.FORBIDDEN),
	INSUFFICIENT_PERMISSION("AUTH_003", "Insufficient permission", HttpStatus.FORBIDDEN),
	APP_TYPE_REQUIRED("AUTH_004", "PLACE_MANAGER app required", HttpStatus.FORBIDDEN),
	
	// 검증 관련 에러 (VALIDATION_0XX)
	INVALID_INPUT("VALIDATION_001", "Invalid input", HttpStatus.BAD_REQUEST),
	REQUIRED_FIELD_MISSING("VALIDATION_002", "Required field is missing", HttpStatus.BAD_REQUEST),
	INVALID_FORMAT("VALIDATION_003", "Invalid format", HttpStatus.BAD_REQUEST),
	VALUE_OUT_OF_RANGE("VALIDATION_004", "Value is out of range", HttpStatus.BAD_REQUEST),
	
	// 시스템 에러 (SYSTEM_0XX)
	INTERNAL_SERVER_ERROR("SYSTEM_001", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
	DATABASE_ERROR("SYSTEM_002", "Database error", HttpStatus.INTERNAL_SERVER_ERROR),
	EXTERNAL_API_ERROR("SYSTEM_003", "External API error", HttpStatus.BAD_GATEWAY),
	CACHE_ERROR("SYSTEM_004", "Cache error", HttpStatus.INTERNAL_SERVER_ERROR),
	EVENT_PUBLISH_FAILED("SYSTEM_005", "Failed to publish event", HttpStatus.INTERNAL_SERVER_ERROR),
	;
	private final String errCode;
	private final String message;
	private final HttpStatus status;
	
	ErrorCode(String errCode, String message, HttpStatus status) {
		
		this.status = status;
		this.errCode = errCode;
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "ErrorCode{"
				+ " status='"
				+ status
				+ '\''
				+ "errCode='"
				+ errCode
				+ '\''
				+ ", message='"
				+ message
				+ '\''
				+ '}';
	}
}
