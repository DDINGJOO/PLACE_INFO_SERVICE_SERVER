package com.teambind.placeinfoserver.place.entity;

/**
 * 키워드 카테고리 타입
 */
public enum KeywordType {
    SPACE_TYPE("공간 유형"),
    INSTRUMENT_EQUIPMENT("악기/장비"),
    AMENITY("편의시설"),
    OTHER_FEATURE("기타 특성");

    private final String description;

    KeywordType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
