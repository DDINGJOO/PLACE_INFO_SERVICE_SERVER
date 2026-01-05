package com.teambind.placeinfoserver.place.controller.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 리소스 소유권 검증이 필요한 API에 적용
 * placeId 파라미터와 X-User-Id 헤더를 비교하여 소유권 확인
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateOwnership {

	/**
	 * placeId 파라미터명 (기본값: "placeId")
	 */
	String placeIdParam() default "placeId";
}
