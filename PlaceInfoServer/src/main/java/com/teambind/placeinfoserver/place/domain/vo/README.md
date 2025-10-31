# Value Objects (VO)

## 개요

Value Object는 **DDD (Domain-Driven Design)**의 핵심 빌딩 블록으로, 도메인의 개념을 명확하게 표현하는 불변 객체입니다.

## Value Object의 특징

### 1. 불변성 (Immutability)

- 생성 후 상태 변경 불가
- Thread-safe 보장
- 안전한 공유 가능

### 2. 값 기반 동등성 (Value Equality)

- ID가 아닌 값으로 비교
- `equals()`, `hashCode()` 오버라이드
- `@EqualsAndHashCode` 사용

### 3. 자체 검증 (Self-Validation)

- 생성 시점에 유효성 검증
- 항상 유효한 상태 보장
- 검증 로직의 중앙화

### 4. 비즈니스 로직 포함

- 도메인 개념을 명확하게 표현
- Primitive Obsession 제거
- 의미 있는 메서드 제공

### 5. Side-Effect Free

- 메서드 호출이 외부 상태를 변경하지 않음
- 순수 함수 (Pure Function)

## 사용 가능한 Value Objects

### 1. Coordinates (좌표)

**파일**: `Coordinates.java`

**용도**: 위도(latitude)와 경도(longitude)를 하나의 개념으로 관리

**생성**:

```java
// 정적 팩토리 메서드 사용 (검증 포함)
Coordinates coords = Coordinates.of(37.5665, 126.9780);

// 잘못된 좌표는 예외 발생
Coordinates invalid = Coordinates.of(91.0, 180.5);  // ❌ CustomException
```

**주요 기능**:

- 좌표 유효성 검증 (-90~90, -180~180)
- 두 좌표 간 거리 계산 (Haversine 공식)
- WGS84 좌표계 (SRID 4326)

**예제**:

```java
Coordinates seoul = Coordinates.of(37.5665, 126.9780);
Coordinates busan = Coordinates.of(35.1796, 129.0756);

// 거리 계산 (미터 단위)
double distance = seoul.distanceTo(busan);  // 약 325km
System.out.println(distance + "m");
```

### 2. Distance (거리)

**파일**: `Distance.java`

**용도**: 거리를 타입 안전하게 관리하고 다양한 단위로 변환

**생성**:

```java
// 다양한 단위로 생성 가능
Distance d1 = Distance.ofMeters(5000);
Distance d2 = Distance.ofKilometers(5.0);
Distance d3 = Distance.ofMiles(3.1);

// 상수 사용
Distance defaultRadius = Distance.DEFAULT_SEARCH_RADIUS;  // 5km
Distance maxRadius = Distance.MAX_SEARCH_RADIUS;          // 50km
```

**주요 기능**:

- 단위 변환 (미터, 킬로미터, 마일)
- 거리 연산 (더하기, 빼기)
- 비교 연산 (크다, 작다)
- 사람이 읽기 쉬운 형식

**예제**:

```java
Distance walk = Distance.ofMeters(1500);
Distance bus = Distance.ofKilometers(5);

// 단위 변환
System.out.println(walk.toKilometers());      // 1.5
System.out.println(bus.toMiles());            // 3.1

// 연산
Distance total = walk.plus(bus);              // 6500m
System.out.println(total.toHumanReadable());  // "6.5km"

// 비교
if (walk.isLessThan(bus)) {
    System.out.println("걸어갈 수 있는 거리");
}
```

### 3. Address (주소)

**파일**: `Address.java`

**용도**: 주소 정보를 하나의 개념으로 관리

**생성**:

```java
Address address = Address.builder()
    .province("서울특별시")
    .city("강남구")
    .district("역삼동")
    .fullAddress("서울특별시 강남구 역삼동 123")
    .addressDetail("테헤란로 타워 10층")
    .postalCode("06234")
    .build();
```

**주요 기능**:

- 짧은 주소 반환 (시/구/동)
- 상세 주소 포함 전체 주소
- 주소 유효성 확인

**예제**:

```java
String shortAddr = address.getShortAddress();
// "서울특별시 강남구 역삼동"

String fullAddr = address.getFullAddressWithDetail();
// "서울특별시 강남구 역삼동 123 테헤란로 타워 10층"

boolean valid = address.isValid();  // true
```

### 4. PhoneNumber (전화번호)

**파일**: `PhoneNumber.java`

**용도**: 전화번호 형식 검증 및 정규화

**생성**:

```java
// 다양한 형식 지원
PhoneNumber phone1 = PhoneNumber.of("02-1234-5678");
PhoneNumber phone2 = PhoneNumber.of("01012345678");
PhoneNumber phone3 = PhoneNumber.of("010 1234 5678");  // 자동 정규화

// 잘못된 형식
PhoneNumber invalid = PhoneNumber.of("123");  // ❌ CustomException
```

