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
- **버전**: 1.1.0

### 핵심 목적

마이크로서비스 아키텍처 환경에서 장소(Place) 정보를 전담 관리하는 서버입니다.

- 음악 연습실, 공연장, 스튜디오 등 장소 정보 관리
- PostGIS 기반 위치 검색 (반경 검색, 지역 검색)
- 복합 조건 검색 (키워드, 카테고리, 장르, 주차 가능 여부 등)
- **UseCase 패턴 기반 CQRS 적용 (v1.1)**
- 이벤트 기반 이미지 동기화 (Kafka)
- 소프트 삭제 및 승인 워크플로우

---

## 주요 기능

### 1. 장소 등록 및 관리

#### 장소 등록

- 필수 정보: 장소명, 위치, 연락처
- 선택 정보: 카테고리, 장르, 주차 정보, 키워드(최대 10개), 이미지(최대 10장)
- 자동 ID 생성 (Snowflake 알고리즘 - 64bit Long)
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

### 3. UseCase 패턴 (CQRS) - v1.1

UseCase 패턴을 적용하여 각 비즈니스 작업을 독립적인 UseCase로 분리했습니다.

#### Command UseCase (쓰기 작업)

각 작업이 하나의 UseCase로 분리되어 단일 책임 원칙(SRP)을 준수합니다:

- **RegisterPlaceUseCase**: 장소 등록
- **UpdatePlaceUseCase**: 장소 정보 수정
- **DeletePlaceUseCase**: 장소 삭제 (소프트 삭제)
- **ActivatePlaceUseCase**: 장소 활성화
- **DeactivatePlaceUseCase**: 장소 비활성화
- **ApprovePlaceUseCase**: 장소 승인
- **RejectPlaceUseCase**: 장소 거부

모든 Command UseCase는 `@Transactional`을 통해 트랜잭션을 관리하며, Dirty Checking을 통한 자동 변경 감지를 활용합니다.

```java

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterPlaceUseCase {
	private final PlaceInfoRepository placeInfoRepository;
	
	public PlaceInfoResponse execute(PlaceRegisterRequest request) {
		// 단일 책임: 장소 등록만 수행
		PlaceInfo placeInfo = PlaceMapper.toEntity(request);
		PlaceInfo saved = placeInfoRepository.save(placeInfo);
		return PlaceMapper.toResponse(saved);
	}
}
```

#### Query UseCase (읽기 작업)

읽기 전용 작업으로 `@Transactional(readOnly = true)`를 적용하여 성능을 최적화합니다:

- **GetPlaceDetailUseCase**: 장소 상세 조회
- **SearchPlacesUseCase**: 장소 검색 (위치/키워드/통합)
- **GetPlacesByUserUseCase**: 사용자별 장소 조회

```java

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPlaceDetailUseCase {
	private final PlaceInfoRepository placeInfoRepository;
	
	public PlaceInfoResponse execute(String placeId) {
		Long id = IdParser.parsePlaceId(placeId);
		PlaceInfo placeInfo = placeInfoRepository.findById(id)
				.orElseThrow(() -> new PlaceNotFoundException(placeId));
		return PlaceMapper.toResponse(placeInfo);
	}
}
```

#### 공통 유틸리티

- **IdParser**: String ID를 Long으로 안전하게 변환하는 유틸리티

```java
public class IdParser {
	public static Long parsePlaceId(String placeId) {
		try {
			return Long.parseLong(placeId);
		} catch (NumberFormatException e) {
			throw new InvalidRequestException("Invalid place ID format: " + placeId);
		}
	}
}
```

#### CQRS 장점

1. **단일 책임 원칙(SRP)**: 각 UseCase가 하나의 비즈니스 작업만 수행
2. **개방-폐쇄 원칙(OCP)**: 새로운 UseCase 추가 시 기존 코드 수정 불필요
3. **테스트 용이성**: UseCase별 독립 테스트 가능
4. **유지보수성**: 변경 영향 범위 최소화
5. **성능 최적화**: Command/Query 분리로 각각 최적화 가능
6. **확장성**: 독립적인 스케일링 가능

### 4. Value Object 패턴

#### Coordinates (좌표)

