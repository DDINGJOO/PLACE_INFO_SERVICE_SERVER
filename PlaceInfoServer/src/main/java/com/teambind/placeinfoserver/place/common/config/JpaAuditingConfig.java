package com.teambind.placeinfoserver.place.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정
 * BaseEntity의 @CreatedDate, @LastModifiedDate 자동 설정
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
