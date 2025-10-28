package com.teambind.placeinfoserver.place.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 테스트용 JPA Auditing 설정
 * BaseEntity의 createdAt, updatedAt 자동 설정을 위해 필요
 */
@TestConfiguration
@EnableJpaAuditing(dateTimeProviderRef = "testDateTimeProvider")
public class JpaAuditingTestConfig {
	
	@Bean
	public DateTimeProvider testDateTimeProvider() {
		return () -> Optional.of(LocalDateTime.now());
	}
}
