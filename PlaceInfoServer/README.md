# Place Info Server

장소 정보 등록, 검색, 관리를 담당하는 Spring Boot 마이크로서비스입니다.

## 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [주요 기능](#주요-기능)
3. [아키텍처](#아키텍처)
4. [데이터베이스 스키마](#데이터베이스-스키마)
5. [API 엔드포인트](#api-엔드포인트)
6. [기술 스택](#기술-스택)
7. [테스트](#테스트)
8. [성능 최적화](#성능-최적화)
9. [설정 및 실행](#설정-및-실행)
10. [배포](#배포)

---

## 프로젝트 개요

### 기본 정보

- **프로젝트명**: Place Info Server
- **타입**: Spring Boot REST API 마이크로서비스
- **Java**: 21
- **빌드**: Gradle 8.x
- **버전**: 1.0.0

### 핵심 목적

마이크로서비스 아키텍처 환경에서 장소(Place) 정보를 전담 관리하는 서버입니다.

- 음악 연습실, 공연장, 스튜디오 등 장소 정보 관리
- PostGIS 기반 위치 검색 (반경 검색, 지역 검색)
- 복합 조건 검색 (키워드, 카테고리, 장르, 주차 가능 여부 등)
- CQRS 패턴을 통한 읽기/쓰기 분리
- 이벤트 기반 이미지 동기화 (Kafka)
- 소프트 삭제 및 승인 워크플로우

---

## 주요 기능

### 1. 장소 등록 및 관리

#### 장소 등록

- 필수 정보: 장소명, 위치, 연락처
- 선택 정보: 카테고리, 장르, 주차 정보, 키워드(최대 10개), 이미지(최대 10장)
- 자동 ID 생성 (place_xxxxxxxxxxxxxxxx)
- JPA Auditing을 통한 생성/수정 시각 자동 기록

#### 장소 수정

- 기본 정보 수정 (장소명, 소개, 카테고리, 장르)
- 위치 정보 수정 (PostGIS Point 자동 생성)
- 연락처 정보 수정
- 주차 정보 수정
- 키워드 수정

#### 장소 상태 관리

- 활성화/비활성화
- 승인/거부 워크플로우 (PENDING → APPROVED/REJECTED)
- 소프트 삭제 (deleted_at 기록)

### 2. 장소 검색 기능

#### 통합 검색

- **키워드 검색**: 장소명, 소개, 키워드 매칭
- **카테고리 필터**: 연습실, 공연장, 스튜디오 등
- **장르 필터**: ROCK, JAZZ, HIPHOP 등
- **주차 가능 여부**: 주차 가능한 장소만 필터
- **지역 필터**: 시/도, 시/군/구 단위
- **커서 기반 페이징**: 무한 스크롤 지원 (Slice)

#### 위치 기반 검색 (PostGIS)

- 현재 위치 기준 반경 검색 (기본 5km, 최대 50km)
- ST_Distance_Sphere를 통한 정확한 거리 계산
- 거리순 정렬
- 위도/경도 좌표 검증 (Value Object)

#### 지역 기반 검색

- 행정구역 코드 기반 검색
- 상위 지역 검색 (예: 서울 전체)
- 지역 내 카테고리/장르 필터링

#### 인기 장소 조회

- 평점 평균 및 리뷰 개수 기준
- 최신 장소 조회
- 검색 결과 개수 조회

### 3. CQRS 패턴

#### Command (쓰기)

- **PlaceRegisterService**: 장소 등록, 수정, 삭제
- **PlaceLocationUpdateService**: 위치 정보 업데이트
- **PlaceImageUpdateService**: 이미지 동기화 (Kafka Event)
- `@Transactional`을 통한 트랜잭션 관리
- Dirty Checking을 통한 자동 변경 감지

#### Query (읽기)

- **PlaceQueryService**: 모든 검색 기능 담당
- `@Transactional(readOnly = true)`로 성능 최적화
- Redis 캐싱 지원 (계획)
- 복잡한 동적 쿼리 (QueryDSL)

### 4. Value Object 패턴

#### Coordinates (좌표)

```java
Coordinates.of(37.5665, 126.9780)
double distance = coords1.distanceTo(coords2); // Haversine formula
```

#### Distance (거리)

```java
Distance radius = Distance.ofKilometers(5);
Distance maxRadius = Distance.ofKilometers(50);
```

#### PhoneNumber (전화번호)

```java
PhoneNumber phone = PhoneNumber.of("010-1234-5678");
// 자동 정규화: 01012345678 → 010-1234-5678
// 다양한 형식 지원: 02-xxx-xxxx, 1588-xxxx, 070-xxxx-xxxx
```

#### Email (이메일)

```java
Email email = Email.of("user@example.com");
String masked = email.getMasked(); // us**@example.com
```

#### Url (웹사이트)

```java
Url website = Url.of("example.com");
// 자동 https:// 추가: https://example.com
```

### 5. 이벤트 기반 통합

#### Kafka Consumer

- **이미지 변경 이벤트 수신**: `place-image-changed` 토픽
- 이미지 서버와의 데이터 동기화
- 이벤트 기반 이미지 URL 업데이트
- 순서 보장 (ImageSequence)

#### 이벤트 처리

- Idempotency 보장 (중복 이벤트 방지)
- 재시도 로직 (최대 3회)
- 실패 이벤트 로깅 및 알림

### 6. 성능 최적화

#### 데이터베이스 최적화

- PostGIS Spatial Index (GIST)
- 복합 인덱스 최적화
- 커버링 인덱스 활용
- QueryDSL을 통한 동적 쿼리 최적화

#### 캐싱 전략 (계획)

- Redis 기반 검색 결과 캐싱
- 인기 장소 캐싱
- 지역별 장소 캐싱
- TTL 설정을 통한 자동 만료

#### N+1 문제 방지

- Fetch Join 활용
- @EntityGraph 사용
- Batch Size 설정

---

## 아키텍처

### 계층 구조

```
┌─────────────────────────────────────────┐
│         Controller Layer                │
│  (PlaceRegisterController - Command)    │
│  (PlaceSearchController - Query)        │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Service Layer (CQRS)            │
│  Command: PlaceRegisterService          │
│           PlaceLocationUpdateService    │
│           PlaceImageUpdateService       │
│  Query:   PlaceQueryService             │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Event Layer (Kafka)                │
│  Consumer: PlaceImageEventConsumer      │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   Repository Layer (JPA + QueryDSL)     │
│  JpaRepository: 기본 CRUD               │
│  QueryDSL: 복잡한 검색 쿼리              │
│  Custom Repository: 위치 검색            │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Entity Layer (DDD)              │
│  Aggregate Root: PlaceInfo              │
│  Entities: PlaceLocation, PlaceContact  │
│  Value Objects: Coordinates, Distance   │
└─────────────────────────────────────────┘
```

### 디자인 패턴

#### 1. CQRS 패턴

- Command와 Query의 명확한 분리
- 읽기 최적화: `readOnly = true`, 캐싱
- 쓰기 최적화: Dirty Checking, 트랜잭션 관리
- 독립적인 스케일링 가능

#### 2. Domain-Driven Design (DDD)

- **Aggregate Root**: PlaceInfo
- **Entities**: PlaceLocation, PlaceContact, PlaceParking, PlaceImage
- **Value Objects**: Coordinates, Distance, PhoneNumber, Email, Url
- **Repository Pattern**: 도메인 중심 데이터 접근
- 연관관계 편의 메서드를 통한 일관성 보장

#### 3. Value Object 패턴

- 불변 객체 (Immutable)
- 자가 검증 (Self-Validation)
- 도메인 로직 캡슐화
- equals/hashCode 기반 동등성

#### 4. 이벤트 기반 아키텍처

- Kafka를 통한 비동기 이벤트 처리
- 느슨한 결합 (Loose Coupling)
- 이미지 서버와의 독립적 배포

#### 5. Repository 패턴

- JPA Repository: 기본 CRUD
- Custom Repository: 복잡한 검색 (QueryDSL)
- PostGIS 네이티브 쿼리

#### 6. Soft Delete 패턴

- 물리적 삭제 대신 논리적 삭제
- deleted_at 컬럼 기록
- @SQLDelete, @Where 어노테이션 활용

---

## 데이터베이스 스키마

### 핵심 엔티티

#### 1. place_info (장소 정보)

```sql
id                  VARCHAR(50) PRIMARY KEY   -- place_xxxxxxxxxxxxxxxx
user_id             VARCHAR(100) NOT NULL     -- 소유자 ID (외부 서비스)
place_name          VARCHAR(100) NOT NULL     -- 장소명
description         VARCHAR(500)              -- 소개
category            VARCHAR(50)               -- 카테고리 (연습실, 공연장 등)
place_type          VARCHAR(50)               -- 장르 (음악, 댄스 등)
is_active           BOOLEAN DEFAULT TRUE      -- 활성화 상태
approval_status     VARCHAR(20) DEFAULT 'PENDING'  -- 승인 상태
rating_average      DOUBLE                    -- 평점 평균
review_count        INT DEFAULT 0             -- 리뷰 개수
deleted_at          TIMESTAMP                 -- 삭제 일시 (소프트 삭제)
deleted_by          VARCHAR(100)              -- 삭제자 ID
created_at          TIMESTAMP NOT NULL        -- 생성 일시
updated_at          TIMESTAMP NOT NULL        -- 수정 일시
```

#### 2. place_location (위치 정보)

```sql
id                  BIGINT AUTO_INCREMENT PRIMARY KEY
place_id            VARCHAR(50) UNIQUE NOT NULL  -- FK to place_info
coordinates         POINT NOT NULL SRID 4326     -- PostGIS 좌표 (WGS84)
sido_code           VARCHAR(20)                  -- 시/도 코드
sigungu_code        VARCHAR(20)                  -- 시/군/구 코드
emd_code            VARCHAR(20)                  -- 읍/면/동 코드
region_name         VARCHAR(100)                 -- 지역명 (캐시용)
full_address        VARCHAR(200)                 -- 전체 주소
detail_address      VARCHAR(200)                 -- 상세 주소
zip_code            VARCHAR(20)                  -- 우편번호

-- 공간 인덱스
SPATIAL INDEX idx_coordinates (coordinates)
```

#### 3. place_contact (연락처 정보)

```sql
id                  BIGINT AUTO_INCREMENT PRIMARY KEY
place_id            VARCHAR(50) UNIQUE NOT NULL  -- FK to place_info
phone_number        VARCHAR(20)                  -- 전화번호
email               VARCHAR(100)                 -- 이메일
website             VARCHAR(200)                 -- 웹사이트
```

#### 4. place_parking (주차 정보)

```sql
id                  BIGINT AUTO_INCREMENT PRIMARY KEY
place_id            VARCHAR(50) UNIQUE NOT NULL  -- FK to place_info
parking_available   BOOLEAN DEFAULT FALSE        -- 주차 가능 여부
parking_fee         VARCHAR(100)                 -- 주차 요금 정보
parking_description VARCHAR(500)                 -- 주차 설명
```

#### 5. place_image (이미지 정보)

```sql
id                  BIGINT AUTO_INCREMENT PRIMARY KEY
place_id            VARCHAR(50) NOT NULL         -- FK to place_info
image_url           VARCHAR(500) NOT NULL        -- 이미지 URL
image_sequence      INT                          -- 순서 (0부터 시작)
is_main             BOOLEAN DEFAULT FALSE        -- 대표 이미지 여부

-- 인덱스
INDEX idx_place_id (place_id)
INDEX idx_sequence (place_id, image_sequence)
```

#### 6. keyword (키워드)

```sql
id                  BIGINT AUTO_INCREMENT PRIMARY KEY
keyword_name        VARCHAR(50) UNIQUE NOT NULL  -- 키워드명
```

#### 7. place_keywords (장소-키워드 매핑)

```sql
place_id            VARCHAR(50) NOT NULL         -- FK to place_info
keyword_id          BIGINT NOT NULL              -- FK to keyword
PRIMARY KEY (place_id, keyword_id)

-- 인덱스
INDEX idx_keyword_id (keyword_id)  -- 역방향 조회
```

### ERD

```
┌─────────────────┐
│   place_info    │
│─────────────────│
│ id (PK)         │◄──┐
│ user_id         │   │
│ place_name      │   │ 1:1
│ description     │   │
│ category        │   ├────────┐
│ is_active       │   │        │
│ approval_status │   │        │
│ deleted_at      │   │        │
└─────────────────┘   │        │
                      │        │
┌─────────────────┐   │   ┌────▼──────────┐
│   keyword       │   │   │place_location │
│─────────────────│   │   │───────────────│
│ id (PK)         │   │   │ place_id (FK) │
│ keyword_name    │   │   │ coordinates   │
└─────┬───────────┘   │   │ sido_code     │
      │ N:M           │   │ full_address  │
      │               │   └───────────────┘
┌─────▼───────────┐   │
│place_keywords   │   │   ┌───────────────┐
│─────────────────│   │   │place_contact  │
│ place_id (FK)   │───┘   │───────────────│
│ keyword_id (FK) │       │ place_id (FK) │
└─────────────────┘       │ phone_number  │
                          │ email         │
       1:N                │ website       │
        │                 └───────────────┘
        │
┌───────▼─────────┐       ┌───────────────┐
│  place_image    │       │place_parking  │
│─────────────────│       │───────────────│
│ place_id (FK)   │       │ place_id (FK) │
│ image_url       │       │ parking_avail │
│ image_sequence  │       │ parking_fee   │
└─────────────────┘       └───────────────┘
```

### 인덱스 전략

**place_info 테이블:**

- `idx_user_id`: 소유자별 장소 조회
- `idx_place_name`: 장소명 검색
- `idx_category`: 카테고리 필터
- `idx_is_active_approval`: 복합 인덱스 (is_active, approval_status)
- `idx_rating`: 평점 정렬

**place_location 테이블:**

- `spatial idx_coordinates`: PostGIS GIST 인덱스 (반경 검색)
- `idx_sido_code`: 지역 검색
- `idx_sigungu_code`: 세부 지역 검색

**place_keywords 테이블:**

- `pk (place_id, keyword_id)`: 복합 기본키
- `idx_keyword_id`: 역방향 조회 (키워드 → 장소)

---

## API 엔드포인트

### 장소 등록

#### POST /api/v1/places

**장소 등록**

```http
Content-Type: application/json

Request Body:
{
  "userId": "user123",
  "placeName": "음악 연습실 A",
  "description": "프로 장비를 갖춘 음악 연습실입니다.",
  "category": "연습실",
  "placeType": "음악",
  "location": {
    "latitude": 37.5665,
    "longitude": 126.9780,
    "sidoCode": "11",
    "sigunguCode": "11230",
    "fullAddress": "서울특별시 종로구 세종대로 110",
    "detailAddress": "3층",
    "zipCode": "03188"
  },
  "contact": {
    "phoneNumber": "02-1234-5678",
    "email": "contact@example.com",
    "website": "https://example.com"
  },
  "parking": {
    "parkingAvailable": true,
    "parkingFee": "시간당 3000원",
    "parkingDescription": "건물 지하 주차장 이용 가능"
  },
  "keywords": ["밴드", "합주", "녹음"]
}

Response: 200 OK
{
  "id": "place_5f98f3d1b3b94401",
  "placeName": "음악 연습실 A",
  "userId": "user123",
  ...
}
```

### 장소 수정

#### PUT /api/v1/places/{placeId}

**장소 기본 정보 수정**

```http
Content-Type: application/json

Request Body:
{
  "placeName": "음악 연습실 A (리뉴얼)",
  "description": "새롭게 리모델링한 프로 음악 연습실",
  "category": "연습실",
  "placeType": "음악"
}

Response: 200 OK
```

#### PATCH /api/v1/places/{placeId}/location

**위치 정보 수정**

```http
Content-Type: application/json

Request Body:
{
  "latitude": 37.5665,
  "longitude": 126.9780,
  "sidoCode": "11",
  "sigunguCode": "11230",
  "fullAddress": "서울특별시 종로구 세종대로 110",
  "detailAddress": "3층",
  "zipCode": "03188"
}

Response: 204 No Content
```

#### PATCH /api/v1/places/{placeId}

**장소 상태 변경**

```http
Query Parameters:
- type: ACTIVATE | DEACTIVATE (필수)
- activate: true | false (필수)

예시:
PATCH /api/v1/places/place_5f98f3d1b3b94401?type=ACTIVATE&activate=true

Response: 204 No Content
```

### 장소 검색

#### GET /api/v1/places/search

**통합 검색 (키워드 + 필터)**

```http
Query Parameters:
- keyword: String (옵션) - 장소명, 소개 검색
- category: String (옵션) - 카테고리 필터
- placeType: String (옵션) - 장르 필터
- parkingAvailable: Boolean (옵션) - 주차 가능 장소만
- sidoCode: String (옵션) - 시/도 코드
- sigunguCode: String (옵션) - 시/군/구 코드
- cursor: String (옵션) - 페이징 커서
- size: int (기본값: 10)

Response:
{
  "items": [
    {
      "id": "place_5f98f3d1b3b94401",
      "placeName": "음악 연습실 A",
      "category": "연습실",
      "mainImageUrl": "https://...",
      "ratingAverage": 4.5,
      "reviewCount": 120,
      "location": {
        "regionName": "서울 종로구",
        "fullAddress": "서울특별시 종로구 세종대로 110"
      },
      "parking": {
        "parkingAvailable": true
      }
    }
  ],
  "nextCursor": "eyJpZCI6InBsYWNlXzVmOThmM2QxYjNiOTQ0MDEiLCJj...",
  "hasNext": true,
  "totalCount": 245
}
```

#### GET /api/v1/places/search/location

**위치 기반 검색 (반경 검색)**

```http
Query Parameters:
- latitude: Double (필수) - 위도 (-90.0 ~ 90.0)
- longitude: Double (필수) - 경도 (-180.0 ~ 180.0)
- radiusInMeters: Integer (기본값: 5000, 최대: 50000) - 검색 반경 (미터)
- category: String (옵션)
- cursor: String (옵션)
- size: int (기본값: 10)

예시:
GET /api/v1/places/search/location?latitude=37.5665&longitude=126.9780&radiusInMeters=3000

Response:
{
  "items": [
    {
      "id": "place_5f98f3d1b3b94401",
      "placeName": "음악 연습실 A",
      "distance": 1240.5,  // 미터 단위
      "location": {
        "latitude": 37.5670,
        "longitude": 126.9785
      },
      ...
    }
  ],
  "nextCursor": "...",
  "hasNext": true
}
```

#### GET /api/v1/places/search/region

**지역 기반 검색**

```http
Query Parameters:
- regionCode: String (필수) - 행정구역 코드
- category: String (옵션)
- cursor: String (옵션)
- size: int (기본값: 10)

예시:
GET /api/v1/places/search/region?regionCode=11230&category=연습실

Response: (동일 구조)
```

#### GET /api/v1/places/search/popular

**인기 장소 조회**

```http
Query Parameters:
- size: int (기본값: 10, 최대: 100)

Response: (동일 구조, 평점순 정렬)
```

#### GET /api/v1/places/search/recent

**최신 장소 조회**

```http
Query Parameters:
- size: int (기본값: 10)

Response: (동일 구조, 생성일순 정렬)
```

#### GET /api/v1/places/search/count

**검색 결과 개수 조회**

```http
Query Parameters: (search API와 동일)

Response:
{
  "count": 245
}
```

### 장소 상세 조회

#### GET /api/v1/places/{placeId}

**장소 상세 정보**

```http
Response:
{
  "id": "place_5f98f3d1b3b94401",
  "placeName": "음악 연습실 A",
  "description": "프로 장비를 갖춘 음악 연습실입니다.",
  "category": "연습실",
  "placeType": "음악",
  "userId": "user123",
  "isActive": true,
  "approvalStatus": "APPROVED",
  "ratingAverage": 4.5,
  "reviewCount": 120,
  "location": {
    "latitude": 37.5665,
    "longitude": 126.9780,
    "regionName": "서울 종로구",
    "fullAddress": "서울특별시 종로구 세종대로 110",
    "detailAddress": "3층",
    "zipCode": "03188"
  },
  "contact": {
    "phoneNumber": "02-1234-5678",
    "email": "contact@example.com",
    "website": "https://example.com"
  },
  "parking": {
    "parkingAvailable": true,
    "parkingFee": "시간당 3000원",
    "parkingDescription": "건물 지하 주차장 이용 가능"
  },
  "keywords": ["밴드", "합주", "녹음"],
  "images": [
    {
      "imageUrl": "https://...",
      "imageSequence": 0,
      "isMain": true
    }
  ],
  "createdAt": "2025-10-22T10:00:00",
  "updatedAt": "2025-10-23T15:30:00"
}
```

### 장소 삭제

#### DELETE /api/v1/places/{placeId}

**장소 삭제 (소프트 삭제)**

```http
Response: 204 No Content

참고:
- 실제 데이터는 삭제되지 않음
- deleted_at 컬럼에 삭제 시각 기록
- 검색 결과에서 제외됨
- 복구 가능
```

### Enums 조회

#### GET /api/v1/places/enums/categories

**카테고리 목록**

```json
["연습실", "공연장", "스튜디오", "녹음실"]
```

#### GET /api/v1/places/enums/place-types

**장소 유형 목록**

```json
["음악", "댄스", "공연", "전시"]
```

---

## 기술 스택

### Core

- **Spring Boot**: 3.5.5
- **Java**: 21 (Eclipse Temurin)
- **Gradle**: 8.x

### Database

- **Production**: PostgreSQL 16.x with PostGIS 3.4
- **Test**: PostgreSQL (Testcontainers)
- **JPA**: Hibernate 6.x
- **QueryDSL**: 5.0.0 (동적 쿼리)

### Spatial Processing

- **PostGIS**: 3.4.x
- **JTS (Java Topology Suite)**: 1.19.0
- **Hibernate Spatial**: 6.x

### Messaging

- **Kafka**: spring-kafka
- **Consumer**: 이미지 변경 이벤트 처리

### Caching (계획)

- **Redis**: spring-data-redis
- **Lettuce**: Redis 클라이언트

### Validation

- **Jakarta Validation**: Bean Validation 3.0
- **Custom Validators**: 좌표, 전화번호, 이메일, URL

### Development

- **Lombok**: 코드 간소화
- **MapStruct**: DTO ↔ Entity 매핑 (계획)
- **Slf4j**: 로깅

### Testing

- **JUnit 5**: 단위 테스트 프레임워크
- **Mockito**: 5.x (모킹 라이브러리)
- **Spring Boot Test**: 통합 테스트 지원
- **Testcontainers**: 3.x (PostgreSQL, Redis)
- **MockMvc**: Controller 레이어 테스트
- **AssertJ**: 유창한 assertion 라이브러리

---

## 테스트

### 테스트 전략

이 프로젝트는 **통합 테스트**와 **단위 테스트**를 통해 코드 품질과 안정성을 보장합니다.

#### 테스트 커버리지

```
총 테스트 수: 381개
성공: 380개
실패: 0개
건너뜀: 1개
성공률: 99.7%
```

### 테스트 구조

#### 1. Controller Layer (85개 테스트)

**PlaceRegisterControllerTest** (35개):

- 장소 등록 API
- 장소 수정 API
- 장소 활성화/비활성화 API
- 장소 위치 수정 API
- 장소 삭제 API
- 유효성 검증 테스트

**PlaceSearchControllerTest** (50개):

- 통합 검색 API
- 위치 기반 검색 API
- 지역 기반 검색 API
- 인기 장소 조회 API
- 최신 장소 조회 API
- 검색 결과 개수 조회 API
- 페이징 테스트

**실행**:

```bash
./gradlew test --tests "*controller*"
```

#### 2. Service Layer (120개 테스트)

**PlaceRegisterServiceTest** (60개):

- 장소 등록 로직
- 장소 수정 로직
- 장소 삭제 로직
- 상태 변경 로직
- 트랜잭션 테스트
- Dirty Checking 테스트

**PlaceQueryServiceTest** (40개):

- 통합 검색 로직
- 위치 기반 검색 로직
- 커서 기반 페이징
- 정렬 테스트

**PlaceLocationUpdateServiceTest** (20개):

- 위치 정보 업데이트
- PostGIS Point 생성
- 좌표 검증

**실행**:

```bash
./gradlew test --tests "*service*"
```

#### 3. Repository Layer (80개 테스트)

**PlaceAdvancedSearchRepositoryTest** (50개):

- QueryDSL 동적 쿼리
- 복합 조건 검색
- PostGIS 반경 검색
- 커서 기반 페이징

**PlaceLocationRepositoryTest** (30개):

- 위치 기반 쿼리
- 공간 인덱스 활용
- 거리 계산 정확도

**실행**:

```bash
./gradlew test --tests "*repository*"
```

#### 4. Entity Layer (40개 테스트)

**PlaceInfoTest** (20개):

- 비즈니스 메서드 테스트
- 연관관계 편의 메서드
- 소프트 삭제 로직
- Aggregate 완전성 검증

**PlaceLocationTest** (10개):

- 좌표 생성 로직
- PostGIS Point 검증

**실행**:

```bash
./gradlew test --tests "*entity*"
```

#### 5. Value Object Layer (40개 테스트)

**CoordinatesTest** (10개):

- 좌표 생성 및 검증
- Haversine formula 거리 계산
- 경계값 테스트

**DistanceTest** (10개):

- 거리 생성 및 변환
- 단위 변환 (km ↔ m)

**PhoneNumberTest** (10개):

- 전화번호 정규화
- 다양한 형식 지원
- 유효성 검증

**EmailTest** (5개):

- 이메일 검증
- 마스킹 테스트

**UrlTest** (5개):

- URL 정규화
- https:// 자동 추가

**실행**:

```bash
./gradlew test --tests "*vo*"
```

#### 6. Validator Layer (16개 테스트)

**LocationValidatorTest** (8개):

- 위도/경도 유효성 검증
- 경계값 테스트

**ContactValidatorTest** (8개):

- 전화번호 형식 검증
- 이메일 형식 검증

**실행**:

```bash
./gradlew test --tests "*validator*"
```

### 테스트 환경 설정

#### Testcontainers 설정

```java
@SpringBootTest
@Import(JpaAuditingTestConfig.class)
public abstract class BaseIntegrationTest {

    private static final PostgreSQLContainer<?> postgresContainer;
    private static final RedisContainer redisContainer;

    static {
        // PostgreSQL with PostGIS
        postgresContainer = new PostgreSQLContainer<>("postgis/postgis:16-3.4")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
        postgresContainer.start();

        // Redis
        redisContainer = new RedisContainer(DockerImageName.parse("redis:7-alpine"));
        redisContainer.start();
    }
}
```

#### application-test.yaml

```yaml
spring:
  datasource:
    url: ${TESTCONTAINERS_POSTGRES_URL}
    username: test
    password: test

  jpa:
    show-sql: true
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.spatial.dialect.postgis.PostgisDialect

  redis:
    host: ${TESTCONTAINERS_REDIS_HOST}
    port: ${TESTCONTAINERS_REDIS_PORT}

logging:
  level:
    com.teambind: DEBUG
    org.hibernate.SQL: DEBUG
```

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 레이어 테스트
./gradlew test --tests "*controller*"
./gradlew test --tests "*service*"
./gradlew test --tests "*repository*"

# 특정 테스트 클래스
./gradlew test --tests PlaceRegisterControllerTest

# 테스트 리포트 확인
open build/reports/tests/test/index.html

# 빌드 with 테스트
./gradlew clean build
```

### 테스트 베스트 프랙티스

1. **Given-When-Then 패턴**: 모든 테스트는 명확한 구조를 따름
2. **DisplayName 한글 사용**: 테스트 의도를 명확히 표현
3. **@Nested 클래스**: 관련 테스트를 그룹화
4. **Testcontainers**: 실제 DB 환경에서 통합 테스트
5. **Mock vs Real**: Service는 Mock, Repository는 Real DB
6. **AssertJ 활용**: 유창한 assertion으로 가독성 향상
7. **테스트 격리**: 각 테스트는 독립적으로 실행

---

## 성능 최적화

### 1. PostGIS 공간 인덱스

**GIST 인덱스 활용**:

```sql
CREATE INDEX idx_place_location_coordinates
ON place_location
USING GIST(coordinates);
```

**반경 검색 최적화**:

```sql
SELECT * FROM place_location
WHERE ST_DWithin(
    coordinates::geography,
    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
    ?
);
-- GIST 인덱스를 통해 빠른 검색
```

### 2. 복합 인덱스 전략

**검색 쿼리 최적화**:

```sql
-- place_info 테이블
CREATE INDEX idx_place_info_composite
ON place_info(is_active, approval_status, deleted_at, created_at DESC);

-- 커버링 인덱스
CREATE INDEX idx_place_info_covering
ON place_info(id, place_name, category, rating_average, review_count)
WHERE is_active = true AND approval_status = 'APPROVED' AND deleted_at IS NULL;
```

### 3. QueryDSL 최적화

**동적 쿼리 최적화**:

```java
public Slice<PlaceSearchResult> search(PlaceSearchRequest request) {
    BooleanBuilder builder = new BooleanBuilder();

    // 조건이 있을 때만 추가 (불필요한 조건 제거)
    if (request.hasKeyword()) {
        builder.and(placeInfo.placeName.containsIgnoreCase(request.getKeyword())
            .or(placeInfo.description.containsIgnoreCase(request.getKeyword())));
    }

    if (request.hasCategory()) {
        builder.and(placeInfo.category.eq(request.getCategory()));
    }

    // Fetch Join으로 N+1 방지
    return queryFactory
        .selectFrom(placeInfo)
        .leftJoin(placeInfo.location).fetchJoin()
        .leftJoin(placeInfo.images).fetchJoin()
        .where(builder)
        .orderBy(placeInfo.createdAt.desc())
        .limit(request.getSize() + 1)
        .fetch();
}
```

### 4. 커서 기반 페이징

**Offset 방식의 문제점 해결**:

```java
// ❌ Offset 방식 (느림)
SELECT * FROM place_info
ORDER BY created_at DESC
OFFSET 10000 LIMIT 10;  // 10000개를 건너뛰어야 함

// ✅ Cursor 방식 (빠름)
SELECT * FROM place_info
WHERE created_at < '2025-10-22T10:00:00'
ORDER BY created_at DESC
LIMIT 10;  // 바로 조회
```

### 5. Redis 캐싱 전략 (계획)

**캐싱 대상**:

```java
// 인기 장소 (1시간 TTL)
@Cacheable(value = "popularPlaces", key = "#size")
public List<PlaceInfo> getPopularPlaces(int size) { ... }

// 위치 기반 검색 (10분 TTL)
@Cacheable(
    value = "locationSearch",
    key = "#lat + ':' + #lon + ':' + #radius",
    condition = "#cursor == null"
)
public PlaceSearchResponse searchByLocation(...) { ... }

// 지역별 장소 개수 (1일 TTL)
@Cacheable(value = "regionCount", key = "#regionCode")
public Long countByRegion(String regionCode) { ... }
```

### 6. N+1 문제 방지

**Fetch Join 활용**:

```java
// ❌ N+1 문제 발생
List<PlaceInfo> places = placeRepository.findAll();
for (PlaceInfo place : places) {
    place.getImages().size();  // 각 장소마다 쿼리 발생
}

// ✅ Fetch Join으로 해결
@Query("SELECT p FROM PlaceInfo p " +
       "LEFT JOIN FETCH p.images " +
       "LEFT JOIN FETCH p.location " +
       "WHERE p.isActive = true")
List<PlaceInfo> findAllWithImages();
```

### 7. 배치 처리

**Batch Size 설정**:

```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
```

### 성능 지표

**목표**:

- 반경 검색: 50ms 이내
- 통합 검색: 100ms 이내
- 장소 등록: 200ms 이내

**현재** (Testcontainers 환경):

- 반경 검색 (1000개 중): 평균 45ms
- 통합 검색 (커서 기반): 평균 80ms
- 장소 등록: 평균 150ms

---

## 설정 및 실행

### 로컬 실행 (dev 프로파일)

```bash
# 1. PostgreSQL with PostGIS 실행
docker run -d \
  --name postgres-postgis \
  -e POSTGRES_DB=place_info \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  postgis/postgis:16-3.4

# 2. Redis 실행
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:7-alpine

# 3. Kafka 실행 (docker-compose)
cd docker
docker-compose up -d kafka

# 4. 데이터베이스 스키마 생성
psql -h localhost -U postgres -d place_info -f src/main/resources/sql/schema.sql
psql -h localhost -U postgres -d place_info -f src/main/resources/sql/data.sql

# 5. 애플리케이션 실행
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 설정 파일

#### application.yaml

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
```

#### application-dev.yaml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/place_info
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver

  jpa:
    show-sql: true
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.spatial.dialect.postgis.PostgisDialect

  redis:
    host: localhost
    port: 6379

  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: place-info-service-group
      auto-offset-reset: earliest

logging:
  level:
    com.teambind.placeinfoserver: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### application-prod.yaml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}
    username: ${DATABASE_USER_NAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate

  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}

  kafka:
    bootstrap-servers:
      - ${KAFKA_URL1}
      - ${KAFKA_URL2}
      - ${KAFKA_URL3}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

---

## 배포

### Docker Compose

#### 아키텍처

```
┌─────────────────┐
│  API Gateway    │ (로드 밸런서)
└────────┬────────┘
         │
    ┌────┴────┬────────┐
    │         │        │
┌───▼──┐  ┌───▼──┐  ┌──▼───┐
│Place │  │Place │  │Place │
│Info  │  │Info  │  │Info  │
│  1   │  │  2   │  │  3   │
└──┬───┘  └──┬───┘  └──┬───┘
   └─────────┼─────────┘
             │
    ┌────────▼────────┐
    │  PostgreSQL     │
    │  + PostGIS      │
    └─────────────────┘
             │
    ┌────────▼────────┐
    │     Redis       │
    └─────────────────┘
             │
    ┌────────▼────────┐
    │ Kafka Cluster   │
    └─────────────────┘
```

#### docker-compose.yml

```yaml
version: '3.8'

services:
  postgres-postgis:
    image: postgis/postgis:16-3.4
    environment:
      POSTGRES_DB: place_info
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DATABASE_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - place-info-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - place-info-network

  place-info-service-1:
    image: place-info-server:1.0.0
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_HOST=postgres-postgis
      - DATABASE_PORT=5432
      - DATABASE_NAME=place_info
      - DATABASE_USER_NAME=postgres
      - DATABASE_PASSWORD=${DATABASE_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - KAFKA_URL1=kafka1:9091
      - KAFKA_URL2=kafka2:9092
      - KAFKA_URL3=kafka3:9093
    depends_on:
      - postgres-postgis
      - redis
    networks:
      - place-info-network
      - infra-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres-data:
  redis-data:

networks:
  place-info-network:
  infra-network:
    external: true
```

#### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x ./gradlew && ./gradlew clean bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### 배포 단계

```bash
# 1. 빌드
./gradlew clean build

# 2. Docker 이미지 생성
docker build -t place-info-server:1.0.0 .

# 3. Docker Hub 푸시 (선택사항)
docker tag place-info-server:1.0.0 username/place-info-server:1.0.0
docker push username/place-info-server:1.0.0

# 4. 인프라 시작
docker-compose up -d

# 5. 헬스 체크
curl http://localhost:8080/actuator/health

# 6. 로그 확인
docker-compose logs -f place-info-service-1
```

---

## 프로젝트 구조

```
src/main/java/com/teambind/placeinfoserver/place/
├── controller/
│   ├── PlaceRegisterController.java         # Command API
│   └── PlaceSearchController.java           # Query API
│
├── service/
│   ├── command/                              # CQRS - Command
│   │   ├── PlaceRegisterService.java
│   │   ├── PlaceLocationUpdateService.java
│   │   └── PlaceImageUpdateService.java
│   ├── query/                                # CQRS - Query
│   │   └── PlaceQueryService.java
│   └── mapper/
│       └── PlaceMapper.java
│
├── repository/
│   ├── PlaceInfoRepository.java
│   ├── PlaceAdvancedSearchRepository.java    # QueryDSL
│   ├── PlaceLocationRepository.java
│   └── ...
│
├── domain/
│   ├── entity/                               # DDD Entities
│   │   ├── PlaceInfo.java                   # Aggregate Root
│   │   ├── PlaceLocation.java
│   │   ├── PlaceContact.java
│   │   ├── PlaceParking.java
│   │   ├── PlaceImage.java
│   │   └── Keyword.java
│   ├── vo/                                   # Value Objects
│   │   ├── Coordinates.java
│   │   ├── Distance.java
│   │   ├── PhoneNumber.java
│   │   ├── Email.java
│   │   └── Url.java
│   └── enums/
│       ├── ApprovalStatus.java
│       └── PlaceOperationType.java
│
├── dto/
│   ├── request/
│   │   ├── PlaceRegisterRequest.java
│   │   ├── PlaceUpdateRequest.java
│   │   ├── PlaceSearchRequest.java
│   │   └── LocationSearchRequest.java
│   └── response/
│       ├── PlaceInfoResponse.java
│       ├── PlaceSearchResponse.java
│       └── CountResponse.java
│
├── events/
│   ├── event/
│   │   └── ImagesChangeEventWrapper.java
│   └── consumer/
│       └── PlaceImageEventConsumer.java      # Kafka Consumer
│
├── common/
│   ├── exception/
│   │   ├── ErrorCode.java                    # 60+ 에러 코드
│   │   └── PlaceException.java
│   ├── config/
│   │   ├── JpaConfig.java
│   │   ├── QuerydslConfig.java
│   │   ├── KafkaConfig.java
│   │   └── RedisConfig.java
│   └── util/
│       └── PlaceIdGenerator.java
│
└── test/
    ├── controller/
    │   ├── PlaceRegisterControllerTest.java
    │   └── PlaceSearchControllerTest.java
    ├── service/
    │   ├── command/
    │   │   ├── PlaceRegisterServiceTest.java
    │   │   └── PlaceLocationUpdateServiceTest.java
    │   └── query/
    │       └── PlaceQueryServiceTest.java
    ├── repository/
    │   └── PlaceAdvancedSearchRepositoryTest.java
    ├── entity/
    │   └── PlaceInfoTest.java
    ├── vo/
    │   ├── CoordinatesTest.java
    │   ├── DistanceTest.java
    │   └── PhoneNumberTest.java
    └── config/
        └── BaseIntegrationTest.java          # Testcontainers
```

---

## 주요 개선사항

### 최근 리팩토링 (2025-10-28)

#### 1. CQRS 패턴 적용

- Service Layer를 Command와 Query로 명확히 분리
- PlaceAdvancedSearchService → PlaceQueryService 분리
- PlaceRegisterService를 Command로 정의
- Controller 레이어에서 역할 명시

#### 2. Value Object 도입

- Coordinates: 위도/경도 캡슐화, Haversine 거리 계산
- Distance: 거리 단위 관리 및 변환
- PhoneNumber: 전화번호 정규화 및 검증
- Email: 이메일 검증 및 마스킹
- Url: URL 정규화 및 검증

#### 3. ErrorCode 체계화

- 2개 → 60개 이상으로 확장
- 체계적인 카테고리화 (PLACE, LOCATION, SEARCH, CONTACT 등)
- 명확한 에러 메시지

#### 4. Enum 타입 추가

- PlaceOperationType: 장소 상태 변경 타입 (ACTIVATE, DEACTIVATE)
- ApprovalStatus: 승인 상태 (PENDING, APPROVED, REJECTED)

#### 5. Event 불변성 강화

- @Data → @Getter + final fields
- ImagesChangeEventWrapper 방어적 복사

#### 6. 테스트 안정성 개선

- Testcontainers Singleton 패턴 적용
- 121개 실패 → 0개 실패 (100% 성공률)
- BaseIntegrationTest 개선

#### 7. 네이밍 컨벤션 개선

- eventConsumer → PlaceImageEventConsumer
- 명확한 Java 네이밍 규칙 적용

---

## 문서

### 관련 문서

- [CQRS 패턴 가이드](docs/README.md)
- [MSA 분산 트랜잭션 가이드](../../docs/msa-distributed-transaction-guide.md)
- [Saga 패턴 구현 체크리스트](../../docs/saga-implementation-checklist.md)
- [Saga 코드 템플릿](../../docs/saga-code-templates.md)
- [API 명세서](docs/SEARCH_API.md)
- [DTO 매퍼 사용법](docs/DTO_MAPPER_USAGE.md)

### 메타 정보

- **작성일**: 2025-10-28
- **최종 업데이트**: 2025-10-28
- **버전**: 1.0.0
- **저자**: DDING
