-- =============================================
-- PlaceInfoServer PostgreSQL Database Schema
-- Version: 1.0.0
-- =============================================

-- =============================================
-- 1. Database Setup & Extensions
-- =============================================

-- Create database if needed (run separately as superuser)
-- CREATE DATABASE placeinfo_db WITH ENCODING 'UTF8';

-- Enable required extensions
CREATE
    EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE
    EXTENSION IF NOT EXISTS "postgis";
CREATE
    EXTENSION IF NOT EXISTS "btree_gist";

-- =============================================
-- 2. Custom Types (Enums)
-- =============================================

-- Approval status enum
CREATE
    TYPE approval_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED'
    );

-- Keyword type enum
CREATE
    TYPE keyword_type AS ENUM (
    'SPACE_TYPE',
    'INSTRUMENT_EQUIPMENT',
    'AMENITY',
    'OTHER_FEATURE'
    );

-- Parking type enum
CREATE
    TYPE parking_type AS ENUM (
    'FREE',
    'PAID'
    );

-- =============================================
-- 3. Main Tables
-- =============================================

-- 3.1 Place Info (Aggregate Root)
CREATE TABLE place_info
(
    id           BIGINT PRIMARY KEY, -- Snowflake ID (Long type)
    user_id      VARCHAR(100) NOT NULL,
    place_name   VARCHAR(100) NOT NULL,
    description  VARCHAR(500),
    category     VARCHAR(50),
    place_type   VARCHAR(50),
    is_active           BOOLEAN      NOT NULL DEFAULT false,
    approval_status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    registration_status VARCHAR(20)  NOT NULL DEFAULT 'UNREGISTERED',
    rating_average      DOUBLE PRECISION,
    review_count INTEGER               DEFAULT 0,
    deleted_at   TIMESTAMP,
    deleted_by   VARCHAR(100),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3.2 Keywords (Master Data)
CREATE TABLE keywords
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(50) NOT NULL,
    type          VARCHAR(50) NOT NULL,
    description   VARCHAR(200),
    display_order INTEGER,
    is_active     BOOLEAN DEFAULT true,
    CONSTRAINT uk_keywords_name_type UNIQUE (name, type)
);

