# Place Info Service - Database Schema

이 디렉토리는 Place Info Service의 데이터베이스 스키마 SQL 파일들을 포함합니다.

## 파일 구조

- `schema.sql` - 전체 스키마 생성을 위한 메인 스크립트
- `schema-place.sql` - 업체(Place) 도메인 관련 테이블 DDL
- `schema-room.sql` - 방(Room) 도메인 관련 테이블 DDL
- `data-keywords.sql` - 키워드 마스터 데이터 초기화 스크립트

## 테이블 목록

### Place Domain

- `place_info` - 업체 기본 정보
- `place_locations` - 업체 위치 정보
- `place_contacts` - 업체 연락처 정보
- `place_parkings` - 업체 주차 정보
- `place_images` - 업체 이미지
- `place_websites` - 업체 웹사이트 URL
- `place_social_links` - 업체 소셜 미디어 링크
- `keywords` - 키워드 마스터 데이터
- `place_keywords` - 업체-키워드 매핑 (다대다)

### Room Domain

- `rooms` - 방 기본 정보
- `room_images` - 방 이미지
- `room_keywords` - 방-키워드 매핑 (다대다)

## 데이터베이스 초기화

### PostgreSQL 사용시

```bash
# 전체 스키마 생성
psql -U username -d database_name -f schema.sql

# 또는 개별 실행
psql -U username -d database_name -f schema-place.sql
psql -U username -d database_name -f schema-room.sql
psql -U username -d database_name -f data-keywords.sql
```

### Spring Boot 자동 실행

`application.yml` 또는 `application.properties`에서 설정:

```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations:
        - classpath:sql/schema-place.sql
        - classpath:sql/schema-room.sql
      data-locations:
        - classpath:sql/data-keywords.sql
```

## 주요 기능

### 지리 데이터 지원

- PostgreSQL + PostGIS 사용시 `place_locations` 테이블의 `coordinates` 컬럼으로 위치 기반 검색 가능

### 자동 업데이트 트리거

- 모든 테이블의 `updated_at` 컬럼은 자동으로 업데이트됨

### 뷰 (View)

- `v_place_full_info` - 업체 전체 정보 조회
- `v_room_full_info` - 방 전체 정보 조회
- `v_place_room_summary` - 업체별 방 요약 정보

### 유틸리티 함수

- `reorder_room_images()` - 방 이미지 순서 재정렬
- `reorder_place_rooms()` - 업체 내 방 순서 재정렬
- `cleanup_inactive_data()` - 오래된 비활성 데이터 정리

## 엔티티 관계

```
PlaceInfo (1) ─────── (1) PlaceLocation
    │
    ├─ (1) ─────────── (1) PlaceContact
    │                         │
    │                         ├─ (*) PlaceWebsites
    │                         └─ (*) PlaceSocialLinks
    │
    ├─ (1) ─────────── (1) PlaceParking
    │
    ├─ (1) ─────────── (*) PlaceImages
    │
    ├─ (*) ─────────── (*) Keywords
    │
    └─ (1) ─────────── (*) Rooms
                             │
                             ├─ (*) RoomImages
                             └─ (*) ─────────── (*) Keywords
```

## 인덱스 전략

- Primary Key 인덱스 (자동 생성)
- Foreign Key 인덱스
- 검색 최적화 인덱스 (approval_status, is_active 등)
- 복합 인덱스 (place_id + status 등)
- 지리 인덱스 (coordinates - GIST)
- 텍스트 검색 인덱스 (GIN)