```java
Coordinates.of(37.5665,126.9780)

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

### 계층 구조 (v1.1)

```
┌─────────────────────────────────────────┐
│         Controller Layer                │
│  (PlaceRegisterController - Command)    │
│  (PlaceSearchController - Query)        │
│  (AdminController - Command)            │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      UseCase Layer (CQRS) - NEW!       │
│                                         │
│  Command UseCases:                      │
│    - RegisterPlaceUseCase               │
│    - UpdatePlaceUseCase                 │
│    - DeletePlaceUseCase                 │
│    - ActivatePlaceUseCase               │
│    - DeactivatePlaceUseCase             │
│    - ApprovePlaceUseCase                │
│    - RejectPlaceUseCase                 │
│                                         │
│  Query UseCases:                        │
│    - GetPlaceDetailUseCase              │
│    - SearchPlacesUseCase                │
│    - GetPlacesByUserUseCase             │
│                                         │
│  Common:                                │
│    - IdParser (유틸리티)                 │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Legacy Service Layer               │
│  (이미지 동기화 등 특수 목적)              │
│    - PlaceImageUpdateService            │
│    - PlaceLocationUpdateService         │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Event Layer (Kafka)                │
│  Consumer: PlaceImageEventConsumer      │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   Repository Layer (JPA + QueryDSL)     │
│  JpaRepository: 기본 CRUD                │
│  QueryDSL: 복잡한 검색 쿼리                 │
│  Custom Repository: 위치 검색             │
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

#### 1. UseCase 패턴 (v1.1)

각 비즈니스 작업을 독립적인 UseCase로 분리하여 단일 책임 원칙을 준수합니다.

**장점**:

- 높은 응집도, 낮은 결합도
- UseCase별 독립 테스트 가능
- 새로운 기능 추가 시 기존 코드 영향 최소화
- 명확한 비즈니스 의도 표현

#### 2. CQRS 패턴

- Command와 Query의 명확한 분리
- 읽기 최적화: `readOnly = true`, 캐싱
- 쓰기 최적화: Dirty Checking, 트랜잭션 관리
- 독립적인 스케일링 가능

#### 3. Domain-Driven Design (DDD)

- **Aggregate Root**: PlaceInfo
- **Entities**: PlaceLocation, PlaceContact, PlaceParking, PlaceImage
- **Value Objects**: Coordinates, Distance, PhoneNumber, Email, Url
- **Repository Pattern**: 도메인 중심 데이터 접근
- 연관관계 편의 메서드를 통한 일관성 보장

#### 4. Value Object 패턴

- 불변 객체 (Immutable)
- 자가 검증 (Self-Validation)
- 도메인 로직 캡슐화
- equals/hashCode 기반 동등성

#### 5. Strategy 패턴 (AddressParser)

주소 데이터 출처(카카오, 네이버, 수동입력)에 따라 다른 파싱 전략을 적용합니다.

**구조**:

```
AddressParser (Context)
└── AddressParsingStrategy (Interface)
    ├── KakaoAddressParsingStrategy
    ├── NaverAddressParsingStrategy
    └── ManualAddressParsingStrategy
```

**장점**:

- 외부 API 변경 시 해당 전략만 수정
- 새로운 주소 제공자 추가 용이 (OCP 준수)
- 전략별 독립 테스트 가능
- 클라이언트 코드 변경 없이 전략 교체

**사용 예시**:

```java

@Service
@RequiredArgsConstructor
public class AddressParser {
	private final Map<AddressSource, AddressParsingStrategy> strategies;
	
	public Address parse(AddressSource source, Object data) {
		AddressParsingStrategy strategy = strategies.get(source);
		return strategy.parse(data);
	}
}
```

#### 6. 이벤트 기반 아키텍처

- Kafka를 통한 비동기 이벤트 처리
- 느슨한 결합 (Loose Coupling)
- 이미지 서버와의 독립적 배포

#### 7. Factory 패턴

- 복잡한 엔티티 생성 로직을 Factory로 분리
- PlaceInfoFactory, PlaceLocationFactory 등
- 테스트 데이터 생성 용이

#### 8. Soft Delete 패턴

- 물리적 삭제 대신 논리적 삭제
- deleted_at 컬럼 기록
- @SQLDelete, @Where 어노테이션 활용

---

## 데이터베이스 스키마

### 핵심 엔티티

