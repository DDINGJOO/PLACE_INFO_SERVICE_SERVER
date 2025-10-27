-- =============================================
-- Place Info Service - Entity-based Database Migration V2
-- =============================================
-- 이 마이그레이션은 JPA 엔티티를 기준으로 테이블을 수정합니다.
-- View 의존성을 처리한 버전입니다.
-- =============================================

-- PostGIS Extension 활성화 (이미 존재하지 않는 경우에만)
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- =============================================
-- 1. 기존 View 삭제 (place_parkings 컬럼 의존성 해결)
-- =============================================
DROP VIEW IF EXISTS v_place_full_info CASCADE;

-- =============================================
-- 2. place_parkings 테이블 수정
-- =============================================
-- 엔티티에 없는 필드들 제거
ALTER TABLE place_parkings
  DROP COLUMN IF EXISTS hourly_rate CASCADE,
  DROP COLUMN IF EXISTS daily_max_rate CASCADE,
  DROP COLUMN IF EXISTS parking_lot_type CASCADE,
  DROP COLUMN IF EXISTS valet_available CASCADE,
  DROP COLUMN IF EXISTS disabled_parking_available CASCADE;

-- =============================================
-- 3. View 재생성 (엔티티 기준으로 수정)
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
    pp.capacity AS parking_capacity,
    pp.description AS parking_description,
    pi.created_at,
    pi.updated_at
FROM place_info pi
LEFT JOIN place_locations pl ON pi.id = pl.place_id
LEFT JOIN place_contacts pc ON pi.id = pc.place_id
LEFT JOIN place_parkings pp ON pi.id = pp.place_id;

-- =============================================
-- 4. 코멘트 업데이트 (엔티티 기준)
-- =============================================
COMMENT ON TABLE place_info IS '업체 기본 정보 테이블 (Aggregate Root)';
COMMENT ON COLUMN place_info.user_id IS '업체 소유자 ID (외부 서비스 참조)';
COMMENT ON COLUMN place_info.place_name IS '업체명';
COMMENT ON COLUMN place_info.description IS '업체 소개 (최대 500자)';
COMMENT ON COLUMN place_info.category IS '업체 카테고리 (예: 연습실, 공연장, 스튜디오 등)';
COMMENT ON COLUMN place_info.place_type IS '업체 유형 (예: 음악, 댄스, 공연 등)';
COMMENT ON COLUMN place_info.is_active IS '업체 활성화 상태';
COMMENT ON COLUMN place_info.approval_status IS '승인 상태 (PENDING/APPROVED/REJECTED)';
COMMENT ON COLUMN place_info.rating_average IS '평점 평균 (리뷰 서버에서 업데이트)';
COMMENT ON COLUMN place_info.review_count IS '리뷰 개수 (리뷰 서버에서 업데이트)';

COMMENT ON TABLE place_locations IS '업체 위치 정보 테이블 (1:1 관계)';
COMMENT ON COLUMN place_locations.coordinates IS 'PostGIS 지리 좌표 (Point, WGS84)';
COMMENT ON COLUMN place_locations.location_guide IS '위치 안내 (예: 2호선 강남역 3번 출구 도보 5분)';
COMMENT ON COLUMN place_locations.latitude IS '위도 (검색 최적화용 별도 저장)';
COMMENT ON COLUMN place_locations.longitude IS '경도 (검색 최적화용 별도 저장)';

COMMENT ON TABLE place_contacts IS '업체 연락처 정보 테이블 (1:1 관계)';
COMMENT ON COLUMN place_contacts.contact IS '연락처 (하이픈 없이 저장)';
COMMENT ON COLUMN place_contacts.email IS '이메일 주소';

COMMENT ON TABLE place_websites IS '업체 웹사이트 URL 테이블 (ElementCollection)';
COMMENT ON COLUMN place_websites.website_url IS '홈페이지 URL (최대 10개)';
COMMENT ON COLUMN place_websites.display_order IS '표시 순서';

COMMENT ON TABLE place_social_links IS '업체 소셜 미디어 링크 테이블 (ElementCollection)';
COMMENT ON COLUMN place_social_links.social_url IS '소셜 미디어 URL (Instagram, Facebook, YouTube 등)';
COMMENT ON COLUMN place_social_links.display_order IS '표시 순서';

COMMENT ON TABLE place_parkings IS '업체 주차 정보 테이블 (1:1 관계)';
COMMENT ON COLUMN place_parkings.available IS '주차 가능 여부';
COMMENT ON COLUMN place_parkings.parking_type IS '주차 타입 (FREE: 무료, PAID: 유료)';
COMMENT ON COLUMN place_parkings.capacity IS '주차 가능 대수';
COMMENT ON COLUMN place_parkings.description IS '주차 관련 상세 설명 (예: 건물 지하 1층, 2시간 무료 주차 가능)';

