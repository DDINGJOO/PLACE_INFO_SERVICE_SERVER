-- =============================================
-- Place Info Service - Entity-based Database Migration
-- =============================================
-- 이 마이그레이션은 JPA 엔티티를 기준으로 테이블을 수정합니다.
-- 실행 전 백업을 권장합니다.
-- =============================================

-- PostGIS Extension 활성화 (이미 존재하지 않는 경우에만)
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- =============================================
-- 1. place_parkings 테이블 수정
-- =============================================
-- 엔티티에 없는 필드들 제거
ALTER TABLE place_parkings
  DROP COLUMN IF EXISTS hourly_rate,
  DROP COLUMN IF EXISTS daily_max_rate,
  DROP COLUMN IF EXISTS parking_lot_type,
  DROP COLUMN IF EXISTS valet_available,
  DROP COLUMN IF EXISTS disabled_parking_available;

-- =============================================
-- 2. place_locations 테이블 - coordinates 타입 확인
-- =============================================
-- JTS Point를 위한 geography 타입이 이미 올바르게 설정되어 있음
-- geography(Point, 4326) 타입은 JTS Point와 호환됨

-- =============================================
-- 3. 엔티티와 일치하도록 테이블 구조 확인 및 수정
-- =============================================

-- place_info 테이블 - 이미 엔티티와 일치
-- 변경 사항 없음

-- place_contacts 테이블 - 이미 엔티티와 일치
-- 변경 사항 없음

-- place_websites 테이블 - 이미 엔티티와 일치 (ElementCollection으로 매핑됨)
-- 변경 사항 없음

-- place_social_links 테이블 - 이미 엔티티와 일치 (ElementCollection으로 매핑됨)
-- 변경 사항 없음

-- place_images 테이블 - 이미 엔티티와 일치
-- id는 VARCHAR(255)로 이미지 서버의 ID를 저장
-- 변경 사항 없음

-- keywords 테이블 - 이미 엔티티와 일치
-- 변경 사항 없음

-- place_keywords 매핑 테이블 - 이미 엔티티와 일치 (@ManyToMany로 매핑됨)
-- 변경 사항 없음

-- =============================================
-- 4. 엔티티 기반 새로운 테이블 구조 확인 스크립트
-- =============================================
-- 다음은 현재 엔티티 기반의 정확한 테이블 구조입니다.

-- place_info 테이블 구조 확인
DO $$
BEGIN
    -- place_info 테이블 확인
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'place_info') THEN
        CREATE TABLE place_info (
            id BIGSERIAL PRIMARY KEY,
            user_id VARCHAR(100) NOT NULL,
            place_name VARCHAR(100) NOT NULL,
            description VARCHAR(500),
            category VARCHAR(50),
            place_type VARCHAR(50),
            is_active BOOLEAN NOT NULL DEFAULT TRUE,
            approval_status VARCHAR(20) DEFAULT 'PENDING' CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED')),
            rating_average DOUBLE PRECISION,
            review_count INTEGER DEFAULT 0,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );
    END IF;
END $$;

-- place_locations 테이블 구조 확인
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'place_locations') THEN
        CREATE TABLE place_locations (
            id BIGSERIAL PRIMARY KEY,
            place_id BIGINT NOT NULL UNIQUE,
            province VARCHAR(50),
            city VARCHAR(50),
            district VARCHAR(50),
            full_address VARCHAR(500) NOT NULL,
            address_detail VARCHAR(200),
            postal_code VARCHAR(10),
            coordinates geography(Point, 4326),
            latitude DOUBLE PRECISION,
            longitude DOUBLE PRECISION,
            location_guide VARCHAR(500),
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_place_location_place FOREIGN KEY (place_id)
                REFERENCES place_info(id) ON DELETE CASCADE
        );
    END IF;
END $$;

-- place_contacts 테이블 구조 확인
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'place_contacts') THEN
        CREATE TABLE place_contacts (
            id BIGSERIAL PRIMARY KEY,
            place_id BIGINT NOT NULL UNIQUE,
            contact VARCHAR(20),
            email VARCHAR(100),
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_place_contact_place FOREIGN KEY (place_id)
                REFERENCES place_info(id) ON DELETE CASCADE
        );
    END IF;
END $$;

-- place_parkings 테이블 구조 확인 (엔티티 기준)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'place_parkings') THEN
        CREATE TABLE place_parkings (
            id BIGSERIAL PRIMARY KEY,
            place_id BIGINT NOT NULL UNIQUE,
            available BOOLEAN NOT NULL DEFAULT FALSE,
            parking_type VARCHAR(10) CHECK (parking_type IN ('FREE', 'PAID')),
            capacity INTEGER,
            description VARCHAR(500),
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT fk_place_parking_place FOREIGN KEY (place_id)
                REFERENCES place_info(id) ON DELETE CASCADE
        );
    END IF;
END $$;