#### 1. place_info (장소 정보)

```sql
id
BIGINT PRIMARY KEY        -- Snowflake 64-bit Long ID
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
id
BIGINT AUTO_INCREMENT PRIMARY KEY
place_id            BIGINT UNIQUE NOT NULL       -- FK to place_info
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
id
BIGINT AUTO_INCREMENT PRIMARY KEY
place_id            BIGINT UNIQUE NOT NULL       -- FK to place_info
phone_number        VARCHAR(20)                  -- 전화번호
email               VARCHAR(100)                 -- 이메일
website             VARCHAR(200)                 -- 웹사이트
```

#### 4. place_parking (주차 정보)

```sql
id
BIGINT AUTO_INCREMENT PRIMARY KEY
place_id            BIGINT UNIQUE NOT NULL       -- FK to place_info
parking_available   BOOLEAN DEFAULT FALSE        -- 주차 가능 여부
parking_fee         VARCHAR(100)                 -- 주차 요금 정보
parking_description VARCHAR(500)                 -- 주차 설명
```

#### 5. place_image (이미지 정보)

```sql
id
BIGINT AUTO_INCREMENT PRIMARY KEY
place_id            BIGINT NOT NULL              -- FK to place_info
image_url           VARCHAR(500) NOT NULL        -- 이미지 URL
image_sequence      INT                          -- 순서 (0부터 시작)
is_main             BOOLEAN DEFAULT FALSE        -- 대표 이미지 여부

-- 인덱스
INDEX idx_place_id (place_id)
INDEX idx_sequence (place_id, image_sequence)
```

#### 6. keyword (키워드)

```sql
id
BIGINT AUTO_INCREMENT PRIMARY KEY
keyword_name        VARCHAR(50) UNIQUE NOT NULL  -- 키워드명
```

#### 7. place_keywords (장소-키워드 매핑)

```sql
place_id
BIGINT NOT NULL              -- FK to place_info
keyword_id          BIGINT NOT NULL              -- FK to keyword
PRIMARY KEY (place_id, keyword_id)

-- 인덱스
INDEX idx_keyword_id (keyword_id) -- 역방향 조회
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

#### PUT /api/v1/places/{placeId}/locations

**위치 정보 수정**

```http
Content-Type: application/json

Request Body:
{
  "from": "KAKAO",
  "addressData": {
    // 카카오/네이버 API 응답 JSON 원본
    "address_name": "서울특별시 종로구 세종대로 110",
    "road_address": {...},
    "x": "126.9780",
    "y": "37.5665"
  },
  "latitude": 37.5665,
  "longitude": 126.9780,
  "locationGuide": "3층 엘리베이터 앞"
}

Response: 200 OK
{
  "placeId": "place_5f98f3d1b3b94401"
}
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
- placeName: String (옵션) - 장소명 검색
- category: String (옵션) - 카테고리 필터
- placeType: String (옵션) - 장르 필터
- keywordIds: List<Long> (옵션) - 키워드 ID 목록
- parkingAvailable: Boolean (옵션) - 주차 가능 장소만
- latitude: Double (옵션) - 위도 (-90.0 ~ 90.0)
- longitude: Double (옵션) - 경도 (-180.0 ~ 180.0)
- radius: Integer (옵션, 기본값: 5000) - 검색 반경 (미터)
- province: String (옵션) - 시/도 (예: 서울특별시)
- city: String (옵션) - 시/군/구 (예: 종로구)
- district: String (옵션) - 동/읍/면 (예: 세종로)
- sortBy: String (기본값: DISTANCE) - 정렬 기준 (DISTANCE, RATING, REVIEW_COUNT, CREATED_AT, PLACE_NAME)
- sortDirection: String (기본값: ASC) - 정렬 방향 (ASC, DESC)
- cursor: String (옵션) - 페이징 커서
- size: int (기본값: 20) - 페이지 크기

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

#### POST /api/v1/places/search/location

**위치 기반 검색 (반경 검색)**

