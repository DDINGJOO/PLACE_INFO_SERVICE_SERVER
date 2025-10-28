package com.teambind.placeinfoserver.place.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 모든 통합 테스트가 상속받을 베이스 클래스
 * PostgreSQL + PostGIS Testcontainer를 설정
 * Redis Testcontainer를 설정
 * JPA Auditing을 활성화하여 BaseEntity의 createdAt, updatedAt 자동 설정
 */
@Import(JpaAuditingTestConfig.class)
public abstract class BaseIntegrationTest {

	private static final PostgreSQLContainer<?> postgresContainer;
	private static final RedisContainer redisContainer;

	static {
		postgresContainer = new PostgreSQLContainer<>(
				DockerImageName.parse("postgis/postgis:15-3.3")
						.asCompatibleSubstituteFor("postgres")
		)
				.withDatabaseName("testdb")
				.withUsername("test")
				.withPassword("test")
				.withReuse(true); // 테스트 간 컨테이너 재사용으로 속도 향상
		postgresContainer.start();

		redisContainer = new RedisContainer(
				DockerImageName.parse("redis:7.2-alpine")
		)
				.withReuse(true); // 테스트 간 컨테이너 재사용으로 속도 향상
		redisContainer.start();
	}

	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgresContainer::getUsername);
		registry.add("spring.datasource.password", postgresContainer::getPassword);

		registry.add("spring.data.redis.host", redisContainer::getHost);
		registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
	}
}
