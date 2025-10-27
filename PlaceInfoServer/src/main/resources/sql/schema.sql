-- =============================================
-- Place Info Service - Complete Database Schema
-- =============================================
-- 전체 데이터베이스 스키마 생성 스크립트
--
-- 실행 순서:
-- 1. schema-place.sql - 업체(Place) 관련 테이블
-- 2. schema-room.sql - 방(Room) 관련 테이블
-- 3. data-keywords.sql - 키워드 초기 데이터
--
-- 사용법:
-- psql -U username -d database_name -f schema.sql
-- =============================================

-- PostGIS extension 활성화 (PostgreSQL에서 지리 데이터 사용시)
-- CREATE EXTENSION IF NOT EXISTS postgis;

-- =============================================
-- INCLUDE: Place Domain Schema
-- =============================================
\i
schema-place.sql


-- =============================================
-- 추가 인덱스 및 성능 최적화
-- =============================================

-- 복합 인덱스: 자주 함께 조회되는 컬럼들
CREATE INDEX IF NOT EXISTS idx_place_info_active_approved
    ON place_info (is_active, approval_status) WHERE is_active = true AND approval_status = 'APPROVED';

CREATE INDEX IF NOT EXISTS idx_rooms_place_active_available
    ON rooms (place_id, is_active, status) WHERE is_active = true AND status = 'AVAILABLE';

-- 텍스트 검색을 위한 인덱스 (PostgreSQL 전용)
CREATE INDEX IF NOT EXISTS idx_place_info_name_gin
    ON place_info USING gin(to_tsvector('simple', place_name));

CREATE INDEX IF NOT EXISTS idx_place_info_description_gin
    ON place_info USING gin(to_tsvector('simple', description));

CREATE INDEX IF NOT EXISTS idx_rooms_name_gin
    ON rooms USING gin(to_tsvector('simple', room_name));

-- =============================================
-- 통계 수집 함수
-- =============================================

-- 업체별 통계 업데이트 함수
CREATE OR REPLACE FUNCTION update_place_statistics()
    RETURNS TRIGGER AS $$
BEGIN
    -- 방 추가/삭제시 업체 통계 업데이트
    IF TG_OP = 'INSERT' OR TG_OP = 'DELETE' THEN
        UPDATE place_info
        SET updated_at = CURRENT_TIMESTAMP
        WHERE id = COALESCE(NEW.place_id, OLD.place_id);
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

-- 통계 업데이트 트리거
CREATE TRIGGER trigger_update_place_statistics
    AFTER INSERT OR DELETE ON rooms
    FOR EACH ROW
EXECUTE FUNCTION update_place_statistics();

-- =============================================
-- 데이터베이스 관리용 프로시저
-- =============================================

-- 오래된 비활성 데이터 정리 프로시저
CREATE OR REPLACE PROCEDURE cleanup_inactive_data(
    p_days_old INTEGER DEFAULT 365
)
    LANGUAGE plpgsql
AS $$
DECLARE
    v_cutoff_date TIMESTAMP;
    v_deleted_places INTEGER := 0;
    v_deleted_rooms INTEGER := 0;
BEGIN
    v_cutoff_date := CURRENT_TIMESTAMP - INTERVAL '1 day' * p_days_old;

    -- 오래된 비활성 방 삭제
    DELETE
    FROM rooms
    WHERE is_active = false
      AND updated_at < v_cutoff_date;
    GET DIAGNOSTICS v_deleted_rooms = ROW_COUNT;

    -- 오래된 거부된 업체 삭제
    DELETE
    FROM place_info
    WHERE approval_status = 'REJECTED'
      AND updated_at < v_cutoff_date;
    GET DIAGNOSTICS v_deleted_places = ROW_COUNT;

    RAISE NOTICE 'Cleanup completed. Deleted % places and % rooms',
                 v_deleted_places, v_deleted_rooms;
END;
$$;

-- =============================================
-- 권한 설정 (필요시 수정)
-- =============================================
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;
-- GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO app_user;
-- GRANT EXECUTE ON ALL PROCEDURES IN SCHEMA public TO app_user;

-- =============================================
-- 초기 데이터 로드
-- =============================================
-- 키워드 초기 데이터 로드
\i
data-keywords.sql

-- =============================================
-- 스키마 버전 관리 테이블
-- =============================================
CREATE TABLE IF NOT EXISTS schema_version
(
    version    VARCHAR(20) PRIMARY KEY,
    description VARCHAR(200),
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 현재 버전 기록
INSERT INTO schema_version (version, description)
VALUES ('1.0.0', 'Initial schema for Place Info Service')
ON CONFLICT
    (version)
DO NOTHING;

-- =============================================
-- 완료 메시지
-- =============================================
DO $$
BEGIN
    RAISE NOTICE '=============================================';
    RAISE NOTICE 'Place Info Service Schema Setup Complete';
    RAISE NOTICE '=============================================';
    RAISE NOTICE 'Created tables: place_info, place_locations, place_contacts, ';
    RAISE NOTICE '               place_parkings, place_images, keywords,';
    RAISE NOTICE '               place_keywords, rooms, room_images, room_keywords';
    RAISE NOTICE 'Created views: v_place_full_info, v_room_full_info, v_place_room_summary';
    RAISE NOTICE 'Loaded initial keyword data';
    RAISE NOTICE '=============================================';
END $$;
