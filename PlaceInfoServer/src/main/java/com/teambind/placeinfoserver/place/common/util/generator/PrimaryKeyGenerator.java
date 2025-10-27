package com.teambind.placeinfoserver.place.common.util.generator;

import org.springframework.stereotype.Component;

@Component
public interface PrimaryKeyGenerator {
	String generateKey();
}
