# Service Layer - CQRS Pattern

## 개요

PlaceInfoServer의 Service Layer는 **CQRS (Command Query Responsibility Segregation)** 패턴을 적용하여 읽기와 쓰기 작업을 명확하게 분리합니다.

## 패키지 구조

```
service/
├── command/              # 쓰기 작업 (Command)
│   ├── PlaceRegisterService.java
│   ├── PlaceLocationUpdateService.java
│   └── PlaceImageUpdateService.java
├── query/                # 읽기 작업 (Query)
│   └── PlaceQueryService.java
└── mapper/               # DTO ↔ Entity 변환
    └── PlaceMapper.java
```

## CQRS 원칙

### Command (명령)

- **책임**: 데이터 변경 (Create, Update, Delete)
- **트랜잭션**: `@Transactional` (읽기/쓰기)
- **반환값**: 변경된 Entity 또는 ID
- **특징**:
	- 비즈니스 로직 실행
	- 데이터 검증
	- 이벤트 발행 (선택적)
	- Dirty Checking을 통한 자동 변경 감지

### Query (조회)

- **책임**: 데이터 조회 (Read)
- **트랜잭션**: `@Transactional(readOnly = true)`
- **반환값**: DTO (Response)
- **특징**:
	- 데이터베이스 읽기 최적화
	- 캐싱 적용 가능
	- 복잡한 검색 로직
	- 페이징 및 정렬

## Command Services

### 1. PlaceRegisterService

**위치**: `service/command/PlaceRegisterService.java`

**주요 기능**:

- `registerPlace()` - 장소 등록
- `updatePlace()` - 장소 정보 수정
- `deletePlace()` - 장소 삭제 (소프트 삭제)
- `activatePlace()` - 장소 활성화
- `deactivatePlace()` - 장소 비활성화
- `approvePlace()` - 장소 승인
- `rejectPlace()` - 장소 거부

**트랜잭션**:

```java

@Transactional
public PlaceInfoResponse registerPlace(PlaceRegisterRequest request) {
	// 1. ID 생성
	// 2. DTO → Entity 변환
	// 3. 저장
	// 4. Entity → Response DTO 변환
}
```

### 2. PlaceLocationUpdateService

**위치**: `service/command/PlaceLocationUpdateService.java`

**주요 기능**:

- `updateLocation()` - 장소 위치 정보 업데이트
- PostGIS Point 생성 및 저장

### 3. PlaceImageUpdateService

**위치**: `service/command/PlaceImageUpdateService.java`

**주요 기능**:

- `updateImage()` - 이미지 정보 업데이트 (Kafka Event 소비)
- 이벤트 기반 이미지 동기화

## Query Service

### PlaceQueryService

**위치**: `service/query/PlaceQueryService.java`

**주요 기능**:

- `search()` - 통합 검색
- `searchByLocation()` - 위치 기반 검색 (PostGIS)
- `searchByKeywords()` - 키워드 기반 검색
- `searchByRegion()` - 지역 기반 검색
- `getPopularPlaces()` - 인기 장소 조회
- `getRecentPlaces()` - 최신 장소 조회
- `countSearchResults()` - 검색 결과 개수 조회

**트랜잭션**:

```java

@Transactional(readOnly = true)
public class PlaceQueryService {
	// 읽기 전용 트랜잭션으로 성능 최적화
}
```

**캐싱 전략** (예정):

```java

@Cacheable(
		value = "placeLocationSearch",
		key = "#request.latitude + ':' + #request.longitude + ':' + #request.radiusInMeters",
		condition = "#request.cursor == null",
		unless = "#result.items.isEmpty()"
)
public PlaceSearchResponse searchByLocation(PlaceSearchRequest request) {
	// ...
}
```

## Mapper

### PlaceMapper

**위치**: `service/mapper/PlaceMapper.java`

**역할**: Anti-Corruption Layer

- Request DTO → Entity 변환
- Entity → Response DTO 변환
- Entity 업데이트 (Dirty Checking)

**주요 메서드**:

