-- PostGIS 확장 활성화
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- 선택적: 추가 확장
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;
CREATE EXTENSION IF NOT EXISTS postgis_tiger_geocoder;

-- 테이블이 없다면 생성 (예시)
CREATE TABLE IF NOT EXISTS place_info (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    location GEOGRAPHY(POINT, 4326),  -- PostGIS 지리 데이터 타입
    address VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 공간 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_place_info_location ON place_info USING GIST(location);

-- 샘플 데이터 삽입 (옵션)
INSERT INTO place_info (name, description, location, address)
VALUES
    ('서울타워', 'N서울타워', ST_GeogFromText('POINT(126.9882 37.5512)'), '서울특별시 용산구 남산공원길 105'),
    ('경복궁', '조선왕조의 정궁', ST_GeogFromText('POINT(126.9770 37.5796)'), '서울특별시 종로구 사직로 161')
ON CONFLICT DO NOTHING;