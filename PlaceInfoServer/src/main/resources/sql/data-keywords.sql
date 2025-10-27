-- =============================================
-- 키워드 마스터 데이터 초기화
-- =============================================
-- 업체가 선택할 수 있는 사전 정의된 키워드 목록
-- =============================================

-- 기존 데이터 삭제 (주의: 운영 환경에서는 사용하지 않음)
-- TRUNCATE TABLE keywords RESTART IDENTITY CASCADE;

-- =============================================
-- 1. 공간 유형 (SPACE_TYPE) 키워드
-- =============================================
INSERT INTO keywords (name, type, description, display_order, is_active)
VALUES ('합주실', 'SPACE_TYPE', '밴드 합주를 위한 전용 공간', 1, true),
       ('연습실', 'SPACE_TYPE', '개인 또는 그룹 연습 공간', 2, true),
       ('레슨실', 'SPACE_TYPE', '1:1 또는 소규모 레슨 전용 공간', 3, true),
       ('녹음실', 'SPACE_TYPE', '음원 녹음 및 믹싱 공간', 4, true),
       ('공연연습실', 'SPACE_TYPE', '공연 리허설 전용 공간', 5, true),
       ('버스킹 준비 공간', 'SPACE_TYPE', '버스킹 공연 준비를 위한 공간', 6, true),
       ('보컬 연습실', 'SPACE_TYPE', '보컬 전용 연습 공간', 7, true),
       ('댄스 연습실', 'SPACE_TYPE', '댄스 연습 전용 공간', 8, true),
       ('스튜디오', 'SPACE_TYPE', '다목적 스튜디오 공간', 9, true),
       ('라이브 공연장', 'SPACE_TYPE', '라이브 공연이 가능한 공간', 10, true)
ON CONFLICT
    (name, type)
DO UPDATE SET
    description = EXCLUDED.description,
    display_order = EXCLUDED.display_order,
    is_active = EXCLUDED.is_active;

-- =============================================
-- 2. 악기/장비 (INSTRUMENT_EQUIPMENT) 키워드
-- =============================================
INSERT INTO keywords (name, type, description, display_order, is_active)
VALUES ('그랜드 피아노', 'INSTRUMENT_EQUIPMENT', '그랜드 피아노 보유', 11, true),
       ('업라이트 피아노', 'INSTRUMENT_EQUIPMENT', '업라이트 피아노 보유', 12, true),
       ('디지털 피아노', 'INSTRUMENT_EQUIPMENT', '디지털 피아노/키보드 보유', 13, true),
       ('드럼 세트', 'INSTRUMENT_EQUIPMENT', '드럼 세트 완비', 14, true),
       ('일렉기타 앰프', 'INSTRUMENT_EQUIPMENT', '일렉기타 앰프 보유', 15, true),
       ('베이스 앰프', 'INSTRUMENT_EQUIPMENT', '베이스 앰프 보유', 16, true),
       ('PA 시스템', 'INSTRUMENT_EQUIPMENT', 'PA 시스템 완비', 17, true),
       ('오디오 인터페이스', 'INSTRUMENT_EQUIPMENT', '레코딩용 오디오 인터페이스 보유', 18, true),
       ('보컬 마이크', 'INSTRUMENT_EQUIPMENT', '전문 보컬 마이크 보유', 19, true),
       ('믹싱 콘솔', 'INSTRUMENT_EQUIPMENT', '믹싱 콘솔 보유', 20, true),
       ('모니터 스피커', 'INSTRUMENT_EQUIPMENT', '스튜디오 모니터 스피커 보유', 21, true),
       ('마이크 세트', 'INSTRUMENT_EQUIPMENT', '다양한 마이크 세트 보유', 22, true),
       ('신디사이저', 'INSTRUMENT_EQUIPMENT', '신디사이저 보유', 23, true),
       ('어쿠스틱 기타', 'INSTRUMENT_EQUIPMENT', '어쿠스틱 기타 대여 가능', 24, true),
       ('캐비닛', 'INSTRUMENT_EQUIPMENT', '기타/베이스 캐비닛 보유', 25, true)
ON CONFLICT
    (name, type)
DO UPDATE SET
    description = EXCLUDED.description,
    display_order = EXCLUDED.display_order,
    is_active = EXCLUDED.is_active;

