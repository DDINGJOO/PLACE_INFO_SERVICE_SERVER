-- =============================================
-- V2: Add Registration Status Column
-- 등록 업체 우선 정렬을 위한 registration_status 컬럼 추가
-- =============================================

-- 1. 등록 상태 컬럼 추가
ALTER TABLE place_info
    ADD COLUMN registration_status VARCHAR(20) NOT NULL DEFAULT 'UNREGISTERED';

-- 2. 기본 인덱스 추가 (등록 상태 필터링용)
CREATE INDEX idx_place_info_registration_status
    ON place_info (registration_status);

-- 3. 복합 인덱스 추가 (등록 상태 우선 정렬 + 평점순)
CREATE INDEX idx_place_info_registration_rating
    ON place_info (registration_status DESC, rating_average DESC);

-- 4. 복합 인덱스 추가 (등록 상태 우선 정렬 + 리뷰수순)
CREATE INDEX idx_place_info_registration_review
    ON place_info (registration_status DESC, review_count DESC);

-- 5. 복합 인덱스 추가 (등록 상태 우선 정렬 + 최신순)
CREATE INDEX idx_place_info_registration_created
    ON place_info (registration_status DESC, created_at DESC);

-- 6. 컬럼 코멘트 추가
COMMENT ON COLUMN place_info.registration_status IS '업체 등록 상태 (REGISTERED: 정식 등록, UNREGISTERED: 미등록)';

-- =============================================
-- End of Migration
-- =============================================