```http
Content-Type: application/json

Request Body:
{
  "latitude": 37.5665,
  "longitude": 126.9780,
  "radius": 3000,
  "keyword": "연습실",
  "keywordIds": [1, 2, 3],
  "parkingAvailable": true,
  "cursor": "eyJpZCI6InBsYWNlXzVmOThmM2QxYjNiOTQ0MDEiLCJj...",
  "size": 10
}

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

---

## 기술 스택

### Core

- **Spring Boot**: 3.5.7
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
- **Testcontainers**: 3.x (PostgreSQL, Kafka)
- **MockMvc**: Controller 레이어 테스트
- **AssertJ**: 유창한 assertion 라이브러리

---

## 테스트

### 테스트 전략

이 프로젝트는 **통합 테스트**와 **단위 테스트**를 통해 코드 품질과 안정성을 보장합니다.

#### 테스트 커버리지 (v1.1)

```
총 테스트 수: 459개
성공: 456개 (99.3%)
실패: 3개 (PlaceImageUpdateService - 별도 이슈)
건너뜀: 1개
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

#### 2. UseCase Layer (56개 테스트) - NEW in v1.1

**Command UseCases**:

- **RegisterPlaceUseCaseTest** (8개): 장소 등록 로직
- **UpdatePlaceUseCaseTest** (6개): 장소 수정 로직
- **DeletePlaceUseCaseTest** (6개): 소프트 삭제 로직
- **ActivatePlaceUseCaseTest** (9개): 활성화/비활성화 로직
- **ApprovePlaceUseCaseTest** (9개): 승인/거부 로직

**Query UseCases**:

- **GetPlaceDetailUseCaseTest** (8개): 상세 조회 로직
- **SearchPlacesUseCaseTest** (마이그레이션): 검색 로직

**Common**:

- **IdParserTest** (10개): ID 파싱 유틸리티

**실행**:

```bash
./gradlew test --tests "*usecase*"
```

#### 3. Service Layer (90개 테스트)

**PlaceAdvancedSearchServiceTest** (30+개):

- 통합 검색 로직 마이그레이션 (SearchPlacesUseCase)
- 위치 기반 검색 로직
- 커서 기반 페이징
- 정렬 테스트

**PlaceLocationUpdateServiceTest** (20개):

- 위치 정보 업데이트
- PostGIS Point 생성
- 좌표 검증

**PlaceImageUpdateServiceTest** (20개):

- 이미지 동기화 로직
- Kafka 이벤트 처리

**실행**:

```bash
./gradlew test --tests "*service*"
```

#### 4. Repository Layer (80개 테스트)

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

#### 5. Entity Layer (40개 테스트)

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

#### 6. Value Object Layer (40개 테스트)

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

### 테스트 환경 설정

#### Testcontainers 설정

```java
@SpringBootTest
@Import(JpaAuditingTestConfig.class)
public abstract class BaseIntegrationTest {
	
	private static final PostgreSQLContainer<?> postgresContainer;
	private static final KafkaContainer kafkaContainer;
	
	static {
		// PostgreSQL with PostGIS
		postgresContainer = new PostgreSQLContainer<>("postgis/postgis:16-3.4")
				.withDatabaseName("testdb")
				.withUsername("test")
				.withPassword("test");
		postgresContainer.start();
		
		// Kafka
		kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
		kafkaContainer.start();
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

  kafka:
    bootstrap-servers: ${TESTCONTAINERS_KAFKA_BOOTSTRAP_SERVERS}

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
./gradlew test --tests "*usecase*"
./gradlew test --tests "*service*"
./gradlew test --tests "*repository*"

# 특정 테스트 클래스
./gradlew test --tests RegisterPlaceUseCaseTest

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
5. **Mock vs Real**: UseCase는 Real, 외부 의존성은 Mock
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
SELECT *
FROM place_location
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
    ON place_info (is_active, approval_status, deleted_at, created_at DESC);

-- 커버링 인덱스
CREATE INDEX idx_place_info_covering
    ON place_info (id, place_name, category, rating_average, review_count) WHERE is_active = true AND approval_status = 'APPROVED' AND deleted_at IS NULL;
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
SELECT *
FROM place_info
ORDER BY
created_at DESC
OFFSET 10000 LIMIT 10;  // 10000개를 건너뛰어야 함

// ✅ Cursor 방식 (빠름)
SELECT *
FROM place_info
WHERE created_at < '2025-10-22T10:00:00'
ORDER BY
created_at DESC
LIMIT 10;  // 바로 조회
```

### 5. N+1 문제 방지