```java
PlaceInfo toEntity(PlaceRegisterRequest request, String generatedId);

PlaceInfoResponse toResponse(PlaceInfo entity);

void updateEntity(PlaceInfo entity, PlaceUpdateRequest request);
```

## Controller와의 연결

### PlaceSearchController (Query)

```java

@RestController
@RequestMapping("/api/v1/places/search")
public class PlaceSearchController {
	private final PlaceQueryService queryService;  // Query Service 주입
	
	@GetMapping
	public ResponseEntity<PlaceSearchResponse> search(...) {
		return ResponseEntity.ok(queryService.search(request));
	}
}
```

### PlaceRegisterController (Command)

```java

@RestController
@RequestMapping("/api/v1/places")
public class PlaceRegisterController {
	private final PlaceRegisterService commandService;  // Command Service 주입
	
	@PostMapping
	public ResponseEntity<PlaceInfoResponse> register(@Valid @RequestBody PlaceRegisterRequest req) {
		return ResponseEntity.ok(commandService.registerPlace(req));
	}
}
```

## CQRS 패턴의 이점

### 1. 명확한 책임 분리

- 읽기와 쓰기 로직이 명확하게 구분됨
- 코드 가독성 향상
- 유지보수 용이

### 2. 성능 최적화

- Query: `readOnly = true`로 최적화
- Command: Dirty Checking으로 효율적인 업데이트
- 독립적인 캐싱 전략 적용 가능

### 3. 확장성

- 읽기/쓰기 부하에 따라 독립적으로 스케일링 가능
- 읽기 전용 복제본(Replica) 활용 가능
- Event Sourcing 전환 가능

### 4. 테스트 용이성

- Command와 Query를 독립적으로 테스트
- Mock 객체 구성 간소화

## 모범 사례

### DO ✅

```java
// Command Service
@Transactional
public PlaceInfoResponse registerPlace(PlaceRegisterRequest request) {
	// 비즈니스 로직 실행
	PlaceInfo entity = placeMapper.toEntity(request, generatedId);
	PlaceInfo saved = repository.save(entity);
	return placeMapper.toResponse(saved);
}

// Query Service
@Transactional(readOnly = true)
public PlaceSearchResponse search(PlaceSearchRequest request) {
	// 검색 로직 실행
	return repository.searchWithCursor(request);
}
```

### DON'T ❌

```java
// ❌ Query Service에서 데이터 변경
@Transactional(readOnly = true)
public void updateViewCount(String placeId) {  // 잘못된 설계
	// Query Service에서는 데이터를 변경하면 안 됨
}

// ❌ Command Service에서 복잡한 조회
@Transactional
public List<PlaceInfo> searchComplexQuery(...) {  // 잘못된 설계
	// Command Service에서는 복잡한 조회를 수행하면 안 됨
}
```

## 마이그레이션 가이드

### 기존 코드에서 마이그레이션

```java
// Before
PlaceAdvancedSearchService searchService;  // 모호한 이름

// After
PlaceQueryService queryService;  // 명확한 Query 역할
PlaceRegisterService commandService;  // 명확한 Command 역할
```

## 향후 계획

### Phase 1 (현재)

- ✅ Service Layer의 Command/Query 분리
- ✅ 패키지 구조 정리
- ✅ Controller 레이어 적용

### Phase 2 (예정)

- [ ] Redis 캐싱 활성화 (Query)
- [ ] Command Handler 패턴 도입 (선택적)
- [ ] Query Object 명시화

### Phase 3 (장기)

- [ ] Event Sourcing 도입 검토
- [ ] CQRS 읽기 전용 데이터베이스 분리 (Read Replica)
- [ ] Materialized View 활용

## 참고 자료

- [Martin Fowler - CQRS](https://martinfowler.com/bliki/CQRS.html)
- [Microsoft - CQRS Pattern](https://docs.microsoft.com/en-us/azure/architecture/patterns/cqrs)
- [DDD Reference - Eric Evans](https://www.domainlanguage.com/ddd/reference/)
