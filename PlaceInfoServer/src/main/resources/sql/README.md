# PlaceInfoServer PostgreSQL 스키마

## 개요

이 디렉토리는 PlaceInfoServer의 PostgreSQL 데이터베이스 스키마 및 관련 SQL 스크립트를 포함합니다.

## 파일 구조

```
sql/
├── schema.sql              # 전체 데이터베이스 스키마 (테이블, 인덱스, 함수, 트리거)
├── data-keywords.sql       # 키워드 마스터 데이터 (60개 사전 정의 키워드)
├── data-sample.sql         # 개발/테스트용 샘플 데이터
└── README.md              # 이 파일
```

## 데이터베이스 요구사항

### PostgreSQL 버전

- PostgreSQL 14.0 이상 권장
- PostgreSQL 12.0 이상 필수

### 필수 확장 기능

```sql
CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";    -- UUID 생성 지원
CREATE
EXTENSION IF NOT EXISTS "postgis";      -- 지리 공간 데이터 지원
CREATE
EXTENSION IF NOT EXISTS "btree_gist";   -- GiST 인덱스 지원
```

## 데이터베이스 설정

### 1. 데이터베이스 생성

```bash
# PostgreSQL 슈퍼유저로 실행
createdb placeinfo_db -E UTF8
```

### 2. 스키마 적용

#### 직접 실행 (순서대로 실행)

```bash
# 1. 전체 스키마 생성
psql -U your_username -d placeinfo_db -f schema.sql

# 2. 키워드 마스터 데이터 삽입
psql -U your_username -d placeinfo_db -f data-keywords.sql

# 3. (선택) 샘플 데이터 삽입 (개발/테스트 환경)
psql -U your_username -d placeinfo_db -f data-sample.sql
```

**주의**: 반드시 위 순서대로 실행해야 합니다. (schema → keywords → sample)

## 주요 테이블 구조

### 메인 테이블

- `place_info` - 장소 정보 (Aggregate Root, **BIGINT PK - Snowflake ID**)
- `keywords` - 키워드 마스터 데이터 (BIGSERIAL PK)

### 연관 엔티티 테이블 (1:1)

- `place_contacts` - 연락처 정보 (BIGSERIAL PK, BIGINT FK)
- `place_locations` - 위치 정보 (PostGIS 지원, BIGSERIAL PK, BIGINT FK)
- `place_parkings` - 주차 정보 (BIGSERIAL PK, BIGINT FK)
- `place_images` - 이미지 정보 (VARCHAR(255) PK, BIGINT FK, 1:N)

### Element Collection 테이블

- `place_websites` - 웹사이트 목록 (place_contact_id FK)
- `place_social_links` - 소셜 미디어 링크 (place_contact_id FK)

### 조인 테이블 (N:M)

- `place_keywords` - 장소-키워드 매핑 (place_info_id, keyword_id 복합키)

## 주요 기능

### 1. 소프트 삭제 (Soft Delete)

```sql
-- place_info 테이블은 소프트 삭제 지원
-- deleted_at, deleted_by 컬럼 사용
-- JPA @SQLDelete, @Where 어노테이션과 연동
```

### 2. 자동 타임스탬프 업데이트

```sql
-- created_at, updated_at 자동 관리
-- 트리거를 통해 updated_at 자동 갱신
```

### 3. 지리 공간 검색

```sql
-- PostGIS를 이용한 위치 기반 검색
-- 예: 특정 지점에서 반경 5km 이내 장소 검색
SELECT *
FROM find_places_within_radius(37.5665, 126.9780, 5000);
```

### 4. 거리 계산

```sql
-- 두 지점 간 거리 계산 (미터 단위)
SELECT calculate_distance(37.5665, 126.9780, 37.4979, 127.0276);
```

## 키워드 카테고리

총 60개의 사전 정의된 키워드:

| 카테고리                 | 개수  | 설명                     |
|----------------------|-----|------------------------|
| SPACE_TYPE           | 10개 | 공간 유형 (합주실, 녹음실 등)     |
| INSTRUMENT_EQUIPMENT | 15개 | 악기/장비 (피아노, 드럼, 앰프 등)  |
| AMENITY              | 15개 | 편의시설 (주차, 화장실, 와이파이 등) |
| OTHER_FEATURE        | 20개 | 기타 특성 (가격, 운영시간, 예약 등) |

## 인덱스 전략

### 검색 성능 최적화

- `user_id`, `approval_status`, `is_active` - 필터링용
- `category`, `place_type` - 분류 검색용
- `rating_average` - 정렬용

### 지리 공간 인덱스

- `coordinates` - GiST 인덱스 (PostGIS)
- `latitude`, `longitude` - B-tree 인덱스 (간단한 범위 검색용)

### 위치 검색 최적화

- `province`, `city`, `district` - 지역별 검색
- `postal_code` - 우편번호 검색

## 샘플 쿼리

### 1. 활성화된 장소 조회 (키워드 포함)

```sql
SELECT pi.*,
       STRING_AGG(k.name, ', ') as keywords
FROM place_info pi
         LEFT JOIN place_keywords pk ON pi.id = pk.place_info_id
         LEFT JOIN keywords k ON pk.keyword_id = k.id
WHERE pi.is_active = true
  AND pi.deleted_at IS NULL
  AND pi.approval_status = 'APPROVED'
GROUP BY pi.id;
```

### 2. 반경 내 장소 검색

```sql
-- 강남역 기준 3km 이내 장소
SELECT pi.place_name,
       pl.full_address,
       ST_Distance(
               pl.coordinates,
               ST_MakePoint(127.0276, 37.4979) : :geography
       ) / 1000 as distance_km
FROM place_info pi
         JOIN place_locations pl ON pi.id = pl.place_info_id
WHERE ST_DWithin(
              pl.coordinates,
              ST_MakePoint(127.0276, 37.4979)::geography,
              3000 -- 3km
      )
ORDER BY distance_km;
```

### 3. 특정 키워드를 가진 장소 검색

```sql
SELECT DISTINCT pi.*
FROM place_info pi
         JOIN place_keywords pk ON pi.id = pk.place_info_id
         JOIN keywords k ON pk.keyword_id = k.id
WHERE k.name IN ('합주실', '녹음실', '24시간 운영')
  AND pi.is_active = true
  AND pi.deleted_at IS NULL;
```

## 주의사항

1. **PostGIS 필수**: 위치 기반 기능을 위해 PostGIS 확장이 반드시 필요합니다.
2. **SRID 4326**: 모든 지리 좌표는 WGS84 (SRID 4326) 기준입니다.
3. **소프트 삭제**: `place_info`의 deleted_at이 NULL이 아닌 레코드는 논리적으로 삭제된 것입니다.
4. **Snowflake ID**: `place_info.id`는 애플리케이션에서 Snowflake 알고리즘으로 생성됩니다.

## 성능 팁

1. **지리 공간 검색**: 큰 반경 검색 시 `ST_DWithin` 사용 (인덱스 활용)
2. **키워드 검색**: 많은 키워드 검색 시 EXISTS 서브쿼리 고려
3. **페이징**: 대량 데이터 조회 시 LIMIT/OFFSET 또는 커서 기반 페이징 사용
4. **통계 갱신**: 주기적으로 `ANALYZE` 명령 실행하여 쿼리 플래너 최적화

## 문의사항

데이터베이스 스키마 관련 문의사항이 있으시면 개발팀에 연락해주세요.