-- 3.3 Room (Separate Aggregate Root)
CREATE TABLE room
(
    id         BIGSERIAL PRIMARY KEY,
    room_id    BIGINT    NOT NULL UNIQUE,
    place_id   BIGINT    NOT NULL,
    is_active  BOOLEAN   NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 4. Related Entity Tables
-- =============================================

-- 4.1 Place Contacts
CREATE TABLE place_contacts
(
    id            BIGSERIAL PRIMARY KEY,
    place_info_id BIGINT    NOT NULL UNIQUE,
    contact       VARCHAR(20),
    email         VARCHAR(100),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_place_contacts_place_info
        FOREIGN KEY (place_info_id)
            REFERENCES place_info (id)
            ON DELETE CASCADE
);

-- 4.2 Place Locations (with PostGIS)
CREATE TABLE place_locations
(
    id             BIGSERIAL PRIMARY KEY,
    place_info_id  BIGINT       NOT NULL UNIQUE,
    -- Address (embedded value object)
    province       VARCHAR(50),
    city           VARCHAR(50),
    district       VARCHAR(50),
    full_address   VARCHAR(500) NOT NULL,
    address_detail VARCHAR(200),
    postal_code    VARCHAR(10),
    -- Spatial data
    coordinates    geography(Point, 4326), -- PostGIS geography type
    latitude       DOUBLE PRECISION,
    longitude      DOUBLE PRECISION,
    location_guide VARCHAR(500),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_place_locations_place_info
        FOREIGN KEY (place_info_id)
            REFERENCES place_info (id)
            ON DELETE CASCADE
);

-- 4.3 Place Parking
CREATE TABLE place_parkings
(
    id            BIGSERIAL PRIMARY KEY,
    place_info_id BIGINT    NOT NULL UNIQUE,
    available     BOOLEAN   NOT NULL DEFAULT false,
    parking_type  VARCHAR(10),
    description   VARCHAR(500),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_place_parkings_place_info
        FOREIGN KEY (place_info_id)
            REFERENCES place_info (id)
            ON DELETE CASCADE,
    CONSTRAINT chk_parking_type
        CHECK (parking_type IN ('FREE', 'PAID'))
);

-- 4.4 Place Images
CREATE TABLE place_images
(
    id            VARCHAR(255) PRIMARY KEY, -- Image ID from external service
    place_info_id BIGINT       NOT NULL,
    image_url     VARCHAR(500) NOT NULL,
    CONSTRAINT fk_place_images_place_info
        FOREIGN KEY (place_info_id)
            REFERENCES place_info (id)
            ON DELETE CASCADE
);

-- =============================================
-- 5. Element Collection Tables
-- =============================================

-- 5.1 Place Websites (Element Collection)
CREATE TABLE place_websites
(
    place_contact_id BIGINT  NOT NULL,
    websites         VARCHAR(500),
    websites_order   INTEGER NOT NULL,
    CONSTRAINT fk_place_websites_contact
        FOREIGN KEY (place_contact_id)
            REFERENCES place_contacts (id)
            ON DELETE CASCADE
);

-- 5.2 Place Social Links (Element Collection)
CREATE TABLE place_social_links
(
    place_contact_id   BIGINT  NOT NULL,
    social_links       VARCHAR(500),
    social_links_order INTEGER NOT NULL,
    CONSTRAINT fk_place_social_links_contact
        FOREIGN KEY (place_contact_id)
            REFERENCES place_contacts (id)
            ON DELETE CASCADE
);

-- =============================================
-- 6. Join Tables
-- =============================================

-- 6.1 Place-Keywords (Many-to-Many)
CREATE TABLE place_keywords
(
    place_info_id BIGINT NOT NULL,
    keyword_id    BIGINT NOT NULL,
    PRIMARY KEY (place_info_id, keyword_id),
    CONSTRAINT fk_place_keywords_place
        FOREIGN KEY (place_info_id)
            REFERENCES place_info (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_place_keywords_keyword
        FOREIGN KEY (keyword_id)
            REFERENCES keywords (id)
            ON DELETE CASCADE
);

-- =============================================
-- 7. Indexes
-- =============================================

-- Place Info indexes
CREATE INDEX idx_place_info_user_id ON place_info (user_id);
CREATE INDEX idx_place_info_approval_status ON place_info (approval_status);
CREATE INDEX idx_place_info_is_active ON place_info (is_active);
CREATE INDEX idx_place_info_deleted_at ON place_info (deleted_at);
CREATE INDEX idx_place_info_category ON place_info (category);
CREATE INDEX idx_place_info_place_type ON place_info (place_type);
CREATE INDEX idx_place_info_rating ON place_info (rating_average);
CREATE INDEX idx_place_info_created_at ON place_info (created_at);
CREATE INDEX idx_place_info_registration_status ON place_info (registration_status);
CREATE INDEX idx_place_info_registration_rating ON place_info (registration_status DESC, rating_average DESC);
CREATE INDEX idx_place_info_registration_review ON place_info (registration_status DESC, review_count DESC);
CREATE INDEX idx_place_info_registration_created ON place_info (registration_status DESC, created_at DESC);

-- Place Contacts indexes
CREATE INDEX idx_place_contacts_email ON place_contacts (email);

-- Place Locations indexes (Spatial + Regular)
CREATE INDEX idx_place_locations_coordinates ON place_locations USING GIST (coordinates);
CREATE INDEX idx_place_locations_lat_lng ON place_locations (latitude, longitude);
CREATE INDEX idx_place_locations_province ON place_locations (province);
CREATE INDEX idx_place_locations_city ON place_locations (city);
CREATE INDEX idx_place_locations_district ON place_locations (district);
CREATE INDEX idx_place_locations_postal_code ON place_locations (postal_code);

-- Place Images indexes
CREATE INDEX idx_place_images_place_info_id ON place_images (place_info_id);
CREATE INDEX idx_place_images_order ON place_images (place_info_id, display_order);

-- Keywords indexes
CREATE INDEX idx_keywords_type ON keywords (type);
CREATE INDEX idx_keywords_is_active ON keywords (is_active);
CREATE INDEX idx_keywords_display_order ON keywords (display_order);

-- Room indexes
CREATE INDEX idx_room_room_id ON room (room_id);
CREATE INDEX idx_room_place_id ON room (place_id);
CREATE INDEX idx_room_is_active ON room (is_active);

-- Element Collection indexes
CREATE INDEX idx_place_websites_contact_id ON place_websites (place_contact_id);
CREATE INDEX idx_place_social_links_contact_id ON place_social_links (place_contact_id);

-- Join table indexes
CREATE INDEX idx_place_keywords_place_id ON place_keywords (place_info_id);
CREATE INDEX idx_place_keywords_keyword_id ON place_keywords (keyword_id);

-- =============================================
-- 8. Triggers for Updated Timestamp
-- =============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$
    LANGUAGE plpgsql;

-- Apply trigger to tables with updated_at
CREATE TRIGGER update_place_info_updated_at
    BEFORE UPDATE
    ON place_info
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_place_contacts_updated_at
    BEFORE UPDATE
    ON place_contacts
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_place_locations_updated_at
    BEFORE UPDATE
    ON place_locations
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_place_parkings_updated_at
    BEFORE UPDATE
    ON place_parkings
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_room_updated_at
    BEFORE UPDATE
    ON room
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- 9. Functions for Business Logic
-- =============================================

-- Function to calculate distance between two points (in meters)
CREATE OR REPLACE FUNCTION calculate_distance(
    lat1 DOUBLE PRECISION,
    lon1 DOUBLE PRECISION,
    lat2 DOUBLE PRECISION,
    lon2 DOUBLE PRECISION
) RETURNS DOUBLE PRECISION AS
$$
BEGIN
    RETURN ST_Distance(
            ST_MakePoint(lon1, lat1) : :geography,
            ST_MakePoint(lon2, lat2) : :geography
           );
END;
$$
    LANGUAGE plpgsql IMMUTABLE;

-- Function to find places within radius (in meters)
CREATE OR REPLACE FUNCTION find_places_within_radius(
    center_lat DOUBLE PRECISION,
    center_lon DOUBLE PRECISION,
    radius_meters DOUBLE PRECISION
)
    RETURNS TABLE
            (
                place_id        BIGINT,
                place_name      VARCHAR(100),
                distance_meters DOUBLE PRECISION
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT pi.id,
               pi.place_name,
               ST_Distance(
                       pl.coordinates,
                       ST_MakePoint(center_lon, center_lat) : :geography
               ) AS distance_meters
        FROM place_info pi
                 JOIN place_locations pl ON pi.id = pl.place_info_id
        WHERE pi.deleted_at IS NULL
          AND pi.is_active = true
          AND ST_DWithin(
                pl.coordinates,
                ST_MakePoint(center_lon, center_lat)::geography,
                radius_meters
              )
        ORDER BY distance_meters;
END;
$$
    LANGUAGE plpgsql;

-- =============================================
-- 10. Comments for Documentation
-- =============================================

-- Table comments
COMMENT
    ON TABLE place_info IS '장소 정보 (Aggregate Root)';
COMMENT
    ON TABLE keywords IS '키워드 마스터 데이터';
COMMENT
    ON TABLE place_contacts IS '장소 연락처 정보';
COMMENT
    ON TABLE place_locations IS '장소 위치 정보 (PostGIS 지원)';
COMMENT
    ON TABLE place_parkings IS '장소 주차 정보';
COMMENT
    ON TABLE place_images IS '장소 이미지 정보';
COMMENT
    ON TABLE place_websites IS '장소 웹사이트 목록 (ElementCollection)';
COMMENT
    ON TABLE place_social_links IS '장소 소셜 링크 목록 (ElementCollection)';
COMMENT
    ON TABLE place_keywords IS '장소-키워드 매핑 (N:N)';

-- Column comments
COMMENT
    ON COLUMN place_info.id IS 'Snowflake 알고리즘으로 생성된 ID';
COMMENT
    ON COLUMN place_info.user_id IS '외부 사용자 서비스 참조';
COMMENT
    ON COLUMN place_info.deleted_at IS '소프트 삭제 타임스탬프';
COMMENT
    ON COLUMN place_info.rating_average IS '리뷰 서비스에서 업데이트';
COMMENT
    ON COLUMN place_info.review_count IS '리뷰 서비스에서 업데이트';
COMMENT
    ON COLUMN place_info.registration_status IS '업체 등록 상태 (REGISTERED: 정식 등록, UNREGISTERED: 미등록)';

COMMENT
    ON COLUMN place_locations.coordinates IS 'PostGIS geography 타입 (SRID 4326)';
COMMENT
    ON COLUMN place_locations.latitude IS '검색 최적화를 위한 중복 저장';
COMMENT
    ON COLUMN place_locations.longitude IS '검색 최적화를 위한 중복 저장';

COMMENT
    ON COLUMN place_images.id IS '외부 이미지 서비스 ID';

-- =============================================
-- 11. Sample Queries
-- =============================================

/*
-- Find active places within 5km radius
SELECT * FROM find_places_within_radius(37.5665, 126.9780, 5000);

-- Get place with all related data
SELECT
    pi.*,
    pc.*,
    pl.*,
    pp.*
FROM place_info pi
LEFT JOIN place_contacts pc ON pi.id = pc.place_info_id
LEFT JOIN place_locations pl ON pi.id = pl.place_info_id
LEFT JOIN place_parkings pp ON pi.id = pp.place_info_id
WHERE pi.id = 'some-id'
    AND pi.deleted_at IS NULL;

-- Get places with specific keywords
SELECT DISTINCT pi.*
FROM place_info pi
JOIN place_keywords pk ON pi.id = pk.place_id
JOIN keywords k ON pk.keyword_id = k.id
WHERE k.name IN ('합주실', '녹음실')
    AND pi.is_active = true
    AND pi.deleted_at IS NULL;
*/

-- =============================================
-- End of Schema
-- =============================================