COMMENT ON TABLE place_images IS '업체 이미지 테이블 (1:N 관계)';
COMMENT ON COLUMN place_images.id IS '이미지 서버에서 제공하는 이미지 ID';
COMMENT ON COLUMN place_images.image_url IS '이미지 URL 또는 외부 서비스의 이미지 ID';

COMMENT ON TABLE keywords IS '키워드 마스터 데이터 테이블';
COMMENT ON COLUMN keywords.name IS '키워드명';
COMMENT ON COLUMN keywords.type IS '키워드 타입 (SPACE_TYPE/INSTRUMENT_EQUIPMENT/AMENITY/OTHER_FEATURE)';
COMMENT ON COLUMN keywords.description IS '키워드 설명';
COMMENT ON COLUMN keywords.display_order IS '표시 순서';
COMMENT ON COLUMN keywords.is_active IS '활성화 상태';

COMMENT ON TABLE place_keywords IS '업체-키워드 매핑 테이블 (다대다 관계)';

-- =============================================
-- 5. 통계 정보를 위한 함수 생성
-- =============================================

-- 업체별 완성도 체크 함수
CREATE OR REPLACE FUNCTION check_place_completeness(p_place_id BIGINT)
RETURNS TABLE(
    is_complete BOOLEAN,
    has_location BOOLEAN,
    has_contact BOOLEAN,
    has_parking_info BOOLEAN,
    has_images BOOLEAN,
    has_keywords BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        (pl.id IS NOT NULL AND pc.id IS NOT NULL) AS is_complete,
        (pl.id IS NOT NULL) AS has_location,
        (pc.id IS NOT NULL) AS has_contact,
        (pp.id IS NOT NULL) AS has_parking_info,
        (COUNT(DISTINCT pimg.id) > 0) AS has_images,
        (COUNT(DISTINCT pk.keyword_id) > 0) AS has_keywords
    FROM place_info pi
    LEFT JOIN place_locations pl ON pi.id = pl.place_id
    LEFT JOIN place_contacts pc ON pi.id = pc.place_id
    LEFT JOIN place_parkings pp ON pi.id = pp.place_id
    LEFT JOIN place_images pimg ON pi.id = pimg.place_id
    LEFT JOIN place_keywords pk ON pi.id = pk.place_id
    WHERE pi.id = p_place_id
    GROUP BY pl.id, pc.id, pp.id;
END;
$$ LANGUAGE plpgsql;

-- 반경 내 업체 검색 함수 (PostGIS 사용)
CREATE OR REPLACE FUNCTION find_places_within_radius(
    p_latitude DOUBLE PRECISION,
    p_longitude DOUBLE PRECISION,
    p_radius_meters DOUBLE PRECISION
)
RETURNS TABLE(
    place_id BIGINT,
    place_name VARCHAR(100),
    category VARCHAR(50),
    distance_meters DOUBLE PRECISION,
    full_address VARCHAR(500)
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        pi.id AS place_id,
        pi.place_name,
        pi.category,
        ST_Distance(
            pl.coordinates,
            ST_GeogFromText(format('POINT(%s %s)', p_longitude, p_latitude))
        ) AS distance_meters,
        pl.full_address
    FROM place_info pi
    JOIN place_locations pl ON pi.id = pl.place_id
    WHERE pi.is_active = true
        AND pi.approval_status = 'APPROVED'
        AND ST_DWithin(
            pl.coordinates,
            ST_GeogFromText(format('POINT(%s %s)', p_longitude, p_latitude)),
            p_radius_meters
        )
    ORDER BY distance_meters;
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- 6. 마이그레이션 완료 메시지
-- =============================================
DO $$
BEGIN
    RAISE NOTICE '===================================================';
    RAISE NOTICE '엔티티 기반 마이그레이션 V2가 완료되었습니다.';
    RAISE NOTICE '===================================================';
    RAISE NOTICE '변경사항:';
    RAISE NOTICE '1. v_place_full_info View 재생성';
    RAISE NOTICE '2. place_parkings 테이블에서 엔티티에 없는 컬럼 제거:';
    RAISE NOTICE '   - hourly_rate (시간당 요금)';
    RAISE NOTICE '   - daily_max_rate (일일 최대 요금)';
    RAISE NOTICE '   - parking_lot_type (주차장 유형)';
    RAISE NOTICE '   - valet_available (발렛 가능)';
    RAISE NOTICE '   - disabled_parking_available (장애인 주차)';
    RAISE NOTICE '3. 유틸리티 함수 추가:';
    RAISE NOTICE '   - check_place_completeness(): 업체 정보 완성도 체크';
    RAISE NOTICE '   - find_places_within_radius(): 반경 내 업체 검색';
    RAISE NOTICE '===================================================';
END $$;