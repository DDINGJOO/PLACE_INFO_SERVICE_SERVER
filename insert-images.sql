-- =============================================
-- Place Images Insert Script
-- Date: 2025-12-07
-- Description: 이미지 데이터 삽입
-- =============================================

-- 먼저 place_images 테이블에 sequence 컬럼이 없다면 추가
ALTER TABLE place_images
    ADD COLUMN IF NOT EXISTS sequence BIGINT;

-- Place ID 249043122901184512의 이미지 데이터 삽입
-- 기존 이미지가 있다면 먼저 삭제 (선택사항)
-- DELETE FROM place_images WHERE place_info_id = 249043122901184512;

-- 이미지 데이터 삽입
INSERT INTO place_images (id, place_info_id, image_url, sequence)
VALUES
    -- 첫 번째 이미지: 프로필 이미지
    ('0035f8bb-d34e-4ba7-9ce4-442bc0ae3f39',
     249043122901184512,
     'https://teambind.co.kr:9200/images/PROFILE/2025/10/25/0035f8bb-d34e-4ba7-9ce4-442bc0ae3f39.webp',
     1),

    -- 두 번째 이미지: 포스트 이미지
    ('0f706420-2bb3-42bf-bb41-d2084883452b',
     249043122901184512,
     'http://teambind.co.kr:9200/images/POST/2025/11/30/0f706420-2bb3-42bf-bb41-d2084883452b.webp',
     2)
ON CONFLICT
    (id)
DO UPDATE SET
    place_info_id = EXCLUDED.place_info_id,
    image_url = EXCLUDED.image_url,
    sequence = EXCLUDED.sequence;

-- 삽입 결과 확인
SELECT *
FROM place_images
WHERE place_info_id = 249043122901184512
ORDER BY sequence;

-- Place Info가 존재하는지 확인
SELECT id, place_name, is_active
FROM place_info
WHERE id = 249043122901184512;
