-- =============================================
-- Place Info Service - Database Schema
-- =============================================
-- 업체 정보 관리를 위한 데이터베이스 스키마
--
-- 테이블 목록:
-- 1. place_info - 업체 기본 정보
-- 2. place_locations - 업체 위치 정보
-- 3. place_contacts - 업체 연락처 정보
-- 4. place_parkings - 업체 주차 정보
-- 5. place_images - 업체 이미지
-- 6. keywords - 키워드 마스터 데이터
-- 7. place_keywords - 업체-키워드 매핑
-- 8. place_websites - 업체 웹사이트 URL
-- 9. place_social_links - 업체 소셜 링크
-- =============================================

-- PostGIS extension for spatial data (필요한 경우)
-- CREATE EXTENSION IF NOT EXISTS postgis;

-- =============================================
-- 1. 업체 기본 정보 테이블
-- =============================================
CREATE TABLE IF NOT EXISTS place_info (
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

-- 인덱스 생성
CREATE INDEX idx_place_info_user_id ON place_info(user_id);
CREATE INDEX idx_place_info_approval_status ON place_info(approval_status);
CREATE INDEX idx_place_info_is_active ON place_info(is_active);
CREATE INDEX idx_place_info_category ON place_info(category);
CREATE INDEX idx_place_info_place_type ON place_info(place_type);

-- =============================================
-- 2. 업체 위치 정보 테이블
-- =============================================
CREATE TABLE IF NOT EXISTS place_locations (
    id BIGSERIAL PRIMARY KEY,
    place_id BIGINT NOT NULL UNIQUE,
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    full_address VARCHAR(500) NOT NULL,
    address_detail VARCHAR(200),
    postal_code VARCHAR(10),
    coordinates geography(Point, 4326),  -- PostGIS 지리 데이터 타입
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    location_guide VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_place_location_place FOREIGN KEY (place_id)
        REFERENCES place_info(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX idx_place_locations_place_id ON place_locations(place_id);
CREATE INDEX idx_place_locations_coordinates ON place_locations USING GIST(coordinates);
CREATE INDEX idx_place_locations_lat_lng ON place_locations(latitude, longitude);
CREATE INDEX idx_place_locations_full_address ON place_locations(full_address);
CREATE INDEX idx_place_locations_province_city ON place_locations(province, city);

-- =============================================
-- 3. 업체 연락처 정보 테이블
-- =============================================
CREATE TABLE IF NOT EXISTS place_contacts (
    id BIGSERIAL PRIMARY KEY,
    place_id BIGINT NOT NULL UNIQUE,
    contact VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_place_contact_place FOREIGN KEY (place_id)
        REFERENCES place_info(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX idx_place_contacts_place_id ON place_contacts(place_id);

-- =============================================
-- 4. 업체 웹사이트 URL 테이블
-- =============================================
CREATE TABLE IF NOT EXISTS place_websites (
    contact_id BIGINT NOT NULL,
    website_url VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL,
    CONSTRAINT fk_place_websites_contact FOREIGN KEY (contact_id)
        REFERENCES place_contacts(id) ON DELETE CASCADE,
    PRIMARY KEY (contact_id, display_order)
);

-- 인덱스 생성
CREATE INDEX idx_place_websites_contact_id ON place_websites(contact_id);

-- =============================================
-- 5. 업체 소셜 링크 테이블
-- =============================================
CREATE TABLE IF NOT EXISTS place_social_links (
    contact_id BIGINT NOT NULL,
    social_url VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL,
    CONSTRAINT fk_place_social_links_contact FOREIGN KEY (contact_id)
        REFERENCES place_contacts(id) ON DELETE CASCADE,
    PRIMARY KEY (contact_id, display_order)
);

-- 인덱스 생성
CREATE INDEX idx_place_social_links_contact_id ON place_social_links(contact_id);

-- =============================================
-- 6. 업체 주차 정보 테이블
-- =============================================
CREATE TABLE IF NOT EXISTS place_parkings (
    id BIGSERIAL PRIMARY KEY,
    place_id BIGINT NOT NULL UNIQUE,
    available BOOLEAN NOT NULL DEFAULT FALSE,
    parking_type VARCHAR(10) CHECK (parking_type IN ('FREE', 'PAID')),
    hourly_rate INTEGER,
    daily_max_rate INTEGER,
    capacity INTEGER,
    parking_lot_type VARCHAR(50),
    description VARCHAR(500),
    valet_available BOOLEAN DEFAULT FALSE,
    disabled_parking_available BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_place_parking_place FOREIGN KEY (place_id)
        REFERENCES place_info(id) ON DELETE CASCADE,
    CONSTRAINT chk_parking_type_required CHECK (
        (available = FALSE) OR
        (available = TRUE AND parking_type IS NOT NULL)
    )
);

-- 인덱스 생성
CREATE INDEX idx_place_parkings_place_id ON place_parkings(place_id);
CREATE INDEX idx_place_parkings_available ON place_parkings(available);
CREATE INDEX idx_place_parkings_parking_type ON place_parkings(parking_type);

-- =============================================
-- 7. 업체 이미지 테이블
-- =============================================
CREATE TABLE IF NOT EXISTS place_images (
    id VARCHAR(255) PRIMARY KEY,  -- 이미지 서버에서 제공하는 ID
    place_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    CONSTRAINT fk_place_images_place FOREIGN KEY (place_id)
        REFERENCES place_info(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX idx_place_images_place_id ON place_images(place_id);

-- =============================================
-- 8. 키워드 마스터 테이블
-- =============================================
CREATE TABLE IF NOT EXISTS keywords (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(30) NOT NULL CHECK (type IN ('SPACE_TYPE', 'INSTRUMENT_EQUIPMENT', 'AMENITY', 'OTHER_FEATURE')),
    description VARCHAR(200),
    display_order INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT uk_keywords_name_type UNIQUE (name, type)
);

-- 인덱스 생성
CREATE INDEX idx_keywords_type ON keywords(type);
CREATE INDEX idx_keywords_is_active ON keywords(is_active);
CREATE INDEX idx_keywords_display_order ON keywords(display_order);

-- =============================================
-- 9. 업체-키워드 매핑 테이블 (다대다)
-- =============================================
CREATE TABLE IF NOT EXISTS place_keywords (
    place_id BIGINT NOT NULL,
    keyword_id BIGINT NOT NULL,
    PRIMARY KEY (place_id, keyword_id),
    CONSTRAINT fk_place_keywords_place FOREIGN KEY (place_id)
        REFERENCES place_info(id) ON DELETE CASCADE,
    CONSTRAINT fk_place_keywords_keyword FOREIGN KEY (keyword_id)
        REFERENCES keywords(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX idx_place_keywords_place_id ON place_keywords(place_id);
CREATE INDEX idx_place_keywords_keyword_id ON place_keywords(keyword_id);

-- =============================================
-- 트리거 함수: updated_at 자동 업데이트
-- =============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 각 테이블에 updated_at 트리거 적용
CREATE TRIGGER update_place_info_updated_at BEFORE UPDATE ON place_info
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_place_locations_updated_at BEFORE UPDATE ON place_locations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_place_contacts_updated_at BEFORE UPDATE ON place_contacts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_place_parkings_updated_at BEFORE UPDATE ON place_parkings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- 뷰: 업체 전체 정보 조회용 뷰
-- =============================================
CREATE OR REPLACE VIEW v_place_full_info AS
SELECT
    pi.id,
    pi.user_id,
    pi.place_name,
    pi.description,
    pi.category,
    pi.place_type,
    pi.is_active,
    pi.approval_status,
    pi.rating_average,
    pi.review_count,
    pl.full_address,
    pl.address_detail,
    pl.province,
    pl.city,
    pl.district,
    pl.postal_code,
    pl.latitude,
    pl.longitude,
    pl.location_guide,
    pc.contact,
    pc.email,
    pp.available AS parking_available,
    pp.parking_type,
    pp.hourly_rate,
    pp.description AS parking_description,
    pi.created_at,
    pi.updated_at
FROM place_info pi
LEFT JOIN place_locations pl ON pi.id = pl.place_id
LEFT JOIN place_contacts pc ON pi.id = pc.place_id
LEFT JOIN place_parkings pp ON pi.id = pp.place_id;

-- =============================================
-- 코멘트 추가
-- =============================================
COMMENT ON TABLE place_info IS '업체 기본 정보 테이블';
COMMENT ON COLUMN place_info.user_id IS '업체 소유자 ID (외부 서비스 참조)';
COMMENT ON COLUMN place_info.place_name IS '업체명';
COMMENT ON COLUMN place_info.description IS '업체 소개 (최대 500자)';
COMMENT ON COLUMN place_info.approval_status IS '승인 상태 (PENDING/APPROVED/REJECTED)';

COMMENT ON TABLE place_locations IS '업체 위치 정보 테이블';
COMMENT ON COLUMN place_locations.coordinates IS 'PostGIS 지리 좌표 (Point)';
COMMENT ON COLUMN place_locations.location_guide IS '위치 안내 (예: 2호선 강남역 3번 출구 도보 5분)';

COMMENT ON TABLE place_contacts IS '업체 연락처 정보 테이블';
COMMENT ON TABLE place_websites IS '업체 웹사이트 URL 테이블';
COMMENT ON TABLE place_social_links IS '업체 소셜 미디어 링크 테이블';

COMMENT ON TABLE place_parkings IS '업체 주차 정보 테이블';
COMMENT ON COLUMN place_parkings.parking_type IS '주차 타입 (FREE: 무료, PAID: 유료)';

COMMENT ON TABLE place_images IS '업체 이미지 테이블';
COMMENT ON COLUMN place_images.id IS '이미지 서버에서 제공하는 이미지 ID';

COMMENT ON TABLE keywords IS '키워드 마스터 데이터 테이블';
COMMENT ON COLUMN keywords.type IS '키워드 타입 (SPACE_TYPE/INSTRUMENT_EQUIPMENT/AMENITY/OTHER_FEATURE)';

COMMENT ON TABLE place_keywords IS '업체-키워드 매핑 테이블 (다대다 관계)';