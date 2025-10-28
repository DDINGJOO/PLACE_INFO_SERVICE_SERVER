-- =============================================
-- Flyway Migration: V1__initial_schema.sql
-- Initial Schema Creation for PlaceInfoServer
-- =============================================

-- Enable extensions (idempotent)
CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE
EXTENSION IF NOT EXISTS "postgis";
CREATE
EXTENSION IF NOT EXISTS "btree_gist";

-- =============================================
-- Drop existing objects (for clean migration)
-- =============================================

-- Drop triggers first
DROP TRIGGER IF EXISTS update_place_info_updated_at ON place_info;
DROP TRIGGER IF EXISTS update_place_contacts_updated_at ON place_contacts;
DROP TRIGGER IF EXISTS update_place_locations_updated_at ON place_locations;
DROP TRIGGER IF EXISTS update_place_parkings_updated_at ON place_parkings;

-- Drop functions
DROP FUNCTION IF EXISTS update_updated_at_column() CASCADE;
DROP FUNCTION IF EXISTS calculate_distance(DOUBLE PRECISION, DOUBLE PRECISION, DOUBLE PRECISION, DOUBLE PRECISION) CASCADE;
DROP FUNCTION IF EXISTS find_places_within_radius(DOUBLE PRECISION, DOUBLE PRECISION, DOUBLE PRECISION) CASCADE;

-- Drop tables in dependency order
DROP TABLE IF EXISTS place_keywords CASCADE;
DROP TABLE IF EXISTS place_social_links CASCADE;
DROP TABLE IF EXISTS place_websites CASCADE;
DROP TABLE IF EXISTS place_images CASCADE;
DROP TABLE IF EXISTS place_parkings CASCADE;
DROP TABLE IF EXISTS place_locations CASCADE;
DROP TABLE IF EXISTS place_contacts CASCADE;
DROP TABLE IF EXISTS place_info CASCADE;
DROP TABLE IF EXISTS keywords CASCADE;

-- Drop types
DROP
TYPE IF EXISTS approval_status CASCADE;
DROP
TYPE IF EXISTS keyword_type CASCADE;
DROP
TYPE IF EXISTS parking_type CASCADE;

-- =============================================
-- Create custom types
-- =============================================

CREATE
TYPE approval_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED'
);

CREATE
TYPE keyword_type AS ENUM (
    'SPACE_TYPE',
    'INSTRUMENT_EQUIPMENT',
    'AMENITY',
    'OTHER_FEATURE'
);

CREATE
TYPE parking_type AS ENUM (
    'FREE',
    'PAID'
);

-- =============================================
-- Create tables
-- =============================================

