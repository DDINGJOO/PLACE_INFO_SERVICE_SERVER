package com.teambind.placeinfoserver.place.config;

import com.teambind.placeinfoserver.place.utils.utils.generator.PrimaryKeyGenerator;
import com.teambind.placeinfoserver.place.utils.utils.generator.impl.Snowflake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CustomConfig {
	
	@Bean
	public PrimaryKeyGenerator PkeyGenerator() {
		return new Snowflake();
	}
}