-- =============================================
-- 3. 편의시설 (AMENITY) 키워드
-- =============================================
INSERT INTO keywords (name, type, description, display_order, is_active)
VALUES ('주차 가능', 'AMENITY', '자체 주차장 또는 인근 주차 가능', 26, true),
       ('화장실 있음', 'AMENITY', '내부 화장실 보유', 27, true),
       ('냉난방 완비', 'AMENITY', '에어컨 및 난방 시설 완비', 28, true),
       ('방음 시설', 'AMENITY', '전문 방음 시설 완비', 29, true),
       ('휴게 공간', 'AMENITY', '별도 휴게 공간 보유', 30, true),
       ('와이파이 제공', 'AMENITY', '무료 와이파이 제공', 31, true),
       ('음료 제공', 'AMENITY', '무료 음료 제공', 32, true),
       ('샤워실', 'AMENITY', '샤워 시설 보유', 33, true),
       ('락커룸', 'AMENITY', '개인 보관함 제공', 34, true),
       ('카페테리아', 'AMENITY', '카페 또는 식음료 공간', 35, true),
       ('흡연실', 'AMENITY', '별도 흡연 공간 보유', 36, true),
       ('엘리베이터', 'AMENITY', '엘리베이터 이용 가능', 37, true),
       ('장애인 편의시설', 'AMENITY', '장애인 접근 가능 시설', 38, true),
       ('탈의실', 'AMENITY', '탈의실 보유', 39, true),
       ('대기실', 'AMENITY', '별도 대기 공간 보유', 40, true)
ON CONFLICT
    (name, type)
DO UPDATE SET
    description = EXCLUDED.description,
    display_order = EXCLUDED.display_order,
    is_active = EXCLUDED.is_active;

-- =============================================
-- 4. 기타 특성 (OTHER_FEATURE) 키워드
-- =============================================
INSERT INTO keywords (name, type, description, display_order, is_active)
VALUES ('저렴한 가격', 'OTHER_FEATURE', '합리적인 가격대', 41, true),
       ('프라이빗 공간', 'OTHER_FEATURE', '독립된 프라이빗 공간', 42, true),
       ('교통 편리', 'OTHER_FEATURE', '대중교통 접근성 우수', 43, true),
       ('신축 시설', 'OTHER_FEATURE', '최근 신축 또는 리모델링', 44, true),
       ('넓은 공간', 'OTHER_FEATURE', '여유로운 공간 규모', 45, true),
       ('깔끔한 인테리어', 'OTHER_FEATURE', '모던하고 깔끔한 인테리어', 46, true),
       ('24시간 운영', 'OTHER_FEATURE', '24시간 이용 가능', 47, true),
       ('심야 이용 가능', 'OTHER_FEATURE', '심야 시간대 이용 가능', 48, true),
       ('조기 오픈', 'OTHER_FEATURE', '이른 아침부터 운영', 49, true),
       ('예약 필수', 'OTHER_FEATURE', '사전 예약 필수', 50, true),
       ('당일 예약 가능', 'OTHER_FEATURE', '당일 예약 가능', 51, true),
       ('장기 대여 가능', 'OTHER_FEATURE', '장기 대여 할인 제공', 52, true),
       ('초보자 환영', 'OTHER_FEATURE', '초보자 친화적 공간', 53, true),
       ('전문가 상주', 'OTHER_FEATURE', '전문 엔지니어/강사 상주', 54, true),
       ('녹음 가능', 'OTHER_FEATURE', '간단한 녹음 작업 가능', 55, true),
       ('라이브 스트리밍 가능', 'OTHER_FEATURE', '라이브 방송 장비 보유', 56, true),
       ('영상 촬영 가능', 'OTHER_FEATURE', '영상 촬영 장비 및 공간', 57, true),
       ('소음 제한 없음', 'OTHER_FEATURE', '시간대별 소음 제한 없음', 58, true),
       ('반려동물 동반 가능', 'OTHER_FEATURE', '반려동물과 함께 이용 가능', 59, true),
       ('단체 할인', 'OTHER_FEATURE', '단체 이용시 할인 제공', 60, true)
ON CONFLICT
    (name, type)
DO UPDATE SET
    description = EXCLUDED.description,
    display_order = EXCLUDED.display_order,
    is_active = EXCLUDED.is_active;

-- =============================================
-- 통계 정보 확인
-- =============================================
SELECT type,
       COUNT(*) as count
FROM keywords
WHERE is_active = true
GROUP BY type
ORDER BY type;
