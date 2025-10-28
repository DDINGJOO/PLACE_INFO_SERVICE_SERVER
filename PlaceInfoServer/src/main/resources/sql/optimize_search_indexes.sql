-- =============================================
-- 공간 탐색 성능 최적화를 위한 인덱스
-- 커서 기반 페이징과 다양한 검색 조건 지원
-- =============================================

-- =============================================
-- 1. 기본 필터 인덱스 (모든 검색에 사용)
-- =============================================

-- 복합 인덱스: 기본 필터 조건 (deleted_at, is_active, approval_status)
-- 모든 검색 쿼리에서 사용되는 기본 조건
CREATE INDEX IF NOT EXISTS idx_place_info_base_filter
ON place_info(deleted_at, is_active, approval_status)
WHERE deleted_at IS NULL
  AND is_active = true
  AND approval_status = 'APPROVED';

-- =============================================
-- 2. 정렬 기준별 커서 페이징 인덱스
-- =============================================

-- 평점순 정렬 + 커서 페이징
CREATE INDEX IF NOT EXISTS idx_place_info_rating_cursor
ON place_info(rating_average DESC NULLS LAST, id ASC)
WHERE deleted_at IS NULL
  AND is_active = true
  AND approval_status = 'APPROVED';

-- 리뷰 수순 정렬 + 커서 페이징
CREATE INDEX IF NOT EXISTS idx_place_info_review_count_cursor
ON place_info(review_count DESC, id ASC)
WHERE deleted_at IS NULL
  AND is_active = true
  AND approval_status = 'APPROVED';

-- 최신순 정렬 + 커서 페이징
CREATE INDEX IF NOT EXISTS idx_place_info_created_at_cursor
ON place_info(created_at DESC, id ASC)
WHERE deleted_at IS NULL
  AND is_active = true
  AND approval_status = 'APPROVED';

-- 이름순 정렬 + 커서 페이징
CREATE INDEX IF NOT EXISTS idx_place_info_name_cursor
ON place_info(place_name ASC, id ASC)
WHERE deleted_at IS NULL
  AND is_active = true
  AND approval_status = 'APPROVED';

-- =============================================
-- 3. 검색 조건별 인덱스
-- =============================================

-- 텍스트 검색을 위한 GIN 인덱스
-- 장소명, 설명, 카테고리 검색 최적화
CREATE EXTENSION IF NOT EXISTS pg_trgm; -- trigram 확장 활성화

CREATE INDEX IF NOT EXISTS idx_place_info_name_trgm
ON place_info USING GIN (place_name gin_trgm_ops)
WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_place_info_description_trgm
ON place_info USING GIN (description gin_trgm_ops)
WHERE deleted_at IS NULL;

-- 카테고리 및 타입 검색
CREATE INDEX IF NOT EXISTS idx_place_info_category_type
ON place_info(category, place_type)
WHERE deleted_at IS NULL
  AND is_active = true
  AND approval_status = 'APPROVED';

-- =============================================
-- 4. 위치 기반 검색 인덱스
-- =============================================

-- PostGIS 공간 인덱스 (이미 존재하지만 재확인)
CREATE INDEX IF NOT EXISTS idx_place_locations_coordinates_gist
ON place_locations USING GIST(coordinates);

-- 지역 검색을 위한 복합 인덱스
CREATE INDEX IF NOT EXISTS idx_place_locations_region
ON place_locations(province, city, district);

-- 위도/경도 범위 검색용 B-tree 인덱스
CREATE INDEX IF NOT EXISTS idx_place_locations_lat_lng_btree
ON place_locations(latitude, longitude)
WHERE latitude IS NOT NULL
  AND longitude IS NOT NULL;

-- =============================================
-- 5. 주차 정보 검색 인덱스
-- =============================================

-- 주차 가능 여부 검색
CREATE INDEX IF NOT EXISTS idx_place_parkings_available
ON place_parkings(place_info_id, available)
WHERE available = true;

-- =============================================
-- 6. 키워드 검색 인덱스
-- =============================================

-- 키워드 매핑 검색 최적화
CREATE INDEX IF NOT EXISTS idx_place_keywords_search
ON place_keywords(keyword_id, place_id);

-- 역방향 인덱스 (place_id로 키워드 조회)
CREATE INDEX IF NOT EXISTS idx_place_keywords_reverse
ON place_keywords(place_id, keyword_id);

-- 활성 키워드 검색
CREATE INDEX IF NOT EXISTS idx_keywords_active
ON keywords(is_active, type, display_order)
WHERE is_active = true;

-- =============================================
-- 7. 조인 최적화 인덱스
-- =============================================

-- 외래키 인덱스 (조인 성능 향상)
CREATE INDEX IF NOT EXISTS idx_place_contacts_place_info_id_fk
ON place_contacts(place_info_id);

CREATE INDEX IF NOT EXISTS idx_place_locations_place_info_id_fk
ON place_locations(place_info_id);

CREATE INDEX IF NOT EXISTS idx_place_parkings_place_info_id_fk
ON place_parkings(place_info_id);