**주요 기능**:

- 자동 정규화 (하이픈 추가)
- 형식 검증 (정규식)
- 숫자만 추출
- 국제 형식 변환 (+82)

**지원 형식**:

- 일반 전화: 02-1234-5678, 031-123-4567
- 휴대폰: 010-1234-5678
- 특수번호: 1588-1234, 1577-1234

**예제**:

```java
PhoneNumber phone = PhoneNumber.of("01012345678");

System.out.println(phone.getValue());              // "010-1234-5678"
System.out.println(phone.getDigitsOnly());         // "01012345678"
System.out.println(phone.toInternationalFormat()); // "+821012345678"
```

### 5. Email (이메일)

**파일**: `Email.java`

**용도**: 이메일 주소 형식 검증 및 관리

**생성**:

```java
Email email = Email.of("user@example.com");

// 자동 정규화 (소문자 변환)
Email normalized = Email.of("User@Example.COM");  // "user@example.com"

// 잘못된 형식
Email invalid = Email.of("not-an-email");  // ❌ CustomException
```

**주요 기능**:

- RFC 5322 표준 준수
- 자동 정규화 (소문자, 공백 제거)
- 도메인/로컬파트 추출
- 마스킹 (개인정보 보호)

**예제**:

```java
Email email = Email.of("testuser@example.com");

System.out.println(email.getDomain());      // "example.com"
System.out.println(email.getLocalPart());   // "testuser"
System.out.println(email.getMasked());      // "te**@example.com"
```

### 6. Url (URL)

**파일**: `Url.java`

**용도**: URL 형식 검증 및 정규화

**생성**:

```java
// 프로토콜 자동 추가
Url url1 = Url.of("www.example.com");        // "https://www.example.com"
Url url2 = Url.of("https://www.example.com");

// 잘못된 URL
Url invalid = Url.of("not a url");  // ❌ CustomException
```

**주요 기능**:

- URL 파싱 및 검증
- 프로토콜 자동 추가 (https://)
- 도메인/경로/프로토콜 추출
- HTTPS 여부 확인

**예제**:

```java
Url url = Url.of("https://www.example.com/path/to/page");

System.out.println(url.getDomain());    // "www.example.com"
System.out.println(url.getProtocol());  // "https"
System.out.println(url.getPath());      // "/path/to/page"
System.out.println(url.isSecure());     // true
```

## Value Objects in Events

### ImagesChangeEventWrapper

**파일**: `events/event/ImagesChangeEventWrapper.java`

**특징**:

- 불변 이벤트 객체
- Jackson 직렬화/역직렬화 지원
- 방어적 복사 (Defensive Copy)
- 불변 리스트 반환

**예제**:

```java
ImagesChangeEventWrapper event = ImagesChangeEventWrapper.builder()
    .referenceId("place-123")
    .images(List.of(
        SequentialImageChangeEvent.builder()
            .imageId("img-1")
            .imageUrl("https://example.com/img1.jpg")
            .sequence(1)
            .build()
    ))
    .build();

// 불변성 보장
event.getImages().add(...);  // ❌ UnsupportedOperationException
```

## 사용 가이드

### 1. 정적 팩토리 메서드 패턴

```java
// ✅ 권장: 정적 팩토리 메서드 사용
Coordinates coords = Coordinates.of(37.5665, 126.9780);
PhoneNumber phone = PhoneNumber.of("010-1234-5678");

// ❌ 비권장: 생성자 직접 호출 (불가능하도록 설계됨)
// Coordinates coords = new Coordinates(37.5665, 126.9780);
```

### 2. 검증 로직의 중앙화

```java
// VO를 사용하면 검증 로직이 한 곳에 집중됨
public void updateLocation(Coordinates coords) {
    // 검증은 이미 Coordinates.of()에서 완료됨
    placeLocation.setCoordinates(coords);
}

// VO를 사용하지 않으면 검증 로직이 분산됨
public void updateLocation(Double lat, Double lon) {
    if (lat < -90 || lat > 90) throw ...;
    if (lon < -180 || lon > 180) throw ...;
    // 검증 로직이 여기저기 중복됨
}
```

### 3. Primitive Obsession 제거

```java
// ❌ Before: Primitive Obsession
public PlaceSearchResponse search(Double lat, Double lon, Integer radius) {
    // lat, lon, radius가 무엇을 의미하는지 불명확
    // 검증 로직이 여기저기 분산
}

// ✅ After: Value Object 사용
public PlaceSearchResponse search(Coordinates coords, Distance radius) {
    // 의미가 명확함
    // 검증은 VO에서 처리
    // 타입 안전성 확보
}
```

### 4. 비즈니스 로직 캡슐화

```java
// ✅ VO에 비즈니스 로직 포함
Coordinates seoul = Coordinates.of(37.5665, 126.9780);
Coordinates busan = Coordinates.of(35.1796, 129.0756);

double distance = seoul.distanceTo(busan);  // 비즈니스 로직이 VO 안에

// ❌ 비즈니스 로직이 외부에 분산
double distance = calculateDistance(
    seoul.getLatitude(), seoul.getLongitude(),
    busan.getLatitude(), busan.getLongitude()
);
```

## Entity에서 사용하기

### @Embeddable 활용

```java
@Entity
public class PlaceLocation {
    @Id
    private String id;

    // VO를 Embedded로 사용
    @Embedded
    private Address address;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "latitude", column = @Column(name = "lat")),
        @AttributeOverride(name = "longitude", column = @Column(name = "lon"))
    })
    private Coordinates coordinates;
}
```

### 일반 필드로 사용

```java
@Entity
public class PlaceContact {
    @Id
    private String id;

    // VO를 일반 필드로 사용
    @Embedded
    private PhoneNumber phoneNumber;

    @Embedded
    private Email email;

    @Embedded
    private Url websiteUrl;
}
```

## 모범 사례

### DO ✅

```java
// 1. 정적 팩토리 메서드 사용
Coordinates coords = Coordinates.of(37.5665, 126.9780);

// 2. 불변성 보장 (final 필드)
public class MyVO {
    private final String value;
}

// 3. 검증 로직 포함
public static Coordinates of(Double lat, Double lon) {
    validateLatitude(lat);
    validateLongitude(lon);
    return new Coordinates(lat, lon);
}

// 4. 의미 있는 메서드 제공
double distance = coords1.distanceTo(coords2);
boolean isNear = distance.isLessThan(Distance.ofKilometers(5));
```

### DON'T ❌

```java
// 1. VO를 가변 객체로 만들기
public class BadVO {
    private String value;
    public void setValue(String value) {  // ❌ setter
        this.value = value;
    }
}

// 2. 검증 없이 생성
public static Coordinates of(Double lat, Double lon) {
    return new Coordinates(lat, lon);  // ❌ 검증 없음
}

// 3. ID 기반 동등성
@Override
public boolean equals(Object o) {
    return this.id.equals(((MyVO) o).id);  // ❌ ID 비교
}

// 4. Null 반환
public Coordinates getCoordinates() {
    return null;  // ❌ Optional 사용 권장
}
```

## 테스트 작성

### 단위 테스트 예제

```java
@Test
void coordinates_생성_성공() {
    // given
    Double lat = 37.5665;
    Double lon = 126.9780;

    // when
    Coordinates coords = Coordinates.of(lat, lon);

    // then
    assertThat(coords.getLatitude()).isEqualTo(lat);
    assertThat(coords.getLongitude()).isEqualTo(lon);
    assertThat(coords.isValid()).isTrue();
}

@Test
void coordinates_잘못된_위도_예외() {
    // given
    Double invalidLat = 91.0;
    Double lon = 126.9780;

    // when & then
    assertThatThrownBy(() -> Coordinates.of(invalidLat, lon))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining("LOCATION_LATITUDE_OUT_OF_RANGE");
}

@Test
void distance_연산_성공() {
    // given
    Distance d1 = Distance.ofKilometers(3);
    Distance d2 = Distance.ofKilometers(2);

    // when
    Distance sum = d1.plus(d2);
    Distance diff = d1.minus(d2);

    // then
    assertThat(sum.toKilometers()).isEqualTo(5.0);
    assertThat(diff.toKilometers()).isEqualTo(1.0);
}
```

## Value Object vs Entity

| 특징   | Value Object          | Entity          |
|------|-----------------------|-----------------|
| 식별자  | 없음                    | 있음 (ID)         |
| 동등성  | 값 기반                  | ID 기반           |
| 불변성  | 불변                    | 가변              |
| 생명주기 | Entity에 종속            | 독립적             |
| 예제   | Coordinates, Distance | PlaceInfo, User |

## 향후 계획

### Phase 1 (완료)

- ✅ Coordinates VO
- ✅ Distance VO
- ✅ Address VO
- ✅ PhoneNumber VO
- ✅ Email VO
- ✅ Url VO
- ✅ Event VO (불변성 개선)

### Phase 2 (예정)

- [ ] Money VO (금액, 통화)
- [ ] DateRange VO (날짜 범위)
- [ ] BusinessHours VO (영업시간)
- [ ] Rating VO (평점)

### Phase 3 (장기)

- [ ] VO Converter 자동 생성
- [ ] Custom Validation Annotation
- [ ] VO Factory 패턴 도입

## 참고 자료

- [DDD Reference - Eric Evans](https://www.domainlanguage.com/ddd/reference/)
- [Implementing Value Objects - Martin Fowler](https://martinfowler.com/bliki/ValueObject.html)
- [Effective Java - Joshua Bloch](https://www.oreilly.com/library/view/effective-java/9780134686097/)