-- place_images 테이블 구조 확인
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'place_images') THEN
        CREATE TABLE place_images (
            id VARCHAR(255) PRIMARY KEY,
            place_id BIGINT NOT NULL,
            image_url VARCHAR(500) NOT NULL,
            CONSTRAINT fk_place_images_place FOREIGN KEY (place_id)
                REFERENCES place_info(id) ON DELETE CASCADE
        );
    END IF;
END $$;

-- keywords 테이블 구조 확인
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'keywords') THEN
        CREATE TABLE keywords (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(50) NOT NULL,
            type VARCHAR(30) NOT NULL CHECK (type IN ('SPACE_TYPE', 'INSTRUMENT_EQUIPMENT', 'AMENITY', 'OTHER_FEATURE')),
            description VARCHAR(200),
            display_order INTEGER,
            is_active BOOLEAN DEFAULT TRUE,
            CONSTRAINT uk_keywords_name_type UNIQUE (name, type)
        );
    END IF;
END $$;

-- place_keywords 매핑 테이블 구조 확인
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'place_keywords') THEN
        CREATE TABLE place_keywords (
            place_id BIGINT NOT NULL,
            keyword_id BIGINT NOT NULL,
            PRIMARY KEY (place_id, keyword_id),
            CONSTRAINT fk_place_keywords_place FOREIGN KEY (place_id)
                REFERENCES place_info(id) ON DELETE CASCADE,
            CONSTRAINT fk_place_keywords_keyword FOREIGN KEY (keyword_id)
                REFERENCES keywords(id) ON DELETE CASCADE
        );
    END IF;
END $$;

-- place_websites 테이블 구조 확인 (PlaceContact의 ElementCollection)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'place_websites') THEN
        CREATE TABLE place_websites (
            contact_id BIGINT NOT NULL,
            website_url VARCHAR(500) NOT NULL,
            display_order INTEGER NOT NULL,
            CONSTRAINT fk_place_websites_contact FOREIGN KEY (contact_id)
                REFERENCES place_contacts(id) ON DELETE CASCADE,
            PRIMARY KEY (contact_id, display_order)
        );
    END IF;
END $$;

-- place_social_links 테이블 구조 확인 (PlaceContact의 ElementCollection)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'place_social_links') THEN
        CREATE TABLE place_social_links (
            contact_id BIGINT NOT NULL,
            social_url VARCHAR(500) NOT NULL,
            display_order INTEGER NOT NULL,
            CONSTRAINT fk_place_social_links_contact FOREIGN KEY (contact_id)
                REFERENCES place_contacts(id) ON DELETE CASCADE,
            PRIMARY KEY (contact_id, display_order)
        );
    END IF;
END $$;

-- =============================================
-- 5. 인덱스 재생성 (필요한 경우)
-- =============================================

-- place_info 인덱스
CREATE INDEX IF NOT EXISTS idx_place_info_user_id ON place_info(user_id);
CREATE INDEX IF NOT EXISTS idx_place_info_approval_status ON place_info(approval_status);
CREATE INDEX IF NOT EXISTS idx_place_info_is_active ON place_info(is_active);
CREATE INDEX IF NOT EXISTS idx_place_info_category ON place_info(category);
CREATE INDEX IF NOT EXISTS idx_place_info_place_type ON place_info(place_type);