CREATE INDEX IF NOT EXISTS idx_place_images_place_info_id_ordered
ON place_images(place_info_id, display_order);

-- =============================================
-- 8. 복합 검색 최적화
-- =============================================

-- 위치 + 카테고리 복합 검색
CREATE INDEX IF NOT EXISTS idx_place_combined_search
ON place_info(category, place_type, rating_average DESC)
WHERE deleted_at IS NULL
  AND is_active = true
  AND approval_status = 'APPROVED';

-- =============================================
-- 9. 통계 및 분석용 인덱스
-- =============================================

-- 사용자별 장소 통계
CREATE INDEX IF NOT EXISTS idx_place_info_user_stats
ON place_info(user_id, is_active, approval_status)
WHERE deleted_at IS NULL;

-- 월별 등록 통계
CREATE INDEX IF NOT EXISTS idx_place_info_monthly_stats
ON place_info(DATE_TRUNC('month', created_at), approval_status)
WHERE deleted_at IS NULL;

-- =============================================
-- 10. 성능 모니터링 뷰
-- =============================================

-- 자주 사용되는 검색 쿼리를 위한 Materialized View
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_active_places AS
SELECT
    pi.id,
    pi.place_name,
    pi.description,
    pi.category,
    pi.place_type,
    pi.rating_average,
    pi.review_count,
    pi.created_at,
    pl.latitude,
    pl.longitude,
    pl.full_address,
    pl.province,
    pl.city,
    pl.district,
    pp.available as parking_available,
    pp.parking_type,
    array_agg(DISTINCT k.name) as keywords
FROM place_info pi
LEFT JOIN place_locations pl ON pi.id = pl.place_info_id
LEFT JOIN place_parkings pp ON pi.id = pp.place_info_id
LEFT JOIN place_keywords pk ON pi.id = pk.place_id
LEFT JOIN keywords k ON pk.keyword_id = k.id AND k.is_active = true
WHERE pi.deleted_at IS NULL
  AND pi.is_active = true
  AND pi.approval_status = 'APPROVED'
GROUP BY pi.id, pi.place_name, pi.description, pi.category, pi.place_type,
         pi.rating_average, pi.review_count, pi.created_at,
         pl.latitude, pl.longitude, pl.full_address, pl.province, pl.city, pl.district,
         pp.available, pp.parking_type;

-- Materialized View 인덱스
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_active_places_id
ON mv_active_places(id);

CREATE INDEX IF NOT EXISTS idx_mv_active_places_location
ON mv_active_places(latitude, longitude)
WHERE latitude IS NOT NULL AND longitude IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_mv_active_places_region
ON mv_active_places(province, city, district);

CREATE INDEX IF NOT EXISTS idx_mv_active_places_rating
ON mv_active_places(rating_average DESC NULLS LAST);

-- =============================================
-- 11. 인덱스 통계 업데이트
-- =============================================

-- 모든 테이블의 통계 정보 업데이트 (쿼리 플래너 최적화)
ANALYZE place_info;
ANALYZE place_locations;
ANALYZE place_parkings;
ANALYZE place_contacts;
ANALYZE place_images;
ANALYZE place_keywords;
ANALYZE keywords;
ANALYZE place_websites;
ANALYZE place_social_links;

-- =============================================
-- 12. 인덱스 사용량 모니터링 쿼리
-- =============================================

-- 인덱스 사용 통계 확인
/*
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan as index_scans,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched,
    pg_size_pretty(pg_relation_size(indexrelid)) as index_size
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY idx_scan DESC;
*/

-- 테이블별 인덱스 효율성 확인
/*
SELECT
    schemaname,
    tablename,
    100 * idx_scan / (seq_scan + idx_scan) as index_usage_percent,
    n_tup_ins + n_tup_upd + n_tup_del as total_writes,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) as table_size
FROM pg_stat_user_tables
WHERE schemaname = 'public'
  AND (seq_scan + idx_scan) > 0
ORDER BY index_usage_percent DESC;
*/

-- =============================================
-- 13. Materialized View 새로고침 함수
-- =============================================

CREATE OR REPLACE FUNCTION refresh_active_places_mv()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_active_places;
END;
$$ LANGUAGE plpgsql;

-- 주기적으로 실행할 크론 작업 (pg_cron 확장 필요)
-- SELECT cron.schedule('refresh-active-places', '*/30 * * * *', 'SELECT refresh_active_places_mv();');

-- =============================================
-- 14. 쿼리 성능 힌트
-- =============================================

COMMENT ON INDEX idx_place_info_base_filter IS
'기본 검색 필터용 인덱스. 모든 검색 쿼리의 베이스';

COMMENT ON INDEX idx_place_info_rating_cursor IS
'평점순 정렬과 커서 페이징을 위한 인덱스';

COMMENT ON INDEX idx_place_locations_coordinates_gist IS
'PostGIS 공간 검색용. ST_DWithin 함수 사용 시 필수';

COMMENT ON INDEX idx_place_info_name_trgm IS
'텍스트 유사도 검색용. LIKE 또는 ILIKE 연산자 사용 시 활용';

-- =============================================
-- End of Optimization
-- =============================================