-- Main aggregate root
CREATE TABLE place_info
(
    id              VARCHAR(100) PRIMARY KEY,
    user_id         VARCHAR(100) NOT NULL,
    place_name      VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    category        VARCHAR(50),
    place_type      VARCHAR(50),
    is_active       BOOLEAN      NOT NULL DEFAULT false,
    approval_status VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    rating_average  DOUBLE PRECISION,
    review_count    INTEGER               DEFAULT 0,
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(100),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Keywords master data
CREATE TABLE keywords
(
    id BIGSERIAL PRIMARY KEY,
    name          VARCHAR(50) NOT NULL,
    type          VARCHAR(50) NOT NULL,
    description   VARCHAR(200),
    display_order INTEGER,
    is_active     BOOLEAN DEFAULT true,
    CONSTRAINT uk_keywords_name_type UNIQUE (name, type)
);

-- Place contacts
CREATE TABLE place_contacts
(
    id BIGSERIAL PRIMARY KEY,
    place_info_id VARCHAR(100) NOT NULL UNIQUE,
    contact       VARCHAR(20),
    email         VARCHAR(100),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_place_contacts_place_info
        FOREIGN KEY (place_info_id)
            REFERENCES place_info (id)
            ON DELETE CASCADE
);

-- Place locations with PostGIS support
CREATE TABLE place_locations
(
    id BIGSERIAL PRIMARY KEY,
    place_info_id  VARCHAR(100) NOT NULL UNIQUE,
    province       VARCHAR(50),
    city           VARCHAR(50),
    district       VARCHAR(50),
    full_address   VARCHAR(500) NOT NULL,
    address_detail VARCHAR(200),
    postal_code    VARCHAR(10),
    coordinates geography(Point, 4326),
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

-- Place parking information
CREATE TABLE place_parkings
(
    id BIGSERIAL PRIMARY KEY,
    place_info_id VARCHAR(100) NOT NULL UNIQUE,
    available     BOOLEAN      NOT NULL DEFAULT false,
    parking_type  VARCHAR(10),
    description   VARCHAR(500),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_place_parkings_place_info
        FOREIGN KEY (place_info_id)
            REFERENCES place_info (id)
            ON DELETE CASCADE,
    CONSTRAINT chk_parking_type
        CHECK (parking_type IN ('FREE', 'PAID'))
);

-- Place images
CREATE TABLE place_images
(
    id            VARCHAR(100) PRIMARY KEY,
    place_info_id VARCHAR(100) NOT NULL,
    image_url     VARCHAR(500) NOT NULL,
    display_order INTEGER DEFAULT 0,
    CONSTRAINT fk_place_images_place_info
        FOREIGN KEY (place_info_id)
            REFERENCES place_info (id)
            ON DELETE CASCADE
);

-- Element collection: websites
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

-- Element collection: social links
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

-- Join table: place-keywords
CREATE TABLE place_keywords
(
    place_id   VARCHAR(100) NOT NULL,
    keyword_id BIGINT       NOT NULL,
    PRIMARY KEY (place_id, keyword_id),
    CONSTRAINT fk_place_keywords_place
        FOREIGN KEY (place_id)
            REFERENCES place_info (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_place_keywords_keyword
        FOREIGN KEY (keyword_id)
            REFERENCES keywords (id)
            ON DELETE CASCADE
);

-- =============================================
-- Create indexes
-- =============================================

-- Place info indexes
CREATE INDEX idx_place_info_user_id ON place_info (user_id);
CREATE INDEX idx_place_info_approval_status ON place_info (approval_status);
CREATE INDEX idx_place_info_is_active ON place_info (is_active);
CREATE INDEX idx_place_info_deleted_at ON place_info (deleted_at);
CREATE INDEX idx_place_info_category ON place_info (category);
CREATE INDEX idx_place_info_place_type ON place_info (place_type);
CREATE INDEX idx_place_info_rating ON place_info (rating_average);
CREATE INDEX idx_place_info_created_at ON place_info (created_at);

-- Place contacts indexes
CREATE INDEX idx_place_contacts_email ON place_contacts (email);

-- Place locations indexes
CREATE INDEX idx_place_locations_coordinates ON place_locations USING GIST(coordinates);
CREATE INDEX idx_place_locations_lat_lng ON place_locations (latitude, longitude);
CREATE INDEX idx_place_locations_province ON place_locations (province);
CREATE INDEX idx_place_locations_city ON place_locations (city);
CREATE INDEX idx_place_locations_district ON place_locations (district);
CREATE INDEX idx_place_locations_postal_code ON place_locations (postal_code);

-- Place images indexes
CREATE INDEX idx_place_images_place_info_id ON place_images (place_info_id);
CREATE INDEX idx_place_images_order ON place_images (place_info_id, display_order);

-- Keywords indexes
CREATE INDEX idx_keywords_type ON keywords (type);
CREATE INDEX idx_keywords_is_active ON keywords (is_active);
CREATE INDEX idx_keywords_display_order ON keywords (display_order);

-- Element collection indexes
CREATE INDEX idx_place_websites_contact_id ON place_websites (place_contact_id);
CREATE INDEX idx_place_social_links_contact_id ON place_social_links (place_contact_id);

-- Join table indexes
CREATE INDEX idx_place_keywords_place_id ON place_keywords (place_id);
CREATE INDEX idx_place_keywords_keyword_id ON place_keywords (keyword_id);

-- =============================================
-- Create functions and triggers
-- =============================================

-- Function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

-- Apply update triggers
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

-- Distance calculation function
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

-- Function to find places within radius
CREATE OR REPLACE FUNCTION find_places_within_radius(
    center_lat DOUBLE PRECISION,
    center_lon DOUBLE PRECISION,
    radius_meters DOUBLE PRECISION
) RETURNS TABLE(
        place_id VARCHAR(100),
        place_name VARCHAR(100),
        distance_meters DOUBLE PRECISION
          ) AS $$
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
-- Add table and column comments
-- =============================================

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
ON COLUMN place_locations.coordinates IS 'PostGIS geography 타입 (SRID 4326)';
COMMENT
ON COLUMN place_locations.latitude IS '검색 최적화를 위한 중복 저장';
COMMENT
ON COLUMN place_locations.longitude IS '검색 최적화를 위한 중복 저장';

COMMENT
ON COLUMN place_images.id IS '외부 이미지 서비스 ID';
