# PlaceInfo Service API 명세

## 개요

PlaceInfo 서비스는 음악 연습 공간, 공연장 등의 장소 정보를 관리하는 MSA 기반 서비스입니다.

- Base URL: `/api/v1`
- Content-Type: `application/json`
- 인코딩: `UTF-8`

## 목차

1. [공간 기본 조회 API](#1-공간-기본-조회-api)
2. [공간 검색 API](#2-공간-검색-api)
3. [공간 등록/수정/삭제 API](#3-공간-등록수정삭제-api)
4. [관리자 API](#4-관리자-api)
5. [키워드 API](#5-키워드-api)
6. [공통 응답 형식](#6-공통-응답-형식)
7. [Enum 타입 정의](#7-enum-타입-정의)

---

## 1. 공간 기본 조회 API

### 1.1. 공간 상세 조회

특정 ID를 가진 공간의 상세 정보를 조회합니다.

**Endpoint**

```
GET /places/{placeId}
```

**Path Parameters**

| 파라미터    | 타입     | 필수 | 설명    | 예시  |
|---------|--------|----|-------|-----|
| placeId | String | Y  | 공간 ID | "1" |

**Response (200 OK)**

```json
{
  "id": "1",
  "userId": "user123",
  "placeName": "음악 연습실 A",
  "description": "방음 시설이 완비된 24시간 연습실입니다",
  "category": "음악연습실",
  "placeType": "실내",
  "contact": {
    "contact": "02-1234-5678",
    "email": "contact@example.com",
    "websites": ["https://example.com"],
    "socialLinks": ["https://instagram.com/example"]
  },
  "location": {
    "address": {
      "province": "서울특별시",
      "city": "강남구",
      "district": "역삼동",
      "fullAddress": "서울특별시 강남구 역삼동 123-45",
      "addressDetail": "역삼빌딩 3층",
      "postalCode": "06234",
      "shortAddress": "강남구 역삼동"
    },
    "latitude": 37.5012345,
    "longitude": 127.0123456,
    "locationGuide": "지하철 2호선 역삼역 3번 출구"
  },
  "parking": {
    "available": true,
    "parkingType": "PAID",
    "description": "건물 지하 1층 유료 주차장 이용 가능"
  },
  "imageUrls": [
    "https://example.com/images/1.jpg",
    "https://example.com/images/2.jpg"
  ],
  "keywords": [
    {
      "id": 1,
      "name": "드럼",
      "type": "INSTRUMENT_EQUIPMENT",
      "description": "드럼 세트 구비",
      "displayOrder": 1
    },
    {
      "id": 2,
      "name": "앰프",
      "type": "INSTRUMENT_EQUIPMENT",
      "description": "기타 앰프 구비",
      "displayOrder": 2
    }
  ],
  "isActive": true,
  "approvalStatus": "APPROVED",
  "ratingAverage": 4.5,
  "reviewCount": 123,
  "roomCount": 3,
  "roomIds": [101, 102, 103],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-20T15:45:00"
}
```

**Error Responses**

| 상태 코드 | 설명         |
|-------|------------|
| 404   | 존재하지 않는 공간 |
| 500   | 서버 내부 오류   |

---

## 2. 공간 검색 API

### 2.1. 통합 검색

다양한 조건으로 공간을 검색합니다. 커서 기반 페이징을 지원합니다.

**Endpoint**

```
GET /places/search
```

**Query Parameters**

| 파라미터             | 타입               | 필수 | 설명                 | 기본값      | 예시                                                     |
|------------------|------------------|----|--------------------|----------|--------------------------------------------------------|
| keyword          | String           | N  | 검색 키워드 (장소명, 설명 등) | -        | "연습실"                                                  |
| placeName        | String           | N  | 장소명                | -        | "음악연습실"                                                |
| category         | String           | N  | 카테고리               | -        | "음악연습실"                                                |
| placeType        | String           | N  | 장소 타입              | -        | "실내"                                                   |
| keywordIds       | List&lt;Long&gt; | N  | 키워드 ID 목록 (콤마 구분)  | -        | "1,2,3"                                                |
| parkingAvailable | Boolean          | N  | 주차 가능 여부           | -        | true                                                   |
| latitude         | Double           | N  | 위도 (위치 기반 검색 시 필수) | -        | 37.5012345                                             |
| longitude        | Double           | N  | 경도 (위치 기반 검색 시 필수) | -        | 127.0123456                                            |
| radius           | Integer          | N  | 검색 반경 (미터)         | 5000     | 3000                                                   |
| province         | String           | N  | 시/도                | -        | "서울특별시"                                                |
| city             | String           | N  | 시/군/구              | -        | "강남구"                                                  |
| district         | String           | N  | 동/읍/면              | -        | "역삼동"                                                  |
| sortBy           | String           | N  | 정렬 기준              | DISTANCE | DISTANCE, RATING, REVIEW_COUNT, CREATED_AT, PLACE_NAME |
| sortDirection    | String           | N  | 정렬 방향              | ASC      | ASC, DESC                                              |
| cursor           | String           | N  | 페이징 커서 (다음 페이지)    | -        | "eyJpZCI6MTIzfQ=="                                     |
| size             | Integer          | N  | 페이지 크기             | 20       | 10                                                     |

**Response (200 OK)**

```json
{
  "items": [
    {
      "id": "1",
      "placeName": "음악 연습실 A",
      "description": "방음 시설이 완비된 24시간 연습실입니다",
      "category": "음악연습실",
      "placeType": "실내",
      "fullAddress": "서울특별시 강남구 역삼동 123-45",
      "latitude": 37.5012345,
      "longitude": 127.0123456,
      "distance": 1234.56,
      "ratingAverage": 4.5,
      "reviewCount": 123,
      "parkingAvailable": true,
      "parkingType": "PAID",
      "thumbnailUrl": "https://example.com/images/thumb1.jpg",
      "keywords": ["드럼", "앰프", "방음"],
      "contact": "02-1234-5678",
      "isActive": true,
      "approvalStatus": "APPROVED",
      "roomCount": 3,
      "roomIds": [101, 102, 103]
    }
  ],
  "nextCursor": "eyJpZCI6MTIzfQ==",
  "hasNext": true,
  "count": 20,
  "totalCount": 156,
  "metadata": {
    "searchTime": 45,
    "sortBy": "DISTANCE",
    "sortDirection": "ASC",
    "centerLat": 37.5012345,
    "centerLng": 127.0123456,
    "radiusInMeters": 5000,
    "appliedFilters": "keyword, parkingAvailable"
  }
}
```

**사용 예시**

```bash
# 키워드 검색
GET /api/v1/places/search?keyword=연습실&size=10

# 위치 기반 검색 (강남역 반경 3km)
GET /api/v1/places/search?latitude=37.4979&longitude=127.0276&radius=3000

# 주차 가능 장소 검색
GET /api/v1/places/search?parkingAvailable=true

# 복합 조건 검색
GET /api/v1/places/search?keyword=연습실&latitude=37.4979&longitude=127.0276&radius=3000&parkingAvailable=true&sortBy=RATING&sortDirection=DESC
```

---

### 2.2. 위치 기반 검색

특정 좌표를 중심으로 반경 내 장소를 검색합니다.

**Endpoint**

```
POST /places/search/location
```

**Request Body**

```json
{
  "latitude": 37.5012345,
  "longitude": 127.0123456,
  "radius": 5000,
  "keyword": "연습실",
  "keywordIds": [1, 2, 3],
  "parkingAvailable": true,
  "cursor": null,
  "size": 20
}
```

**필드 설명**

| 필드               | 타입               | 필수 | 제약조건           | 설명                    |
|------------------|------------------|----|----------------|-----------------------|
| latitude         | Double           | Y  | -90.0 ~ 90.0   | 위도                    |
| longitude        | Double           | Y  | -180.0 ~ 180.0 | 경도                    |
| radius           | Integer          | N  | 100 ~ 50000    | 검색 반경 (미터), 기본값: 5000 |
| keyword          | String           | N  | 최대 100자        | 검색 키워드                |
| keywordIds       | List&lt;Long&gt; | N  | 최대 20개         | 키워드 ID 목록             |
| parkingAvailable | Boolean          | N  | -              | 주차 가능 여부              |
| cursor           | String           | N  | -              | 페이징 커서                |
| size             | Integer          | N  | 1 ~ 100        | 페이지 크기, 기본값: 20       |

**Response (200 OK)**

PlaceSearchResponse와 동일 (2.1 참조)

**Validation 오류 (400 Bad Request)**

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "latitude",
      "rejectedValue": 95.0,
      "message": "위도는 90 이하여야 합니다"
    }
  ]
}
```

---

### 2.3. 지역별 검색

특정 행정구역 내 장소를 검색합니다.

**Endpoint**

```
GET /places/search/region
```

**Query Parameters**

| 파라미터     | 타입      | 필수 | 설명     | 예시      |
|----------|---------|----|--------|---------|
| province | String  | Y  | 시/도    | "서울특별시" |
| city     | String  | N  | 시/군/구  | "강남구"   |
| district | String  | N  | 동/읍/면  | "역삼동"   |
| cursor   | String  | N  | 페이징 커서 | -       |
| size     | Integer | N  | 페이지 크기 | 20      |

**Response (200 OK)**

PlaceSearchResponse와 동일 (2.1 참조)

**사용 예시**

```bash
# 서울특별시 전체 검색
GET /api/v1/places/search/region?province=서울특별시

# 강남구 전체 검색
GET /api/v1/places/search/region?province=서울특별시&city=강남구

# 역삼동 검색
GET /api/v1/places/search/region?province=서울특별시&city=강남구&district=역삼동
```

---

### 2.4. 인기 장소 조회

평점과 리뷰 기준으로 인기 장소를 조회합니다.

**Endpoint**

```
GET /places/search/popular
```

**Query Parameters**

| 파라미터 | 타입      | 필수 | 설명    | 기본값 |
|------|---------|----|-------|-----|
| size | Integer | N  | 조회 개수 | 10  |

**Response (200 OK)**

PlaceSearchResponse와 동일 (2.1 참조)

---

### 2.5. 최신 장소 조회

최근 등록된 장소를 조회합니다.

**Endpoint**

```
GET /places/search/recent
```

**Query Parameters**

| 파라미터 | 타입      | 필수 | 설명    | 기본값 |
|------|---------|----|-------|-----|
| size | Integer | N  | 조회 개수 | 10  |

**Response (200 OK)**

PlaceSearchResponse와 동일 (2.1 참조)

---

### 2.6. 검색 결과 개수 조회

검색 조건에 맞는 전체 결과 수를 반환합니다.

**Endpoint**

```
POST /places/search/count
```

**Request Body**

PlaceSearchRequest와 동일 (2.1의 Query Parameters를 JSON으로 전달)

**Response (200 OK)**

```json
{
  "count": 156
}
```

---

## 3. 공간 등록/수정/삭제 API

### 3.1. 공간 등록

새로운 공간을 등록합니다.

**Endpoint**

```
POST /places
```

**Request Body**

```json
{
  "placeOwnerId": "user123",
  "placeName": "음악 연습실 A",
  "description": "방음 시설이 완비된 24시간 연습실입니다",
  "category": "음악연습실",
  "placeType": "실내",
  "contact": {
    "contact": "02-1234-5678",
    "email": "contact@example.com",
    "websites": ["https://example.com"],
    "socialLinks": ["https://instagram.com/example"]
  },
  "location": {
    "from": "KAKAO",
    "addressData": {
      "address_name": "서울 강남구 역삼동 123-45",
      "road_address": {
        "address_name": "서울 강남구 테헤란로 123",
        "building_name": "역삼빌딩"
      }
    },
    "latitude": 37.5012345,
    "longitude": 127.0123456,
    "locationGuide": "지하철 2호선 역삼역 3번 출구"
  },
  "parking": {
    "available": true,
    "parkingType": "PAID",
    "description": "건물 지하 1층 유료 주차장 이용 가능"
  }
}
```

**필드 설명**

| 필드                     | 타입                 | 필수 | 제약조건                 | 설명               |
|------------------------|--------------------|----|----------------------|------------------|
| placeOwnerId           | String             | Y  | -                    | 장소 소유자 ID        |
| placeName              | String             | Y  | 최대 100자              | 장소명              |
| description            | String             | N  | 최대 1000자             | 설명               |
| category               | String             | N  | 최대 50자               | 카테고리             |
| placeType              | String             | N  | 최대 50자               | 장소 타입            |
| contact                | Object             | N  | -                    | 연락처 정보           |
| contact.contact        | String             | N  | -                    | 전화번호             |
| contact.email          | String             | N  | -                    | 이메일              |
| contact.websites       | List&lt;String&gt; | N  | -                    | 웹사이트 URL 목록      |
| contact.socialLinks    | List&lt;String&gt; | N  | -                    | 소셜 미디어 링크 목록     |
| location               | Object             | N  | -                    | 위치 정보            |
| location.from          | String             | Y  | KAKAO, NAVER, MANUAL | 주소 데이터 출처        |
| location.addressData   | Object             | N  | -                    | 외부 API 응답 원본 데이터 |
| location.latitude      | Double             | Y  | -90.0 ~ 90.0         | 위도               |
| location.longitude     | Double             | Y  | -180.0 ~ 180.0       | 경도               |
| location.locationGuide | String             | N  | 최대 500자              | 위치 안내            |
| parking                | Object             | N  | -                    | 주차 정보            |
| parking.available      | Boolean            | N  | -                    | 주차 가능 여부         |
| parking.parkingType    | String             | N  | FREE, PAID           | 주차 타입            |
| parking.description    | String             | N  | -                    | 주차 안내            |

**Response (200 OK)**

PlaceInfoResponse와 동일 (1.1 참조)

**Validation 오류 (400 Bad Request)**

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "placeName",
      "rejectedValue": "",
      "message": "장소명은 필수입니다"
    },
    {
      "field": "location.latitude",
      "rejectedValue": null,
      "message": "위도는 필수입니다"
    }
  ]
}
```

---

### 3.2. 공간 활성화/비활성화

공간의 활성화 상태를 변경합니다.

**Endpoint**

```
PATCH /places/{placeId}
```

**Path Parameters**

| 파라미터    | 타입     | 필수 | 설명    |
|---------|--------|----|-------|
| placeId | String | Y  | 공간 ID |

**Query Parameters**

| 파라미터     | 타입      | 필수 | 설명                              | 예시       |
|----------|---------|----|---------------------------------|----------|
| type     | String  | Y  | 작업 타입 (ACTIVATE만 지원)            | ACTIVATE |
| activate | Boolean | Y  | 활성화 여부 (true: 활성화, false: 비활성화) | true     |

**Response (204 No Content)**

응답 바디 없음

**사용 예시**

```bash
# 활성화
PATCH /api/v1/places/1?type=ACTIVATE&activate=true

# 비활성화
PATCH /api/v1/places/1?type=ACTIVATE&activate=false
```

**Error Responses**

| 상태 코드 | 설명                             |
|-------|--------------------------------|
| 400   | 잘못된 요청 (type이 ACTIVATE가 아닌 경우) |
| 404   | 존재하지 않는 공간                     |

---

### 3.3. 공간 위치 정보 수정

공간의 위치 정보를 수정합니다.

**Endpoint**

```
PUT /places/{placeId}/locations
```

**Path Parameters**

| 파라미터    | 타입     | 필수 | 설명    |
|---------|--------|----|-------|
| placeId | String | Y  | 공간 ID |

**Request Body**

PlaceLocationRequest와 동일 (3.1의 location 필드 참조)

**Response (200 OK)**

```json
{
  "placeId": "1"
}
```

---

### 3.4. 공간 삭제 (소프트 삭제)

공간을 삭제합니다. (소프트 삭제)

**Endpoint**

```
DELETE /places/{placeId}
```

**Path Parameters**

| 파라미터    | 타입     | 필수 | 설명    |
|---------|--------|----|-------|
| placeId | String | Y  | 공간 ID |

**Response (204 No Content)**

응답 바디 없음

**참고사항**

- 이 API는 소유자 권한으로 삭제합니다 (deletedBy: "OWNER")
- 실제 데이터는 삭제되지 않고 isActive = false로 변경됩니다

---

## 4. 관리자 API

### 4.1. 공간 승인/거부

관리자가 등록된 공간을 승인하거나 거부합니다.

**Endpoint**

```
PATCH /admin/places/{placeId}
```

**Path Parameters**

| 파라미터    | 타입     | 필수 | 설명    |
|---------|--------|----|-------|
| placeId | String | Y  | 공간 ID |

**Query Parameters**

| 파라미터     | 타입      | 필수 | 설명                          | 예시      |
|----------|---------|----|-----------------------------|---------|
| type     | String  | Y  | 작업 타입 ("approve"만 지원)       | approve |
| contents | Boolean | Y  | 승인 여부 (true: 승인, false: 거부) | true    |

**Response (204 No Content)**

응답 바디 없음

**사용 예시**

```bash
# 승인
PATCH /api/v1/admin/places/1?type=approve&contents=true

# 거부
PATCH /api/v1/admin/places/1?type=approve&contents=false
```

**Error Responses**

| 상태 코드 | 설명                              |
|-------|---------------------------------|
| 400   | 잘못된 요청 (type이 "approve"가 아닌 경우) |
| 403   | 권한 없음 (관리자 아닌 경우)               |
| 404   | 존재하지 않는 공간                      |

---

### 4.2. 공간 삭제 (관리자)

관리자가 공간을 삭제합니다.

**Endpoint**

```
DELETE /admin/places/{placeId}
```

**Path Parameters**

| 파라미터    | 타입     | 필수 | 설명    |
|---------|--------|----|-------|
| placeId | String | Y  | 공간 ID |

**Response (204 No Content)**

응답 바디 없음

**참고사항**

- 이 API는 관리자 권한으로 삭제합니다 (deletedBy: "ADMIN")
- 실제 데이터는 삭제되지 않고 isActive = false로 변경됩니다

---

## 5. 키워드 API

### 5.1. 키워드 목록 조회

활성화된 키워드 목록을 조회합니다. 타입별 필터링을 지원합니다.

**Endpoint**

```
GET /keywords
```

**Query Parameters**

| 파라미터 | 타입     | 필수 | 설명        | 예시                                                       |
|------|--------|----|-----------|----------------------------------------------------------|
| type | String | N  | 키워드 타입 필터 | SPACE_TYPE, INSTRUMENT_EQUIPMENT, AMENITY, OTHER_FEATURE |

**Response (200 OK)**

```json
[
  {
    "id": 1,
    "name": "드럼",
    "type": "INSTRUMENT_EQUIPMENT",
    "description": "드럼 세트 구비",
    "displayOrder": 1
  },
  {
    "id": 2,
    "name": "앰프",
    "type": "INSTRUMENT_EQUIPMENT",
    "description": "기타 앰프 구비",
    "displayOrder": 2
  },
  {
    "id": 3,
    "name": "연습실",
    "type": "SPACE_TYPE",
    "description": "음악 연습 공간",
    "displayOrder": 1
  }
]
```

**사용 예시**

```bash
# 전체 키워드 조회
GET /api/v1/keywords

# 악기/장비 키워드만 조회
GET /api/v1/keywords?type=INSTRUMENT_EQUIPMENT

# 편의시설 키워드만 조회
GET /api/v1/keywords?type=AMENITY
```

---

## 6. 공통 응답 형식

### 6.1. 성공 응답

대부분의 GET/POST 요청은 200 OK와 함께 JSON 데이터를 반환합니다.
PATCH/DELETE 요청은 204 No Content를 반환합니다.

### 6.2. 오류 응답

**Validation 오류 (400 Bad Request)**

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "필드명",
      "rejectedValue": "거부된 값",
      "message": "오류 메시지"
    }
  ]
}
```

**리소스 없음 (404 Not Found)**

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "리소스를 찾을 수 없습니다"
}
```

**서버 오류 (500 Internal Server Error)**

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "서버 내부 오류가 발생했습니다"
}
```

---

## 7. Enum 타입 정의

### 7.1. KeywordType (키워드 타입)

| 값                    | 설명    |
|----------------------|-------|
| SPACE_TYPE           | 공간 유형 |
| INSTRUMENT_EQUIPMENT | 악기/장비 |
| AMENITY              | 편의시설  |
| OTHER_FEATURE        | 기타 특성 |

### 7.2. ApprovalStatus (승인 상태)

| 값        | 설명    |
|----------|-------|
| PENDING  | 승인 대기 |
| APPROVED | 승인 완료 |
| REJECTED | 승인 거부 |

### 7.3. ParkingType (주차 타입)

| 값    | 설명 |
|------|----|
| FREE | 무료 |
| PAID | 유료 |

### 7.4. AddressSource (주소 출처)

| 값      | 설명         |
|--------|------------|
| KAKAO  | 카카오 주소 API |
| NAVER  | 네이버 주소 API |
| MANUAL | 수동 입력      |

### 7.5. PlaceOperationType (장소 작업 타입)

| 값        | 설명       |
|----------|----------|
| ACTIVATE | 활성화/비활성화 |

### 7.6. SortBy (정렬 기준)

| 값            | 설명               |
|--------------|------------------|
| DISTANCE     | 거리순 (위치 기반 검색 시) |
| RATING       | 평점순              |
| REVIEW_COUNT | 리뷰 개수순           |
| CREATED_AT   | 등록일순             |
| PLACE_NAME   | 이름순              |

### 7.7. SortDirection (정렬 방향)

| 값    | 설명   |
|------|------|
| ASC  | 오름차순 |
| DESC | 내림차순 |

---

## 부록: 응답 객체 상세

### PlaceInfoResponse

공간 상세 정보 응답 객체

| 필드             | 타입                          | Nullable | 설명         |
|----------------|-----------------------------|----------|------------|
| id             | String                      | N        | 공간 ID      |
| userId         | String                      | N        | 사용자 ID     |
| placeName      | String                      | N        | 장소명        |
| description    | String                      | Y        | 설명         |
| category       | String                      | Y        | 카테고리       |
| placeType      | String                      | Y        | 장소 타입      |
| contact        | PlaceContactResponse        | Y        | 연락처 정보     |
| location       | PlaceLocationResponse       | Y        | 위치 정보      |
| parking        | PlaceParkingResponse        | Y        | 주차 정보      |
| imageUrls      | List&lt;String&gt;          | Y        | 이미지 URL 목록 |
| keywords       | List&lt;KeywordResponse&gt; | Y        | 키워드 목록     |
| isActive       | Boolean                     | N        | 활성화 여부     |
| approvalStatus | ApprovalStatus              | N        | 승인 상태      |
| ratingAverage  | Double                      | Y        | 평균 평점      |
| reviewCount    | Integer                     | Y        | 리뷰 개수      |
| roomCount      | Integer                     | Y        | 룸 개수       |
| roomIds        | List&lt;Long&gt;            | Y        | 룸 ID 목록    |
| createdAt      | LocalDateTime               | N        | 생성일시       |
| updatedAt      | LocalDateTime               | N        | 수정일시       |

### PlaceContactResponse

연락처 정보 응답 객체

| 필드          | 타입                 | Nullable | 설명           |
|-------------|--------------------|----------|--------------|
| contact     | String             | Y        | 전화번호         |
| email       | String             | Y        | 이메일          |
| websites    | List&lt;String&gt; | Y        | 웹사이트 URL 목록  |
| socialLinks | List&lt;String&gt; | Y        | 소셜 미디어 링크 목록 |

### PlaceLocationResponse

위치 정보 응답 객체

| 필드            | 타입              | Nullable | 설명    |
|---------------|-----------------|----------|-------|
| address       | AddressResponse | Y        | 주소 정보 |
| latitude      | Double          | Y        | 위도    |
| longitude     | Double          | Y        | 경도    |
| locationGuide | String          | Y        | 위치 안내 |

### AddressResponse

주소 정보 응답 객체

| 필드            | 타입     | Nullable | 설명                    |
|---------------|--------|----------|-----------------------|
| province      | String | Y        | 시/도                   |
| city          | String | Y        | 시/군/구                 |
| district      | String | Y        | 동/읍/면                 |
| fullAddress   | String | Y        | 전체 주소                 |
| addressDetail | String | Y        | 상세 주소                 |
| postalCode    | String | Y        | 우편번호                  |
| shortAddress  | String | Y        | 짧은 주소 (시/군/구 + 동/읍/면) |

### PlaceParkingResponse

주차 정보 응답 객체

| 필드          | 타입          | Nullable | 설명                 |
|-------------|-------------|----------|--------------------|
| available   | Boolean     | Y        | 주차 가능 여부           |
| parkingType | ParkingType | Y        | 주차 타입 (FREE, PAID) |
| description | String      | Y        | 주차 안내              |

### PlaceSearchResponse

검색 응답 객체

| 필드         | 타입                          | Nullable | 설명            |
|------------|-----------------------------|----------|---------------|
| items      | List&lt;PlaceSearchItem&gt; | N        | 검색 결과 목록      |
| nextCursor | String                      | Y        | 다음 페이지 커서     |
| hasNext    | Boolean                     | N        | 다음 페이지 존재 여부  |
| count      | Integer                     | N        | 현재 페이지 항목 수   |
| totalCount | Long                        | Y        | 전체 항목 수 (선택적) |
| metadata   | SearchMetadata              | Y        | 검색 메타데이터      |

### PlaceSearchItem

검색 결과 항목

| 필드               | 타입                 | Nullable | 설명       |
|------------------|--------------------|----------|----------|
| id               | String             | N        | 공간 ID    |
| placeName        | String             | N        | 장소명      |
| description      | String             | Y        | 설명       |
| category         | String             | Y        | 카테고리     |
| placeType        | String             | Y        | 장소 타입    |
| fullAddress      | String             | Y        | 전체 주소    |
| latitude         | Double             | Y        | 위도       |
| longitude        | Double             | Y        | 경도       |
| distance         | Double             | Y        | 거리 (미터)  |
| ratingAverage    | Double             | Y        | 평균 평점    |
| reviewCount      | Integer            | Y        | 리뷰 개수    |
| parkingAvailable | Boolean            | Y        | 주차 가능 여부 |
| parkingType      | String             | Y        | 주차 타입    |
| thumbnailUrl     | String             | Y        | 썸네일 URL  |
| keywords         | List&lt;String&gt; | Y        | 키워드 목록   |
| contact          | String             | Y        | 연락처      |
| isActive         | Boolean            | N        | 활성화 여부   |
| approvalStatus   | String             | N        | 승인 상태    |
| roomCount        | Integer            | Y        | 룸 개수     |
| roomIds          | List&lt;Long&gt;   | Y        | 룸 ID 목록  |

### SearchMetadata

검색 메타데이터

| 필드             | 타입      | Nullable | 설명             |
|----------------|---------|----------|----------------|
| searchTime     | Long    | Y        | 검색 소요 시간 (밀리초) |
| sortBy         | String  | Y        | 정렬 기준          |
| sortDirection  | String  | Y        | 정렬 방향          |
| centerLat      | Double  | Y        | 중심 좌표 위도       |
| centerLng      | Double  | Y        | 중심 좌표 경도       |
| radiusInMeters | Integer | Y        | 검색 반경 (미터)     |
| appliedFilters | String  | Y        | 적용된 필터 목록      |

### KeywordResponse

키워드 응답 객체

| 필드           | 타입          | Nullable | 설명     |
|--------------|-------------|----------|--------|
| id           | Long        | N        | 키워드 ID |
| name         | String      | N        | 키워드 이름 |
| type         | KeywordType | N        | 키워드 타입 |
| description  | String      | Y        | 설명     |
| displayOrder | Integer     | Y        | 표시 순서  |

---

## 변경 이력

| 버전    | 날짜         | 변경 내용        |
|-------|------------|--------------|
| 1.0.0 | 2024-01-15 | 초기 API 명세 작성 |
