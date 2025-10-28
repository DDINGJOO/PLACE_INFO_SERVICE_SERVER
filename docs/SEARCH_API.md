# 공간 탐색 API 문서

## 개요

커서 기반 페이징과 다양한 검색 옵션을 지원하는 고성능 공간 탐색 API

## 특징

- **커서 기반 페이징**: 대용량 데이터의 빠른 탐색
- **위치 기반 검색**: PostGIS를 활용한 효율적인 지리 공간 검색
- **다양한 정렬 옵션**: 거리, 평점, 리뷰 수, 최신순, 이름순
- **Redis 캐싱**: 자주 조회되는 데이터 캐싱
- **최적화된 인덱스**: 빠른 쿼리 실행

## API 엔드포인트

### 1. 통합 검색

```
GET /api/v1/places/search
```

#### 요청 파라미터

| 파라미터             | 타입         | 필수 | 설명        | 기본값      |
|------------------|------------|----|-----------|----------|
| keyword          | String     | N  | 검색 키워드    | -        |
| placeName        | String     | N  | 장소명       | -        |
| category         | String     | N  | 카테고리      | -        |
| placeType        | String     | N  | 장소 타입     | -        |
| keywordIds       | List<Long> | N  | 키워드 ID 목록 | -        |
| parkingAvailable | Boolean    | N  | 주차 가능 여부  | -        |
| latitude         | Double     | N  | 위도        | -        |
| longitude        | Double     | N  | 경도        | -        |
| radius           | Integer    | N  | 검색 반경(미터) | 5000     |
| province         | String     | N  | 시/도       | -        |
| city             | String     | N  | 시/군/구     | -        |
| district         | String     | N  | 동/읍/면     | -        |
| sortBy           | Enum       | N  | 정렬 기준     | DISTANCE |
| sortDirection    | Enum       | N  | 정렬 방향     | ASC      |
| cursor           | String     | N  | 페이징 커서    | -        |
| size             | Integer    | N  | 페이지 크기    | 20       |

#### 정렬 기준 (sortBy)

- `DISTANCE`: 거리순 (위치 검색 시)
- `RATING`: 평점순
- `REVIEW_COUNT`: 리뷰 수순
- `CREATED_AT`: 최신순
- `PLACE_NAME`: 이름순

#### 응답 예시

```json
{
  "items": [
    {
      "id": "1832654789123456789",
      "placeName": "강남 뮤직 스튜디오",
      "description": "최고의 음향 시설을 갖춘 전문 합주실",
      "category": "합주실",
      "placeType": "음악",
      "fullAddress": "서울특별시 강남구 역삼동 테헤란로 123",
      "latitude": 37.5013,
      "longitude": 127.0392,
      "distance": 1234.56,
      "ratingAverage": 4.5,
      "reviewCount": 23,
      "parkingAvailable": true,
      "parkingType": "PAID",
      "thumbnailUrl": "https://images.example.com/thumb.jpg",
      "keywords": [
        "합주실",
        "녹음실",
        "주차 가능"
      ],
      "contact": "0212345678",
      "isActive": true,
      "approvalStatus": "APPROVED"
    }
  ],
  "nextCursor": "eyJsYXN0SWQiOiIxODMyNjU0Nzg5MTIzNDU2Nzg5IiwibGFzdFNvcnRWYWx1ZSI6MS4yMzQ1Nh0=",
  "hasNext": true,
  "count": 20,
  "totalCount": null,
  "metadata": {
    "searchTime": 45,
    "sortBy": "DISTANCE",
    "sortDirection": "ASC",
    "centerLat": 37.5665,
    "centerLng": 126.9780,
    "radiusInMeters": 5000,
    "appliedFilters": "parkingAvailable=true"
  }
}
```

### 2. 위치 기반 검색

```
POST /api/v1/places/search/location
```

#### 요청 본문

```json
{
  "latitude": 37.5665,
  "longitude": 126.9780,
  "radius": 3000,
  "keyword": "합주실",
  "keywordIds": [
    1,
    2,
    3
  ],
  "parkingAvailable": true,
  "cursor": null,
  "size": 20
}
```

### 3. 지역별 검색

```
GET /api/v1/places/search/region
```

#### 요청 파라미터

| 파라미터     | 타입      | 필수 | 설명     | 기본값 |
|----------|---------|----|--------|-----|
| province | String  | Y  | 시/도    | -   |
| city     | String  | N  | 시/군/구  | -   |
| district | String  | N  | 동/읍/면  | -   |
| cursor   | String  | N  | 페이징 커서 | -   |
| size     | Integer | N  | 페이지 크기 | 20  |

