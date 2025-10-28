# DTO & Mapper 사용 가이드

## 구조 개요

```
dto/
├── request/           # 클라이언트 요청 DTO
│   ├── PlaceRegisterRequest
│   ├── PlaceUpdateRequest
│   ├── PlaceContactRequest
│   ├── PlaceLocationRequest
│   ├── PlaceParkingUpdateRequest
│   └── AddressRequest
│
└── response/          # 서버 응답 DTO
    ├── PlaceInfoResponse
    ├── PlaceInfoSummaryResponse
    ├── PlaceContactResponse
    ├── PlaceLocationResponse
    ├── PlaceParkingResponse
    ├── AddressResponse
    └── KeywordResponse

mapper/
└── PlaceMapper        # Entity <-> DTO 변환
```

## Request DTO

### PlaceRegisterRequest

업체 신규 등록 시 사용 (위치 정보와 이미지는 별도 API로 등록)

```json
{
  "placeOwnerId": "user123",
  "placeName": "강남 음악 연습실",
  "description": "전문 음악 장비를 갖춘 연습실",
  "category": "연습실",
  "placeType": "음악",
  "contact": {
    "contact": "02-1234-5678",
    "email": "contact@example.com",
    "websites": ["https://example.com"],
    "socialLinks": ["https://instagram.com/example"]
  },
  "parking": {
    "available": true,
    "parkingType": "FREE",
    "description": "건물 지하 주차장 2시간 무료"
  }
}
```

### PlaceUpdateRequest

업체 정보 수정 시 사용 (부분 업데이트 가능)

```json
{
  "placeName": "새로운 연습실명",
  "description": "업데이트된 설명",
  "contact": {
    "email": "newemail@example.com"
  }
}
```

## Response DTO

### PlaceInfoResponse

전체 상세 정보

```json
{
  "id": "place_123",
  "userId": "user123",
  "placeName": "강남 음악 연습실",
  "description": "전문 음악 장비를 갖춘 연습실",
  "category": "연습실",
  "placeType": "음악",
  "contact": {
    "contact": "02-1234-5678",
    "email": "contact@example.com",
    "websites": [
      "https://example.com"
    ],
    "socialLinks": [
      "https://instagram.com/example"
    ]
  },
  "location": {
    "address": {
      "province": "서울특별시",
      "city": "강남구",
      "district": "역삼동",
      "fullAddress": "서울특별시 강남구 테헤란로 123",
      "addressDetail": "테헤란빌딩 5층",
      "postalCode": "06234",
      "shortAddress": "서울특별시 강남구 역삼동"
    },
    "latitude": 37.5665,
    "longitude": 126.9780,
    "locationGuide": "강남역 3번 출구 도보 5분"
  },
  "parking": {
    "available": true,
    "parkingType": "FREE",
    "description": "건물 지하 주차장 2시간 무료"
  },
  "imageUrls": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ],
  "keywords": [
    {
      "id": 1,
      "name": "그랜드 피아노",
      "type": "INSTRUMENT_EQUIPMENT",
      "description": "고급 그랜드 피아노 구비",
      "displayOrder": 1
    }
  ],
  "isActive": true,
  "approvalStatus": "APPROVED",
  "ratingAverage": 4.5,
  "reviewCount": 120,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

### PlaceInfoSummaryResponse

목록 조회용 요약 정보

```json
{
  "id": "place_123",
  "placeName": "강남 음악 연습실",
  "category": "연습실",
  "placeType": "음악",
  "thumbnailUrl": "https://example.com/thumbnail.jpg",
  "shortAddress": "서울특별시 강남구 역삼동",
  "parkingAvailable": true,
  "ratingAverage": 4.5,
  "reviewCount": 120,
  "approvalStatus": "APPROVED",
  "isActive": true
}
```

## Mapper 사용법

### 서비스 레이어에서 사용

```java
@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceInfoRepository repository;
    private final PlaceMapper mapper;

    // 등록
    @Transactional
    public PlaceInfoResponse register(PlaceRegisterRequest request) {
        String id = generateId();
        PlaceInfo entity = mapper.toEntity(request, id);
        PlaceInfo saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    // 조회
    @Transactional(readOnly = true)
    public PlaceInfoResponse getPlace(String id) {
        PlaceInfo entity = repository.findById(id)
            .orElseThrow(() -> new NotFoundException());
        return mapper.toResponse(entity);
    }

    // 목록 조회
    @Transactional(readOnly = true)
    public List<PlaceInfoSummaryResponse> getPlaceList() {
        List<PlaceInfo> entities = repository.findAll();
        return mapper.toSummaryResponseList(entities);
    }

    // 수정
    @Transactional
    public PlaceInfoResponse update(String id, PlaceUpdateRequest request) {
        PlaceInfo entity = repository.findById(id)
            .orElseThrow(() -> new NotFoundException());
        mapper.updateEntity(entity, request);
        // 더티 체킹으로 자동 저장
        return mapper.toResponse(entity);
    }
}
```

### Controller에서 사용

```java
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @PostMapping
    public ResponseEntity<PlaceInfoResponse> registerPlace(
            @RequestBody PlaceRegisterRequest request) {
        PlaceInfoResponse response = placeService.register(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceInfoResponse> getPlace(@PathVariable String id) {
        PlaceInfoResponse response = placeService.getPlace(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PlaceInfoSummaryResponse>> getPlaceList() {
        List<PlaceInfoSummaryResponse> response = placeService.getPlaceList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaceInfoResponse> updatePlace(
            @PathVariable String id,
            @RequestBody PlaceUpdateRequest request) {
        PlaceInfoResponse response = placeService.update(id, request);
        return ResponseEntity.ok(response);
    }
}
```

## 장점

### 1. 관심사의 분리

- Request DTO: 클라이언트 입력 데이터
- Response DTO: 클라이언트 응답 데이터
- Entity: 비즈니스 로직과 영속성

### 2. 유지보수성

- 매핑 로직이 Mapper에 집중되어 관리 용이
- 서비스 코드가 간결하고 읽기 쉬움

### 3. 유연성

- 부분 업데이트 지원 (null 체크)
- 다양한 응답 형태 제공 (상세/요약)
- DTO 구조 변경 시 Mapper만 수정

### 4. 테스트 용이성

- Mapper 단독 테스트 가능
- 서비스 레이어 테스트 시 Mock 객체 활용 용이

## 개선 예정 사항

1. MapStruct 도입 검토 (자동 매핑)
2. DTO Validation 추가 (Bean Validation)
3. 페이징 응답 DTO 추가
4. 검색 조건 Request DTO 추가