**Fetch Join 활용**:

```java
// ❌ N+1 문제 발생
List<PlaceInfo> places = placeRepository.findAll();
for(
PlaceInfo place :places){
		place.

getImages().

size();  // 각 장소마다 쿼리 발생
}

// ✅ Fetch Join으로 해결
@Query("SELECT p FROM PlaceInfo p " +
		"LEFT JOIN FETCH p.images " +
		"LEFT JOIN FETCH p.location " +
		"WHERE p.isActive = true")
List<PlaceInfo> findAllWithImages();
```

### 6. 배치 처리

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

# 2. Kafka 실행
docker run -d \
  --name kafka \
  -p 9092:9092 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  confluentinc/cp-kafka:7.4.0

# 3. 데이터베이스 스키마 생성
psql -h localhost -U postgres -d place_info -f src/main/resources/sql/schema.sql
psql -h localhost -U postgres -d place_info -f src/main/resources/sql/data.sql

# 4. 애플리케이션 실행
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

---

## 배포

### Docker Compose

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

  place-info-service:
    image: place-info-server:1.1.0
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_HOST=postgres-postgis
      - DATABASE_PORT=5432
      - DATABASE_NAME=place_info
      - DATABASE_USER_NAME=postgres
      - DATABASE_PASSWORD=${DATABASE_PASSWORD}
      - KAFKA_URL1=kafka1:9091
      - KAFKA_URL2=kafka2:9092
      - KAFKA_URL3=kafka3:9093
    depends_on:
      - postgres-postgis
    networks:
      - place-info-network
      - infra-network
    healthcheck:
      test: [ "CMD", "curl", "-f", "http://localhost:8080/actuator/health" ]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres-data:

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
docker build -t place-info-server:1.1.0 .

# 3. 인프라 시작
docker-compose up -d

# 4. 헬스 체크
curl http://localhost:8080/actuator/health

# 5. 로그 확인
docker-compose logs -f place-info-service
```

---

## 프로젝트 구조

```
src/main/java/com/teambind/placeinfoserver/place/
├── controller/
│   ├── PlaceRegisterController.java         # Command API
│   ├── PlaceSearchController.java           # Query API
│   └── AdminController.java                 # Admin API
│
├── service/
│   ├── usecase/                             # NEW in v1.1
│   │   ├── command/                         # Command UseCases
│   │   │   ├── RegisterPlaceUseCase.java
│   │   │   ├── UpdatePlaceUseCase.java
│   │   │   ├── DeletePlaceUseCase.java
│   │   │   ├── ActivatePlaceUseCase.java
│   │   │   ├── DeactivatePlaceUseCase.java
│   │   │   ├── ApprovePlaceUseCase.java
│   │   │   └── RejectPlaceUseCase.java
│   │   ├── query/                           # Query UseCases
│   │   │   ├── GetPlaceDetailUseCase.java
│   │   │   ├── SearchPlacesUseCase.java
│   │   │   └── GetPlacesByUserUseCase.java
│   │   └── common/                          # Common Utilities
│   │       └── IdParser.java
│   ├── command/                             # Legacy Services
│   │   ├── PlaceLocationUpdateService.java
│   │   └── PlaceImageUpdateService.java
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
│   ├── factory/                              # Factory Pattern
│   │   ├── PlaceInfoFactory.java
│   │   └── PlaceLocationFactory.java
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
│   │   ├── ErrorCode.java
│   │   ├── domain/
│   │   │   └── PlaceNotFoundException.java
│   │   └── application/
│   │       └── InvalidRequestException.java
│   ├── config/
│   │   ├── JpaConfig.java
│   │   ├── QuerydslConfig.java
│   │   └── KafkaConfig.java
│   └── util/
│       ├── generator/
│       │   ├── PrimaryKeyGenerator.java
│       │   └── Snowflake.java
│       ├── address/
│       │   ├── strategy/
│       │   │   ├── AddressParsingStrategy.java
│       │   │   ├── KakaoAddressParsingStrategy.java
│       │   │   ├── NaverAddressParsingStrategy.java
│       │   │   └── ManualAddressParsingStrategy.java
│       │   └── exception/
│       │       ├── AddressParsingException.java
│       │       └── UnsupportedAddressSourceException.java
│       ├── geometry/
│       │   └── GeometryUtil.java
│       ├── json/
│       │   ├── JsonUtil.java
│       │   └── JsonUtilWithObjectMapper.java
│       └── AddressParser.java
│
└── test/
    ├── controller/
    │   ├── PlaceRegisterControllerTest.java
    │   └── PlaceSearchControllerTest.java
    ├── usecase/                              # NEW in v1.1
    │   ├── command/
    │   │   ├── RegisterPlaceUseCaseTest.java
    │   │   ├── UpdatePlaceUseCaseTest.java
    │   │   ├── DeletePlaceUseCaseTest.java
    │   │   ├── ActivatePlaceUseCaseTest.java
    │   │   └── ApprovePlaceUseCaseTest.java
    │   ├── query/
    │   │   └── GetPlaceDetailUseCaseTest.java
    │   └── common/
    │       └── IdParserTest.java
    ├── service/
    │   ├── command/
    │   │   └── PlaceLocationUpdateServiceTest.java
    │   └── PlaceAdvancedSearchServiceTest.java
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

