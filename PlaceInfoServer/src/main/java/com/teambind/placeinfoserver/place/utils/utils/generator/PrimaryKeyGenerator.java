package com.teambind.placeinfoserver.place.utils.utils.generator;

import org.springframework.stereotype.Component;

@Component
public interface PrimaryKeyGenerator {
  String generateKey();
}
