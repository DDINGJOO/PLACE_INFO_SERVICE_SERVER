package com.teambind.placeinfoserver.place.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers 설정
 * PostgreSQL + PostGIS 컨테이너를 사용한 통합 테스트
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfig {
	
	private static final PostgreSQLContainer<?> postgresContainer;
	
	static {
		postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgis/postgis:15-3.4"))
				.withDatabaseName("testdb")
				.withUsername("test")
				.withPassword("test")
				.withReuse(true); // 테스트 간 컨테이너 재사용으로 속도 향상
		postgresContainer.start();
	}
	
	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgresContainer::getUsername);
		registry.add("spring.datasource.password", postgresContainer::getPassword);
	}
	
	@Bean
	public PostgreSQLContainer<?> postgresContainer() {
		return postgresContainer;
	}
}