### v1.1 (2025-11-03)

#### 1. UseCase 패턴 도입 (CQRS)

**Before**:

```java
// 하나의 Service에 7개 메서드
@Service
public class PlaceRegisterService {
	public PlaceInfoResponse registerPlace(...)
	
	public PlaceInfoResponse updatePlace(...)
	
	public void deletePlace(...)
	
	public void activatePlace(...)
	
	public void deactivatePlace(...)
	
	public void approvePlace(...)
	
	public void rejectPlace(...)
}
```

**After**:

```java
// 7개의 독립적인 Command UseCase
@Service
public class RegisterPlaceUseCase { ...
}

@Service
public class UpdatePlaceUseCase { ...
}

@Service
public class DeletePlaceUseCase { ...
}

// Query UseCases
@Service
public class GetPlaceDetailUseCase { ...
}

@Service
public class SearchPlacesUseCase { ...
}
```

**장점**:

- 단일 책임 원칙(SRP) 준수
- 개방-폐쇄 원칙(OCP) 준수
- 높은 응집도, 낮은 결합도
- UseCase별 독립 테스트 가능
- 명확한 비즈니스 의도 표현

#### 2. Controller 리팩토링

Controller가 UseCase를 직접 호출하도록 변경:

```java

@RestController
@RequiredArgsConstructor
public class PlaceRegisterController {
	private final RegisterPlaceUseCase registerPlaceUseCase;
	private final UpdatePlaceUseCase updatePlaceUseCase;
	private final DeletePlaceUseCase deletePlaceUseCase;
	// ...
}
```

#### 3. 테스트 커버리지 확대

- UseCase별 통합 테스트 추가: 56개 케이스
- PlaceAdvancedSearchServiceTest 마이그레이션
- 총 459개 테스트 (99.3% 성공률)

#### 4. Exception 계층 구조화

```
PlaceException (부모)
├── Domain Exception
│   ├── PlaceNotFoundException
│   ├── PlaceAlreadyExistsException
│   └── InvalidPlaceStateException
└── Application Exception
    ├── InvalidRequestException
    ├── UnauthorizedAccessException
    └── ValidationFailedException
```

#### 5. Factory 패턴 적용

복잡한 엔티티 생성 로직을 Factory로 분리:

```java
public class PlaceInfoFactory {
	public static PlaceInfo create(PlaceRegisterRequest request) {
		// 복잡한 생성 로직
	}
}
```

#### 6. AddressParser Strategy 패턴

주소 파싱 로직에 Strategy 패턴 적용하여 확장성 개선

### v1.0 (2025-10-28)

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

#### 4. 테스트 안정성 개선

- Testcontainers Singleton 패턴 적용
- 121개 실패 → 0개 실패 (100% 성공률)
- BaseIntegrationTest 개선

---

## 문서

### 관련 문서

- [CQRS 패턴 가이드](docs/cqrs-pattern-guide.md)
- [UseCase 패턴 가이드](docs/usecase-pattern-guide.md)
- [API 명세서](docs/api-specification.md)

### 메타 정보

- **작성일**: 2025-10-28
- **최종 업데이트**: 2025-11-03
- **버전**: 1.1.0
- **저자**: DDING JOO
