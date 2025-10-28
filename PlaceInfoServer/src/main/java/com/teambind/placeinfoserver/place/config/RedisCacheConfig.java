package com.teambind.placeinfoserver.place.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 캐싱 설정
 * 공간 탐색 성능 최적화를 위한 캐시 구성
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

	/**
	 * 캐시 매니저 설정
	 */
	@Bean
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		// 기본 캐시 설정
		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofMinutes(10)) // 기본 TTL: 10분
				.disableCachingNullValues()
				.serializeKeysWith(RedisSerializationContext.SerializationPair
						.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair
						.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper())));

		// 캐시별 개별 설정
		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

		// 위치 기반 검색 캐시 (5분 TTL)
		cacheConfigurations.put("placeLocationSearch",
				defaultConfig.entryTtl(Duration.ofMinutes(5)));

		// 인기 장소 캐시 (30분 TTL)
		cacheConfigurations.put("popularPlaces",
				defaultConfig.entryTtl(Duration.ofMinutes(30)));

		// 키워드 검색 캐시 (10분 TTL)
		cacheConfigurations.put("keywordSearch",
				defaultConfig.entryTtl(Duration.ofMinutes(10)));

		// 지역별 검색 캐시 (15분 TTL)
		cacheConfigurations.put("regionSearch",
				defaultConfig.entryTtl(Duration.ofMinutes(15)));

		// 장소 상세 정보 캐시 (1시간 TTL)
		cacheConfigurations.put("placeDetails",
				defaultConfig.entryTtl(Duration.ofHours(1)));

		// 키워드 목록 캐시 (1일 TTL)
		cacheConfigurations.put("keywords",
				defaultConfig.entryTtl(Duration.ofDays(1)));

		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(defaultConfig)
				.withInitialCacheConfigurations(cacheConfigurations)
				.transactionAware()
				.build();
	}

	/**
	 * JSON 직렬화를 위한 ObjectMapper 설정
	 */
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());

		// 타입 정보 포함 (역직렬화를 위해)
		BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
				.allowIfSubType(Object.class)
				.build();

		mapper.activateDefaultTyping(
				typeValidator,
				ObjectMapper.DefaultTyping.NON_FINAL,
				JsonTypeInfo.As.PROPERTY
		);

		return mapper;
	}
}