-- place_locations 인덱스
CREATE INDEX IF NOT EXISTS idx_place_locations_place_id ON place_locations(place_id);
CREATE INDEX IF NOT EXISTS idx_place_locations_coordinates ON place_locations USING GIST(coordinates);
CREATE INDEX IF NOT EXISTS idx_place_locations_lat_lng ON place_locations(latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_place_locations_full_address ON place_locations(full_address);
CREATE INDEX IF NOT EXISTS idx_place_locations_province_city ON place_locations(province, city);

-- place_contacts 인덱스
CREATE INDEX IF NOT EXISTS idx_place_contacts_place_id ON place_contacts(place_id);

-- place_parkings 인덱스
CREATE INDEX IF NOT EXISTS idx_place_parkings_place_id ON place_parkings(place_id);
CREATE INDEX IF NOT EXISTS idx_place_parkings_available ON place_parkings(available);
CREATE INDEX IF NOT EXISTS idx_place_parkings_parking_type ON place_parkings(parking_type);

-- place_images 인덱스
CREATE INDEX IF NOT EXISTS idx_place_images_place_id ON place_images(place_id);

-- keywords 인덱스
CREATE INDEX IF NOT EXISTS idx_keywords_type ON keywords(type);
CREATE INDEX IF NOT EXISTS idx_keywords_is_active ON keywords(is_active);
CREATE INDEX IF NOT EXISTS idx_keywords_display_order ON keywords(display_order);

-- place_keywords 인덱스
CREATE INDEX IF NOT EXISTS idx_place_keywords_place_id ON place_keywords(place_id);
CREATE INDEX IF NOT EXISTS idx_place_keywords_keyword_id ON place_keywords(keyword_id);

-- place_websites 인덱스
CREATE INDEX IF NOT EXISTS idx_place_websites_contact_id ON place_websites(contact_id);

-- place_social_links 인덱스
CREATE INDEX IF NOT EXISTS idx_place_social_links_contact_id ON place_social_links(contact_id);

-- =============================================
-- 6. 트리거 함수 재생성
-- =============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 트리거 재생성 (필요한 경우)
DROP TRIGGER IF EXISTS update_place_info_updated_at ON place_info;
CREATE TRIGGER update_place_info_updated_at BEFORE UPDATE ON place_info
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_place_locations_updated_at ON place_locations;
CREATE TRIGGER update_place_locations_updated_at BEFORE UPDATE ON place_locations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_place_contacts_updated_at ON place_contacts;
CREATE TRIGGER update_place_contacts_updated_at BEFORE UPDATE ON place_contacts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_place_parkings_updated_at ON place_parkings;
CREATE TRIGGER update_place_parkings_updated_at BEFORE UPDATE ON place_parkings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- 7. 초기 키워드 데이터 삽입 (존재하지 않는 경우)
-- =============================================
INSERT INTO keywords (name, type, description, display_order, is_active)
SELECT * FROM (VALUES
    -- 공간 유형
    ('합주실', 'SPACE_TYPE', '밴드 합주 연습 공간', 1, true),
    ('연습실', 'SPACE_TYPE', '개인/그룹 연습 공간', 2, true),
    ('레슨실', 'SPACE_TYPE', '음악 레슨 전용 공간', 3, true),
    ('녹음실', 'SPACE_TYPE', '녹음 전문 공간', 4, true),
    ('공연연습실', 'SPACE_TYPE', '공연 리허설 공간', 5, true),
    ('버스킹 준비 공간', 'SPACE_TYPE', '버스킹 준비 및 연습 공간', 6, true),

    -- 악기/장비
    ('그랜드 피아노', 'INSTRUMENT_EQUIPMENT', '그랜드 피아노 보유', 11, true),
    ('업라이트 피아노', 'INSTRUMENT_EQUIPMENT', '업라이트 피아노 보유', 12, true),
    ('드럼 세트', 'INSTRUMENT_EQUIPMENT', '드럼 세트 보유', 13, true),
    ('일렉기타 앰프', 'INSTRUMENT_EQUIPMENT', '일렉기타 앰프 보유', 14, true),
    ('베이스 앰프', 'INSTRUMENT_EQUIPMENT', '베이스 앰프 보유', 15, true),
    ('PA 시스템', 'INSTRUMENT_EQUIPMENT', 'PA 시스템 보유', 16, true),
    ('오디오 인터페이스', 'INSTRUMENT_EQUIPMENT', '레코딩용 오디오 인터페이스', 17, true),
    ('보컬 마이크', 'INSTRUMENT_EQUIPMENT', '보컬용 마이크 보유', 18, true),

    -- 편의시설
    ('주차 가능', 'AMENITY', '주차 시설 이용 가능', 21, true),
    ('화장실 있음', 'AMENITY', '화장실 시설 보유', 22, true),
    ('냉난방 완비', 'AMENITY', '냉난방 시설 완비', 23, true),
    ('방음 시설', 'AMENITY', '방음 처리 완료', 24, true),
    ('휴게 공간', 'AMENITY', '휴게 공간 보유', 25, true),
    ('와이파이 제공', 'AMENITY', '무료 와이파이 제공', 26, true),

    -- 기타 특성
    ('저렴한 가격', 'OTHER_FEATURE', '합리적인 가격', 31, true),
    ('프라이빗 공간', 'OTHER_FEATURE', '독립된 개인 공간', 32, true),
    ('교통 편리', 'OTHER_FEATURE', '대중교통 접근성 좋음', 33, true),
    ('신축 시설', 'OTHER_FEATURE', '최근 신축/리모델링', 34, true),
    ('넓은 공간', 'OTHER_FEATURE', '넓은 연습 공간', 35, true),
    ('깔끔한 인테리어', 'OTHER_FEATURE', '깔끔한 인테리어', 36, true)
) AS v(name, type, description, display_order, is_active)
WHERE NOT EXISTS (
    SELECT 1 FROM keywords k
    WHERE k.name = v.name AND k.type = v.type
);

-- =============================================
-- 8. 마이그레이션 완료 메시지
-- =============================================
DO $$
BEGIN
    RAISE NOTICE '엔티티 기반 마이그레이션이 완료되었습니다.';
    RAISE NOTICE '변경사항:';
    RAISE NOTICE '1. place_parkings 테이블에서 불필요한 컬럼 제거';
    RAISE NOTICE '2. 모든 테이블이 JPA 엔티티와 일치하도록 수정됨';
END $$;