### 4. 인기 장소 조회

```
GET /api/v1/places/search/popular
```

#### 요청 파라미터

| 파라미터 | 타입      | 필수 | 설명    | 기본값 |
|------|---------|----|-------|-----|
| size | Integer | N  | 조회 개수 | 10  |

### 5. 최신 등록 장소 조회

```
GET /api/v1/places/search/recent
```

#### 요청 파라미터

| 파라미터 | 타입      | 필수 | 설명    | 기본값 |
|------|---------|----|-------|-----|
| size | Integer | N  | 조회 개수 | 10  |

### 6. 검색 결과 개수 조회

```
POST /api/v1/places/search/count
```

#### 요청 본문

PlaceSearchRequest와 동일한 구조

#### 응답 예시

```json
{
  "count": 42
}
```

## 커서 기반 페이징

### 개념

- 전통적인 offset/limit 방식 대신 커서를 사용
- 대용량 데이터에서도 일정한 성능 보장
- 실시간 데이터 변경에도 안정적

### 사용법

1. 첫 번째 요청: `cursor` 파라미터 없이 요청
2. 응답에서 `nextCursor` 값 확인
3. 다음 페이지 요청 시 `cursor` 파라미터에 `nextCursor` 값 전달
4. `hasNext`가 `false`가 될 때까지 반복

### 예시

```bash
# 첫 페이지
curl "http://localhost:8080/api/v1/places/search?keyword=합주실&size=20"

# 다음 페이지 (응답의 nextCursor 사용)
curl "http://localhost:8080/api/v1/places/search?keyword=합주실&size=20&cursor=eyJsYXN0SWQiOi..."
```

## 성능 최적화

### 인덱스 전략

- 기본 필터 복합 인덱스
- 정렬 기준별 커서 페이징 인덱스
- PostGIS 공간 인덱스
- 텍스트 검색을 위한 GIN 인덱스

### 캐싱 전략

| 캐시 이름               | TTL | 용도       |
|---------------------|-----|----------|
| placeLocationSearch | 5분  | 위치 기반 검색 |
| popularPlaces       | 30분 | 인기 장소    |
| keywordSearch       | 10분 | 키워드 검색   |
| regionSearch        | 15분 | 지역별 검색   |
| placeDetails        | 1시간 | 장소 상세 정보 |
| keywords            | 1일  | 키워드 목록   |

### 쿼리 성능 팁

1. **위치 검색**: 반경을 적절히 제한 (최대 50km)
2. **키워드 검색**: 너무 많은 키워드 동시 검색 지양
3. **정렬**: 인덱스가 있는 필드로 정렬
4. **페이지 크기**: 20-50 사이 권장

## 에러 처리

### 에러 코드

| 코드  | 설명          |
|-----|-------------|
| 400 | 잘못된 요청 파라미터 |
| 404 | 결과 없음       |
| 500 | 서버 내부 오류    |

### 에러 응답 예시

```json
{
  "error": {
    "code": "INVALID_LOCATION",
    "message": "유효하지 않은 위도/경도입니다",
    "timestamp": "2024-10-28T12:34:56Z"
  }
}
```

## 사용 예시

### JavaScript/TypeScript

```typescript
// 위치 기반 검색
const searchByLocation = async (lat: number, lng: number, radius: number) => {
    const response = await fetch('/api/v1/places/search/location', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            latitude: lat,
            longitude: lng,
            radius: radius,
            size: 20
        })
    });

    return await response.json();
};

// 커서 페이징
const fetchNextPage = async (cursor: string) => {
    const params = new URLSearchParams({
        keyword: '합주실',
        cursor: cursor,
        size: '20'
    });

    const response = await fetch(`/api/v1/places/search?${params}`);
    return await response.json();
};
```

### Java

```java
// RestTemplate 예시
RestTemplate restTemplate = new RestTemplate();

// 통합 검색
String url = UriComponentsBuilder
		.fromHttpUrl("http://localhost:8080/api/v1/places/search")
		.queryParam("keyword", "합주실")
		.queryParam("latitude", 37.5665)
		.queryParam("longitude", 126.9780)
		.queryParam("radius", 3000)
		.build()
		.toUriString();

PlaceSearchResponse response = restTemplate.getForObject(url, PlaceSearchResponse.class);
```

## 제약 사항

- 최대 페이지 크기: 100
- 최대 검색 반경: 50km
- 키워드 최대 개수: 10개
- API 호출 제한: 분당 100회 (IP당)

## 문의 및 지원

- API 문의: api@teambind.com
- 기술 지원: support@teambind.com
