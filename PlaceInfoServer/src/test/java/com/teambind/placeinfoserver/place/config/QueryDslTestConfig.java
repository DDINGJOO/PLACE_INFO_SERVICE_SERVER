package com.teambind.placeinfoserver.place.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * QueryDSL 테스트 설정
 *
 * @DataJpaTest에서 JPAQueryFactory 빈을 제공
 */
@TestConfiguration
public class QueryDslTestConfig {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Bean
	public JPAQueryFactory jpaQueryFactory() {
		return new JPAQueryFactory(entityManager);
	}
}
