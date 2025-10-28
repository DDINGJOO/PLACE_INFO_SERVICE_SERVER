-- =============================================
-- PlaceInfoServer Sample Test Data
-- =============================================
-- 테스트 및 개발 환경용 샘플 데이터
-- =============================================

-- 샘플 장소 정보 데이터 삽입
INSERT INTO place_info (id,
                        user_id,
                        place_name,
                        description,
                        category,
                        place_type,
                        is_active,
                        approval_status,
                        rating_average,
                        review_count)
VALUES ('1832654789123456789', 'user_001', '강남 뮤직 스튜디오',
        '최고의 음향 시설을 갖춘 전문 합주실입니다. 다양한 장비를 보유하고 있으며, 녹음 작업도 가능합니다.',
        '합주실', '음악', true, 'APPROVED', 4.5, 23),

       ('1832654789123456790', 'user_002', '홍대 라이브 하우스',
        '라이브 공연이 가능한 공간으로, 소규모 콘서트나 버스킹 준비에 적합합니다.',
        '공연장', '음악', true, 'APPROVED', 4.8, 15),

       ('1832654789123456791', 'user_003', '서초 댄스 아카데미',
        '넓은 거울과 최신 음향 시설을 갖춘 댄스 전문 연습실입니다.',
        '연습실', '댄스', true, 'PENDING', null, 0),

       ('1832654789123456792', 'user_004', '마포 레코딩 스튜디오',
        '전문 엔지니어가 상주하는 본격 레코딩 스튜디오입니다. 믹싱/마스터링 서비스도 제공합니다.',
        '녹음실', '음악', false, 'APPROVED', 4.9, 8);

-- 샘플 연락처 정보
INSERT INTO place_contacts (place_info_id,
                            contact,
                            email)
VALUES ('1832654789123456789', '0212345678', 'contact@gangnam-music.com'),
       ('1832654789123456790', '0298765432', 'info@hongdae-live.com'),
       ('1832654789123456791', '0234567890', 'dance@seocho.com'),
       ('1832654789123456792', '0245678901', 'recording@mapo.studio');

-- 샘플 웹사이트 정보
INSERT INTO place_websites (place_contact_id,
                            websites,
                            websites_order)
VALUES (1, 'https://www.gangnam-music.com', 0),
       (1, 'https://booking.gangnam-music.com', 1),
       (2, 'https://www.hongdae-live.com', 0),
       (3, 'https://seocho-dance.com', 0);

-- 샘플 소셜 링크 정보
INSERT INTO place_social_links (place_contact_id,
                                social_links,
                                social_links_order)
VALUES (1, 'https://instagram.com/gangnam_music', 0),
       (1, 'https://facebook.com/gangnammusic', 1),
       (2, 'https://instagram.com/hongdae_live', 0),
       (3, 'https://instagram.com/seocho_dance', 0),
       (3, 'https://youtube.com/@seochodance', 1);

-- 샘플 위치 정보 (강남, 홍대, 서초, 마포 지역)
INSERT INTO place_locations (place_info_id,
                             province,
                             city,
                             district,
                             full_address,
                             address_detail,
                             postal_code,
                             coordinates,
                             latitude,
                             longitude,
                             location_guide)
VALUES ('1832654789123456789', '서울특별시', '강남구', '역삼동',
        '서울특별시 강남구 역삼동 테헤란로 123', '뮤직빌딩 3층',
        '06234',
        ST_SetSRID(ST_MakePoint(127.0392, 37.5013), 4326)::geography,
        37.5013, 127.0392,
        '역삼역 3번 출구에서 도보 5분'),

       ('1832654789123456790', '서울특별시', '마포구', '서교동',
        '서울특별시 마포구 서교동 와우산로 145', 'B1층',
        '04053',
        ST_SetSRID(ST_MakePoint(126.9227, 37.5563), 4326)::geography,
        37.5563, 126.9227,
        '홍대입구역 9번 출구에서 도보 7분'),

       ('1832654789123456791', '서울특별시', '서초구', '서초동',
        '서울특별시 서초구 서초동 서초대로 234', '댄스빌딩 5층',
        '06587',
        ST_SetSRID(ST_MakePoint(127.0074, 37.4955), 4326)::geography,
        37.4955, 127.0074,
        '교대역 1번 출구에서 도보 3분'),

       ('1832654789123456792', '서울특별시', '마포구', '상수동',
        '서울특별시 마포구 상수동 독막로 78', '지하 1층',
        '04072',
        ST_SetSRID(ST_MakePoint(126.9145, 37.5478), 4326)::geography,
        37.5478, 126.9145,
        '상수역 2번 출구에서 도보 10분');

-- 샘플 주차 정보
INSERT INTO place_parkings (place_info_id,
                            available,
                            parking_type,
                            description)
VALUES ('1832654789123456789', true, 'PAID', '건물 지하 주차장 이용 가능. 시간당 3,000원'),
       ('1832654789123456790', false, null, '주차 불가. 인근 공영주차장 이용 권장'),
       ('1832654789123456791', true, 'FREE', '2시간 무료 주차 가능. 이후 시간당 2,000원'),
       ('1832654789123456792', true, 'FREE', '고객 전용 무료 주차 3대 가능');

-- 샘플 이미지 정보
INSERT INTO place_images (id,
                          place_info_id,
                          image_url,
                          display_order)
