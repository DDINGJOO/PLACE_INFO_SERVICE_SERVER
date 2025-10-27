package com.teambind.placeinfoserver;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires full infrastructure (PostgreSQL, Kafka, Redis) to run")
class PlaceInfoServerApplicationTests {
	
	@Test
	void contextLoads() {
	}
	
}
