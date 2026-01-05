package com.teambind.placeinfoserver.place.controller.aspect;

import com.teambind.placeinfoserver.place.common.exception.application.ForbiddenException;
import com.teambind.placeinfoserver.place.common.exception.application.InvalidRequestException;
import com.teambind.placeinfoserver.place.common.exception.domain.PlaceNotFoundException;
import com.teambind.placeinfoserver.place.controller.annotation.RequirePlaceManager;
import com.teambind.placeinfoserver.place.controller.annotation.ValidateOwnership;
import com.teambind.placeinfoserver.place.domain.entity.PlaceInfo;
import com.teambind.placeinfoserver.place.domain.enums.AppType;
import com.teambind.placeinfoserver.place.repository.PlaceInfoRepository;
import com.teambind.placeinfoserver.place.service.usecase.common.IdParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Parameter;

/**
 * PLACE_MANAGER 앱 전용 API 검증 Aspect
 * - 헤더 검증 (X-App-Type, X-User-Id)
 * - 소유권 검증 (placeId와 userId 매칭)
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(1)
public class PlaceManagerAspect {
	
	private static final String HEADER_APP_TYPE = "X-App-Type";
	private static final String HEADER_USER_ID = "X-User-Id";
	
	private final PlaceInfoRepository placeInfoRepository;
	
	/**
	 * @RequirePlaceManager 어노테이션이 붙은 메서드 실행 전 헤더 검증
	 */
	@Before("@annotation(requirePlaceManager)")
	public void validatePlaceManagerHeaders(JoinPoint joinPoint, RequirePlaceManager requirePlaceManager) {
		HttpServletRequest request = getCurrentRequest();
		
		String appTypeHeader = request.getHeader(HEADER_APP_TYPE);
		String userId = request.getHeader(HEADER_USER_ID);
		
		validateRequiredHeader(userId, HEADER_USER_ID);
		validatePlaceManagerApp(parseAppType(appTypeHeader));
		
		log.debug("PlaceManager 헤더 검증 통과: userId={}", userId);
	}
	
	/**
	 * @ValidateOwnership 어노테이션이 붙은 메서드 실행 전 소유권 검증
	 */
	@Before("@annotation(validateOwnership)")
	public void validateResourceOwnership(JoinPoint joinPoint, ValidateOwnership validateOwnership) {
		HttpServletRequest request = getCurrentRequest();
		String userId = request.getHeader(HEADER_USER_ID);
		
		if (userId == null || userId.isBlank()) {
			throw InvalidRequestException.headerMissing(HEADER_USER_ID);
		}
		
		String placeId = extractPlaceId(joinPoint, validateOwnership.placeIdParam());
		
		if (placeId != null) {
			PlaceInfo placeInfo = placeInfoRepository.findById(IdParser.parsePlaceId(placeId))
					.orElseThrow(PlaceNotFoundException::new);
			
			if (!placeInfo.getUserId().equals(userId)) {
				throw ForbiddenException.notOwner();
			}
			
			log.debug("소유권 검증 통과: placeId={}, userId={}", placeId, userId);
		}
	}
	
	private HttpServletRequest getCurrentRequest() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attrs == null) {
			throw new IllegalStateException("요청 컨텍스트를 찾을 수 없습니다.");
		}
		return attrs.getRequest();
	}
	
	private void validateRequiredHeader(String headerValue, String headerName) {
		if (headerValue == null || headerValue.isBlank()) {
			throw InvalidRequestException.headerMissing(headerName);
		}
	}
	
	private AppType parseAppType(String appTypeHeader) {
		validateRequiredHeader(appTypeHeader, HEADER_APP_TYPE);
		try {
			return AppType.valueOf(appTypeHeader);
		} catch (IllegalArgumentException e) {
			throw InvalidRequestException.invalidFormat(HEADER_APP_TYPE);
		}
	}
	
	private void validatePlaceManagerApp(AppType appType) {
		if (appType != AppType.PLACE_MANAGER) {
			throw ForbiddenException.placeManagerOnly();
		}
	}
	
	private String extractPlaceId(JoinPoint joinPoint, String paramName) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Parameter[] parameters = signature.getMethod().getParameters();
		Object[] args = joinPoint.getArgs();
		
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].getName().equals(paramName)) {
				return args[i] != null ? args[i].toString() : null;
			}
		}
		return null;
	}
}