VALUES ('img_001_main', '1832654789123456789', 'https://images.example.com/gangnam/main.jpg', 0),
       ('img_001_room1', '1832654789123456789', 'https://images.example.com/gangnam/room1.jpg', 1),
       ('img_001_room2', '1832654789123456789', 'https://images.example.com/gangnam/room2.jpg', 2),
       ('img_002_main', '1832654789123456790', 'https://images.example.com/hongdae/main.jpg', 0),
       ('img_002_stage', '1832654789123456790', 'https://images.example.com/hongdae/stage.jpg', 1),
       ('img_003_main', '1832654789123456791', 'https://images.example.com/seocho/main.jpg', 0);

-- 샘플 장소-키워드 매핑
-- 강남 뮤직 스튜디오
INSERT INTO place_keywords (place_id, keyword_id)
SELECT '1832654789123456789', id
FROM keywords
WHERE (name, type) IN (
                       ('합주실', 'SPACE_TYPE'),
                       ('녹음실', 'SPACE_TYPE'),
                       ('드럼 세트', 'INSTRUMENT_EQUIPMENT'),
                       ('일렉기타 앰프', 'INSTRUMENT_EQUIPMENT'),
                       ('베이스 앰프', 'INSTRUMENT_EQUIPMENT'),
                       ('PA 시스템', 'INSTRUMENT_EQUIPMENT'),
                       ('주차 가능', 'AMENITY'),
                       ('냉난방 완비', 'AMENITY'),
                       ('방음 시설', 'AMENITY'),
                       ('교통 편리', 'OTHER_FEATURE')
    );

-- 홍대 라이브 하우스
INSERT INTO place_keywords (place_id, keyword_id)
SELECT '1832654789123456790', id
FROM keywords
WHERE (name, type) IN (
                       ('라이브 공연장', 'SPACE_TYPE'),
                       ('버스킹 준비 공간', 'SPACE_TYPE'),
                       ('PA 시스템', 'INSTRUMENT_EQUIPMENT'),
                       ('보컬 마이크', 'INSTRUMENT_EQUIPMENT'),
                       ('모니터 스피커', 'INSTRUMENT_EQUIPMENT'),
                       ('와이파이 제공', 'AMENITY'),
                       ('음료 제공', 'AMENITY'),
                       ('프라이빗 공간', 'OTHER_FEATURE'),
                       ('라이브 스트리밍 가능', 'OTHER_FEATURE'),
                       ('영상 촬영 가능', 'OTHER_FEATURE')
    );

-- 서초 댄스 아카데미
INSERT INTO place_keywords (place_id, keyword_id)
SELECT '1832654789123456791', id
FROM keywords
WHERE (name, type) IN (
                       ('댄스 연습실', 'SPACE_TYPE'),
                       ('연습실', 'SPACE_TYPE'),
                       ('주차 가능', 'AMENITY'),
                       ('탈의실', 'AMENITY'),
                       ('샤워실', 'AMENITY'),
                       ('냉난방 완비', 'AMENITY'),
                       ('넓은 공간', 'OTHER_FEATURE'),
                       ('깔끔한 인테리어', 'OTHER_FEATURE'),
                       ('초보자 환영', 'OTHER_FEATURE'),
                       ('당일 예약 가능', 'OTHER_FEATURE')
    );

-- 마포 레코딩 스튜디오
INSERT INTO place_keywords (place_id, keyword_id)
SELECT '1832654789123456792', id
FROM keywords
WHERE (name, type) IN (
                       ('녹음실', 'SPACE_TYPE'),
                       ('스튜디오', 'SPACE_TYPE'),
                       ('오디오 인터페이스', 'INSTRUMENT_EQUIPMENT'),
                       ('믹싱 콘솔', 'INSTRUMENT_EQUIPMENT'),
                       ('모니터 스피커', 'INSTRUMENT_EQUIPMENT'),
                       ('마이크 세트', 'INSTRUMENT_EQUIPMENT'),
                       ('방음 시설', 'AMENITY'),
                       ('주차 가능', 'AMENITY'),
                       ('전문가 상주', 'OTHER_FEATURE'),
                       ('예약 필수', 'OTHER_FEATURE')
    );

-- =============================================
-- 검증 쿼리
-- =============================================

-- 장소별 키워드 확인
SELECT pi.place_name,
       STRING_AGG(k.name || ' (' || k.type || ')', ', ' ORDER BY k.display_order) as keywords
FROM place_info pi
         LEFT JOIN place_keywords pk ON pi.id = pk.place_id
         LEFT JOIN keywords k ON pk.keyword_id = k.id
GROUP BY pi.id, pi.place_name;

-- 위치 기반 검색 테스트 (강남역 기준 5km 이내)
SELECT pi.place_name,
       pl.full_address,
       ST_Distance(
               pl.coordinates,
               ST_SetSRID(ST_MakePoint(127.0276, 37.4979), 4326) : :geography
       ) / 1000 as distance_km
FROM place_info pi
         JOIN place_locations pl ON pi.id = pl.place_info_id
WHERE ST_DWithin(
              pl.coordinates,
              ST_SetSRID(ST_MakePoint(127.0276, 37.4979), 4326)::geography,
              5000 -- 5km
      )
ORDER BY distance_km;
