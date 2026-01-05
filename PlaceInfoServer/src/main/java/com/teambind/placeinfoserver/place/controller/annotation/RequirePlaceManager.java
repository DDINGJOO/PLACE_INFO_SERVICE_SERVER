package com.teambind.placeinfoserver.place.controller.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PLACE_MANAGER 앱 타입과 사용자 ID를 필수로 요구하는 API에 적용
 * X-App-Type: PLACE_MANAGER, X-User-Id 헤더 검증
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePlaceManager {
}
