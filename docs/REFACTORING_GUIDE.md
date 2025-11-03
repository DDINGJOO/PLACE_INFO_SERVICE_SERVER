# PlaceInfo Server 리팩토링 가이드

## 목차

1. [현재 상태 분석](#현재-상태-분석)
2. [개선안 개요](#개선안-개요)
3. [상세 리팩토링 방안](#상세-리팩토링-방안)
4. [우선순위 및 로드맵](#우선순위-및-로드맵)

---

## 현재 상태 분석

### 프로젝트 규모

- **소스 파일**: 73개
- **테스트 파일**: 19개
- **테스트 커버리지**: 약 26%
- **주요 도메인**: Place (업체 정보 관리)

### 적용된 패턴

- DDD 부분 적용 (Aggregate Root, Value Objects)
- CQRS 부분 적용 (Command/Query Service 분리)
- Domain Events (events 패키지)
- Mapper Pattern

### 감지된 주요 문제

#### 1. Anemic Domain Model

- `PlaceInfo` 엔티티에 `@Setter` 노출
- `PlaceMapper`가 Entity 내부 상태를 직접 조작
- 비즈니스 규칙이 Service와 Entity에 분산

**문제 코드 위치:**

```
PlaceInfoServer/src/main/java/com/teambind/placeinfoserver/place/domain/entity/PlaceInfo.java:32
PlaceInfoServer/src/main/java/com/teambind/placeinfoserver/place/service/mapper/PlaceMapper.java:292-325
```

#### 2. SOLID 원칙 위반

- **SRP 위반**: `PlaceRegisterService`가 너무 많은 책임 (등록/수정/삭제/활성화/승인)
- **OCP 위반**: `AddressParser`의 switch 표현식 (새로운 주소 소스 추가 시 수정 필요)
- **ISP 위반**: Service 인터페이스 미사용

**문제 코드 위치:**

```
PlaceInfoServer/src/main/java/com/teambind/placeinfoserver/place/service/command/PlaceRegisterService.java:18
PlaceInfoServer/src/main/java/com/teambind/placeinfoserver/place/common/util/AddressParser.java:30
```

#### 3. Factory Pattern 미사용

- Entity 생성 로직이 `PlaceMapper`에 집중
- 생성 로직의 재사용 불가
- 테스트 픽스처와 프로덕션 코드의 생성 로직 중복

**문제 코드 위치:**

```
PlaceInfoServer/src/main/java/com/teambind/placeinfoserver/place/service/mapper/PlaceMapper.java:181-209
```

#### 4. Hexagonal Architecture 미적용

- Infrastructure 계층이 Domain에 침투
- JPA 의존성이 Domain Entity에 직접 노출
- 외부 시스템 연동 시 확장 어려움

#### 5. Exception 처리 미흡

- `CustomException`이 너무 단순
- Domain Exception vs Application Exception 구분 없음
- Controller에 하드코딩된 에러 메시지

**문제 코드 위치:**

```
PlaceInfoServer/src/main/java/com/teambind/placeinfoserver/place/common/exception/CustomException.java:5
PlaceInfoServer/src/main/java/com/teambind/placeinfoserver/place/controller/PlaceRegisterController.java:69
```

---

## 개선안 개요

| 개선 영역                  | 우선순위 | 예상 기간 | 영향도 |
|------------------------|------|-------|-----|
| Factory Pattern 도입     | P1   | 1주    | 중   |
| Domain Model 개선        | P1   | 2주    | 높음  |
| Exception 계층 구조화       | P1   | 1주    | 중   |
| Service Layer 분리       | P2   | 2주    | 높음  |
| Strategy Pattern 적용    | P2   | 1주    | 낮음  |
| Hexagonal Architecture | P3   | 1개월   | 높음  |
| 테스트 커버리지 개선            | P3   | 지속적   | 높음  |

---

## 상세 리팩토링 방안

### 1. Domain Model 개선 - Anemic Domain Model 해결

#### 목표

- Rich Domain Model 구축
- 캡슐화 강화 및 불변성 보장
- 비즈니스 로직을 Domain Entity에 집중

#### 현재 문제점

**Before:**

```java

@Setter  // 모든 필드에 Setter 노출
public class PlaceInfo extends BaseEntity {
	private String placeName;
	private String description;
	// ...
}

// Service에서 직접 조작
public void updateEntity(PlaceInfo entity, PlaceUpdateRequest request) {
	if (request.getPlaceName() != null) {
		entity.setPlaceName(request.getPlaceName());  // 직접 Setter 호출
	}
}
```

#### 개선 방안 A: Rich Domain Model (추천)

**장점:**

- 도메인 로직이 한 곳에 집중
- 캡슐화 강화, 불변성 보장
- SOLID 원칙 준수 (특히 SRP, OCP)
- 테스트 용이성 향상

**단점:**

- 초기 리팩토링 비용 높음
- Entity가 복잡해질 수 있음

**After:**

```java
// @Setter 제거, 비즈니스 메서드로 대체
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceInfo extends BaseEntity {
	
	private String placeName;
	private String description;
	private String category;
	private String placeType;
	
	/**
	 * 기본 정보 업데이트
	 * 비즈니스 규칙: 업체명은 필수, 100자 이하
	 */
	public void updateBasicInfo(String placeName, String description,
	                            String category, String placeType) {
		validatePlaceName(placeName);
		this.placeName = placeName;
		this.description = description;
		this.category = category;
		this.placeType = placeType;
	}
	
	/**
	 * 연락처 정보 업데이트
	 * 비즈니스 규칙: Contact가 없으면 새로 생성, 있으면 업데이트
	 */
	public void updateContact(String contact, String email,
	                          List<Url> websites, List<Url> socialLinks) {
		if (this.contact == null) {
			this.contact = PlaceContact.create(contact, email, websites, socialLinks, this);
		} else {
			this.contact.update(contact, email, websites, socialLinks);
		}
	}
	
	/**
	 * 위치 정보 업데이트
	 * 비즈니스 규칙: 좌표와 주소는 함께 변경되어야 함
	 */
	public void updateLocation(Address address, Double latitude, Double longitude,
	                           String locationGuide) {
		if (this.location == null) {
			this.location = PlaceLocation.create(address, latitude, longitude,
					locationGuide, this);
		} else {
			this.location.update(address, latitude, longitude, locationGuide);
		}
	}
	
	/**
	 * 업체명 검증
	 */
	private void validatePlaceName(String name) {
		if (name == null || name.isBlank()) {
			throw new InvalidPlaceNameException("업체명은 필수입니다.");
		}
		if (name.length() > 100) {
			throw new InvalidPlaceNameException("업체명은 100자를 초과할 수 없습니다.");
		}
	}
}

// PlaceContact도 동일하게 개선
public class PlaceContact extends BaseEntity {
	
	public static PlaceContact create(String contact, String email,
	                                  List<Url> websites, List<Url> socialLinks,
	                                  PlaceInfo placeInfo) {
		validateContact(contact);
		validateEmail(email);
		
		return PlaceContact.builder()
				.contact(contact)
				.email(email)
				.websites(websites)
				.socialLinks(socialLinks)
				.placeInfo(placeInfo)
				.build();
	}
	
	public void update(String contact, String email,
	                   List<Url> websites, List<Url> socialLinks) {
		validateContact(contact);
		validateEmail(email);
		
		this.contact = contact;
		this.email = email;
		this.websites = websites;
		this.socialLinks = socialLinks;
	}
	
	private static void validateContact(String contact) {
		if (contact == null || contact.isBlank()) {
			throw new InvalidContactException("연락처는 필수입니다.");
		}
	}
	
	private static void validateEmail(String email) {
		if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
			throw new InvalidEmailException("이메일 형식이 올바르지 않습니다.");
		}
	}
}
```

**Service는 단순해짐:**

```java

@Service
@RequiredArgsConstructor
public class UpdatePlaceUseCase {
	
	private final PlaceInfoRepository repository;
	
	@Transactional
	public PlaceInfoResponse execute(String placeId, PlaceUpdateRequest request) {
		PlaceInfo placeInfo = repository.findById(placeId)
				.orElseThrow(() -> new PlaceNotFoundException(placeId));
		
		// 도메인 메서드로 위임 - Service는 조율만 담당
		placeInfo.updateBasicInfo(
				request.getPlaceName(),
				request.getDescription(),
				request.getCategory(),
				request.getPlaceType()
		);
		
		if (request.getContact() != null) {
			placeInfo.updateContact(
					request.getContact().getContact(),
					request.getContact().getEmail(),
					request.getContact().getWebsites(),
					request.getContact().getSocialLinks()
			);
		}
		
		return PlaceInfoResponse.from(placeInfo);
	}
}
```

#### 개선 방안 B: Domain Service 활용

**적용 시나리오:**

- 여러 Aggregate를 조율하는 복잡한 로직
- Entity에 두기엔 너무 복잡한 비즈니스 규칙

**예시:**

```java

@Component
public class PlaceUpdateDomainService {
	
	/**
	 * 업체 정보 업데이트 with 복잡한 비즈니스 규칙
	 * - 승인된 업체는 카테고리 변경 불가
	 * - 활성화된 업체는 필수 정보 모두 있어야 함
	 */
	public void updatePlaceWithValidation(PlaceInfo placeInfo,
	                                      PlaceUpdateRequest request) {
		// 승인 상태 확인
		if (placeInfo.isApproved() && request.getCategory() != null) {
			throw new CannotChangeCategoryException("승인된 업체는 카테고리를 변경할 수 없습니다.");
		}
		
		// 업체 정보 업데이트
		placeInfo.updateBasicInfo(
				request.getPlaceName(),
				request.getDescription(),
				request.getCategory(),
				request.getPlaceType()
		);
		
		// 활성화 상태라면 완전성 검증
		if (placeInfo.isActive() && !placeInfo.isComplete()) {
			throw new IncompletePlaceInfoException("활성화된 업체는 모든 필수 정보가 있어야 합니다.");
		}
	}
}
```

**권장 사항:** 방안 A를 기본으로 적용하고, 복잡한 로직은 방안 B로 보완

---

### 2. Factory Pattern 도입

#### 목표

- Entity 생성 로직의 재사용성 확보
- 생성 의도 명확화
- 테스트 용이성 향상

#### 현재 문제점

**Before:**

```java
// PlaceMapper에 생성 로직이 집중
public PlaceInfo toEntity(PlaceRegisterRequest request, String generatedId) {
	PlaceInfo placeInfo = PlaceInfo.builder()
			.id(generatedId)
			.userId(request.getPlaceOwnerId())
			.placeName(request.getPlaceName())
			// ... 복잡한 생성 로직
			.build();
	
	if (request.getContact() != null) {
		placeInfo.setContact(toContactEntity(request.getContact(), placeInfo));
	}
	// ...
	
	return placeInfo;
}
```

#### 개선 방안 A: Static Factory Method

**적용 시나리오:**

- 단순한 생성 로직
- 외부 의존성이 없는 경우

**After:**

```java
public class PlaceInfo extends BaseEntity {
	
	/**
	 * 기본 정보만으로 업체 생성
	 */
	public static PlaceInfo create(String id, String userId, String placeName,
	                               String description, String category, String placeType) {
		validatePlaceName(placeName);
		validateUserId(userId);
		
		return PlaceInfo.builder()
				.id(id)
				.userId(userId)
				.placeName(placeName)
				.description(description)
				.category(category)
				.placeType(placeType)
				.isActive(true)
				.approvalStatus(ApprovalStatus.PENDING)
				.reviewCount(0)
				.build();
	}
	
	/**
	 * 전체 정보를 포함한 업체 생성
	 */
	public static PlaceInfo createWithFullInfo(String id, String userId,
	                                           String placeName, String description,
	                                           String category, String placeType,
	                                           PlaceContact contact,
	                                           PlaceLocation location,
	                                           PlaceParking parking) {
		PlaceInfo placeInfo = create(id, userId, placeName, description, category, placeType);
		
		if (contact != null) {
			placeInfo.setContact(contact);
		}
		if (location != null) {
			placeInfo.setLocation(location);
		}
		if (parking != null) {
			placeInfo.setParking(parking);
		}
		
		return placeInfo;
	}
	
	private static void validateUserId(String userId) {
		if (userId == null || userId.isBlank()) {
			throw new InvalidUserIdException("사용자 ID는 필수입니다.");
		}
	}
}
```

#### 개선 방안 B: Separate Factory Class (추천)

**적용 시나리오:**

- 복잡한 생성 로직
- 외부 의존성 필요 (AddressParser, PrimaryKeyGenerator 등)

**After:**

```java

@Component
@RequiredArgsConstructor
public class PlaceInfoFactory {
	
	private final PrimaryKeyGenerator keyGenerator;
	private final PlaceContactFactory contactFactory;
	private final PlaceLocationFactory locationFactory;
	private final PlaceParkingFactory parkingFactory;
	
	/**
	 * 등록 요청으로부터 PlaceInfo 생성
	 */
	public PlaceInfo create(PlaceRegisterRequest request) {
		String id = keyGenerator.generateKey();
		
		PlaceInfo placeInfo = PlaceInfo.create(
				id,
				request.getPlaceOwnerId(),
				request.getPlaceName(),
				request.getDescription(),
				request.getCategory(),
				request.getPlaceType()
		);
		
		// 연관 엔티티 생성 및 설정
		if (request.getContact() != null) {
			PlaceContact contact = contactFactory.create(request.getContact(), placeInfo);
			placeInfo.setContact(contact);
		}
		
		if (request.getLocation() != null) {
			PlaceLocation location = locationFactory.create(request.getLocation(), placeInfo);
			placeInfo.setLocation(location);
		}
		
		if (request.getParking() != null) {
			PlaceParking parking = parkingFactory.create(request.getParking(), placeInfo);
			placeInfo.setParking(parking);
		}
		
		return placeInfo;
	}
	
	/**
	 * 테스트용 PlaceInfo 생성
	 */
	public PlaceInfo createForTest(String placeName, String userId) {
		String id = keyGenerator.generateKey();
		return PlaceInfo.create(id, userId, placeName, null, null, null);
	}
}

@Component
@RequiredArgsConstructor
public class PlaceLocationFactory {
	
	private final AddressParser addressParser;
	
	public PlaceLocation create(PlaceLocationRequest request, PlaceInfo placeInfo) {
		// AddressParser를 사용하여 주소 파싱
		AddressRequest addressRequest = addressParser.parse(
				request.getFrom(),
				request.getAddressData()
		);
		
		Address address = Address.builder()
				.province(addressRequest.getProvince())
				.city(addressRequest.getCity())
				.district(addressRequest.getDistrict())
				.fullAddress(addressRequest.getFullAddress())
				.addressDetail(addressRequest.getAddressDetail())
				.postalCode(addressRequest.getPostalCode())
				.build();
		
		return PlaceLocation.create(
				address,
				request.getLatitude(),
				request.getLongitude(),
				request.getLocationGuide(),
				placeInfo
		);
	}
}
```

**Service 사용:**

```java

@Service
@RequiredArgsConstructor
public class RegisterPlaceUseCase {
	
	private final PlaceInfoRepository repository;
	private final PlaceInfoFactory factory;
	
	@Transactional
	public PlaceInfoResponse execute(PlaceRegisterRequest request) {
		// Factory에 생성 위임
		PlaceInfo placeInfo = factory.create(request);
		
		// 저장
		PlaceInfo saved = repository.save(placeInfo);
		
		return PlaceInfoResponse.from(saved);
	}
}
```

**테스트 용이성:**

```java

@ExtendWith(MockitoExtension.class)
class RegisterPlaceUseCaseTest {
	
	@Mock
	private PlaceInfoRepository repository;
	
	@Mock
	private PlaceInfoFactory factory;
	
	@InjectMocks
	private RegisterPlaceUseCase useCase;
	
	@Test
	@DisplayName("유효한 요청으로 업체 등록 시 성공한다")
	void registerPlace_WithValidRequest_Success() {
		// Given
		PlaceRegisterRequest request = PlaceRequestFactory.createValid();
		PlaceInfo mockPlace = PlaceTestFactory.create("place-1");
		
		given(factory.create(request)).willReturn(mockPlace);
		given(repository.save(any())).willReturn(mockPlace);
		
		// When
		PlaceInfoResponse response = useCase.execute(request);
		
		// Then
		assertThat(response.getId()).isEqualTo("place-1");
		verify(factory).create(request);
		verify(repository).save(mockPlace);
	}
}
```

**권장 사항:** 방안 B (Separate Factory Class) - 외부 의존성과 복잡한 로직 고려

---

### 3. Service Layer SOLID 원칙 준수

#### 목표

- SRP (Single Responsibility Principle) 준수
- ISP (Interface Segregation Principle) 준수
- Use Case별 명확한 책임 분리

#### 현재 문제점

**Before:**

```java

@Service
public class PlaceRegisterService {
	// 너무 많은 책임
	public PlaceInfoResponse registerPlace(...)      // 등록
	
	public PlaceInfoResponse updatePlace(...)        // 수정
	
	public void deletePlace(...)                     // 삭제
	
	public String activatePlace(...)                 // 활성화
	
	public String deactivatePlace(...)               // 비활성화
	
	public String approvePlace(...)                  // 승인
	
	public String rejectPlace(...)                   // 거부
}
```

#### 개선 방안 A: Use Case별 Service 분리 (추천)

**장점:**

- 각 Use Case가 명확히 분리
- SRP, ISP 준수
- 테스트 용이성
- Hexagonal Architecture로 발전 가능
- MSA 환경에서 서비스 분리 시 유리

**단점:**

- Service 클래스 수 증가
- 초기 구조 설계 필요

**After - 패키지 구조:**

```
service/
├── usecase/
│   ├── command/
│   │   ├── RegisterPlaceUseCase.java
│   │   ├── UpdatePlaceUseCase.java
│   │   ├── DeletePlaceUseCase.java
│   │   ├── ActivatePlaceUseCase.java
│   │   ├── DeactivatePlaceUseCase.java
│   │   ├── ApprovePlaceUseCase.java
│   │   └── RejectPlaceUseCase.java
│   └── query/
│       ├── GetPlaceDetailUseCase.java
│       ├── SearchPlacesUseCase.java
│       └── GetPlacesByUserUseCase.java
└── mapper/
    └── PlaceMapper.java (Response DTO 변환만 담당)
```

**Command Use Cases:**

```java
/**
 * 업체 등록 Use Case
 * 단일 책임: 새로운 업체를 시스템에 등록
 */
@Service
@RequiredArgsConstructor
public class RegisterPlaceUseCase {
	
	private final PlaceInfoRepository repository;
	private final PlaceInfoFactory factory;
	private final PlaceRegisteredEventPublisher eventPublisher;
	
	@Transactional
	public PlaceInfoResponse execute(PlaceRegisterRequest request) {
		// 1. 업체 생성
		PlaceInfo placeInfo = factory.create(request);
		
		// 2. 저장
		PlaceInfo saved = repository.save(placeInfo);
		
		// 3. 이벤트 발행
		eventPublisher.publish(new PlaceRegisteredEvent(saved.getId(), saved.getUserId()));
		
		// 4. 응답 변환
		return PlaceInfoResponse.from(saved);
	}
}

/**
 * 업체 정보 수정 Use Case
 * 단일 책임: 기존 업체의 기본 정보를 수정
 */
@Service
@RequiredArgsConstructor
public class UpdatePlaceUseCase {
	
	private final PlaceInfoRepository repository;
	private final PlaceUpdateDomainService domainService;
	
	@Transactional
	public PlaceInfoResponse execute(String placeId, PlaceUpdateRequest request) {
		// 1. 업체 조회
		PlaceInfo placeInfo = repository.findById(placeId)
				.orElseThrow(() -> new PlaceNotFoundException(placeId));
		
		// 2. 도메인 로직 실행
		domainService.updatePlaceWithValidation(placeInfo, request);
		
		// 3. 응답 변환 (더티 체킹으로 자동 저장)
		return PlaceInfoResponse.from(placeInfo);
	}
}

/**
 * 업체 승인 Use Case
 * 단일 책임: 관리자가 업체를 승인 처리
 */
@Service
@RequiredArgsConstructor
public class ApprovePlaceUseCase {
	
	private final PlaceInfoRepository repository;
	private final PlaceApprovedEventPublisher eventPublisher;
	
	@Transactional
	public void execute(String placeId) {
		// 1. 업체 조회
		PlaceInfo placeInfo = repository.findById(placeId)
				.orElseThrow(() -> new PlaceNotFoundException(placeId));
		
		// 2. 승인 가능 여부 확인
		if (!placeInfo.isComplete()) {
			throw new CannotApprovePlaceException("필수 정보가 모두 입력되지 않았습니다.");
		}
		
		if (placeInfo.isApproved()) {
			throw new AlreadyApprovedException("이미 승인된 업체입니다.");
		}
		
		// 3. 승인 처리
		placeInfo.approve();
		
		// 4. 이벤트 발행
		eventPublisher.publish(new PlaceApprovedEvent(placeInfo.getId()));
	}
}

/**
 * 업체 삭제 Use Case
 * 단일 책임: 업체를 소프트 삭제 처리
 */
@Service
@RequiredArgsConstructor
public class DeletePlaceUseCase {
	
	private final PlaceInfoRepository repository;
	private final PlaceDeletedEventPublisher eventPublisher;
	
	@Transactional
	public void execute(String placeId, String deletedBy) {
		// 1. 업체 조회
		PlaceInfo placeInfo = repository.findById(placeId)
				.orElseThrow(() -> new PlaceNotFoundException(placeId));
		
		// 2. 삭제 가능 여부 확인
		if (placeInfo.isDeleted()) {
			throw new AlreadyDeletedException("이미 삭제된 업체입니다.");
		}
		
		// 3. 소프트 삭제
		placeInfo.softDelete(deletedBy);
		
		// 4. 이벤트 발행 (연관 서비스에 삭제 알림)
		eventPublisher.publish(new PlaceDeletedEvent(placeId, deletedBy));
	}
}
```

**Query Use Cases:**

```java
/**
 * 업체 상세 조회 Use Case
 * 단일 책임: 업체의 전체 상세 정보를 조회
 */
@Service
@RequiredArgsConstructor
public class GetPlaceDetailUseCase {
	
	private final PlaceInfoRepository repository;
	
	@Transactional(readOnly = true)
	public PlaceInfoResponse execute(String placeId) {
		PlaceInfo placeInfo = repository.findById(placeId)
				.orElseThrow(() -> new PlaceNotFoundException(placeId));
		
		return PlaceInfoResponse.from(placeInfo);
	}
}

/**
 * 업체 검색 Use Case
 * 단일 책임: 조건에 맞는 업체 목록을 검색
 */
@Service
@RequiredArgsConstructor
public class SearchPlacesUseCase {
	
	private final PlaceAdvancedSearchRepository searchRepository;
	
	@Transactional(readOnly = true)
	public PlaceSearchResponse execute(PlaceSearchRequest request) {
		List<PlaceInfo> places = searchRepository.search(request);
		
		List<PlaceInfoSummaryResponse> summaries = places.stream()
				.map(PlaceInfoSummaryResponse::from)
				.toList();
		
		return PlaceSearchResponse.of(summaries, places.size());
	}
}
```

**Controller 개선:**

```java

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceCommandController {
	
	private final RegisterPlaceUseCase registerUseCase;
	private final UpdatePlaceUseCase updateUseCase;
	private final DeletePlaceUseCase deleteUseCase;
	private final ActivatePlaceUseCase activateUseCase;
	private final DeactivatePlaceUseCase deactivateUseCase;
	
	@PostMapping
	public ResponseEntity<PlaceInfoResponse> register(@Valid @RequestBody PlaceRegisterRequest request) {
		PlaceInfoResponse response = registerUseCase.execute(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@PutMapping("/{placeId}")
	public ResponseEntity<PlaceInfoResponse> update(
			@PathVariable String placeId,
			@Valid @RequestBody PlaceUpdateRequest request) {
		PlaceInfoResponse response = updateUseCase.execute(placeId, request);
		return ResponseEntity.ok(response);
	}
	
	@PatchMapping("/{placeId}/activate")
	public ResponseEntity<Void> activate(@PathVariable String placeId) {
		activateUseCase.execute(placeId);
		return ResponseEntity.noContent().build();
	}
	
	@PatchMapping("/{placeId}/deactivate")
	public ResponseEntity<Void> deactivate(@PathVariable String placeId) {
		deactivateUseCase.execute(placeId);
		return ResponseEntity.noContent().build();
	}
	
	@DeleteMapping("/{placeId}")
	public ResponseEntity<Void> delete(
			@PathVariable String placeId,
			@RequestParam String deletedBy) {
		deleteUseCase.execute(placeId, deletedBy);
		return ResponseEntity.noContent().build();
	}
}

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/places")
public class PlaceAdminController {
	
	private final ApprovePlaceUseCase approveUseCase;
	private final RejectPlaceUseCase rejectUseCase;
	
	@PatchMapping("/{placeId}/approve")
	public ResponseEntity<Void> approve(@PathVariable String placeId) {
		approveUseCase.execute(placeId);
		return ResponseEntity.noContent().build();
	}
	
	@PatchMapping("/{placeId}/reject")
	public ResponseEntity<Void> reject(@PathVariable String placeId) {
		rejectUseCase.execute(placeId);
		return ResponseEntity.noContent().build();
	}
}

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceQueryController {
	
	private final GetPlaceDetailUseCase getDetailUseCase;
	private final SearchPlacesUseCase searchUseCase;
	private final GetPlacesByUserUseCase getByUserUseCase;
	
	@GetMapping("/{placeId}")
	public ResponseEntity<PlaceInfoResponse> getDetail(@PathVariable String placeId) {
		PlaceInfoResponse response = getDetailUseCase.execute(placeId);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/search")
	public ResponseEntity<PlaceSearchResponse> search(@Valid PlaceSearchRequest request) {
		PlaceSearchResponse response = searchUseCase.execute(request);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/users/{userId}")
	public ResponseEntity<List<PlaceInfoSummaryResponse>> getByUser(@PathVariable String userId) {
		List<PlaceInfoSummaryResponse> response = getByUserUseCase.execute(userId);
		return ResponseEntity.ok(response);
	}
}
```

#### 개선 방안 B: Facade Pattern 유지 + 내부 분리

**적용 시나리오:**

- 외부 인터페이스 변경 최소화가 중요한 경우
- 점진적 리팩토링을 원하는 경우

**After:**

```java

@Service
@RequiredArgsConstructor
public class PlaceService {
	
	// 내부적으로 Use Case들을 사용
	private final RegisterPlaceUseCase registerUseCase;
	private final UpdatePlaceUseCase updateUseCase;
	private final DeletePlaceUseCase deleteUseCase;
	
	public PlaceInfoResponse registerPlace(PlaceRegisterRequest request) {
		return registerUseCase.execute(request);
	}
	
	public PlaceInfoResponse updatePlace(String placeId, PlaceUpdateRequest request) {
		return updateUseCase.execute(placeId, request);
	}
	
	// ... Facade 메서드들
}
```

**권장 사항:** 방안 A (Use Case별 분리) - MSA 환경과 Clean Architecture에 적합

---

### 4. Strategy Pattern 적용 - AddressParser 개선

#### 목표

- OCP (Open-Closed Principle) 준수
- 새로운 주소 소스 추가 시 기존 코드 수정 불필요
- 각 전략의 독립적 테스트 가능

#### 현재 문제점

**Before:**

```java
public class AddressParser {
	
	public AddressRequest parse(AddressSource from, Object addressData) {
		// Switch 표현식 - 새로운 소스 추가 시 이 코드를 수정해야 함 (OCP 위반)
		return switch (from) {
			case KAKAO -> parseKakaoAddress(addressData);
			case NAVER -> parseNaverAddress(addressData);
			case MANUAL -> parseManualAddress(addressData);
		};
	}
}
```

#### 개선 방안: Strategy Pattern (추천)

**장점:**

- OCP 준수 (새로운 전략 추가 시 기존 코드 수정 불필요)
- SRP 준수 (각 전략이 하나의 파싱 로직만 담당)
- 각 전략의 테스트 독립적
- 전략 추가/제거가 용이

**After - 패키지 구조:**

```
common/util/address/
├── AddressParser.java (Context)
├── strategy/
│   ├── AddressParsingStrategy.java (Interface)
│   ├── KakaoAddressParsingStrategy.java
│   ├── NaverAddressParsingStrategy.java
│   └── ManualAddressParsingStrategy.java
└── exception/
    └── UnsupportedAddressSourceException.java
```

**Strategy Interface:**

```java
/**
 * 주소 파싱 전략 인터페이스
 */
public interface AddressParsingStrategy {
	
	/**
	 * 이 전략이 지원하는 주소 소스
	 */
	AddressSource supports();
	
	/**
	 * 주소 데이터를 파싱하여 AddressRequest로 변환
	 *
	 * @param addressData 외부 API 응답 원본 데이터
	 * @return 파싱된 AddressRequest
	 * @throws AddressParsingException 파싱 실패 시
	 */
	AddressRequest parse(Object addressData);
}
```

**Concrete Strategies:**

```java
/**
 * 카카오맵 주소 파싱 전략
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoAddressParsingStrategy implements AddressParsingStrategy {
	
	private final ObjectMapper objectMapper;
	
	@Override
	public AddressSource supports() {
		return AddressSource.KAKAO;
	}
	
	@Override
	public AddressRequest parse(Object addressData) {
		try {
			KakaoAddressRequest kakaoAddress = objectMapper.convertValue(
					addressData,
					KakaoAddressRequest.class
			);
			
			return kakaoAddress.toAddressRequest();
		
		} catch (IllegalArgumentException e) {
			log.error("카카오 주소 파싱 실패: {}", e.getMessage(), e);
			throw new AddressParsingException(
					"카카오 주소 데이터 파싱에 실패했습니다: " + e.getMessage(),
					e
			);
		}
	}
}

/**
 * 네이버맵 주소 파싱 전략
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NaverAddressParsingStrategy implements AddressParsingStrategy {
	
	private final ObjectMapper objectMapper;
	
	@Override
	public AddressSource supports() {
		return AddressSource.NAVER;
	}
	
	@Override
	public AddressRequest parse(Object addressData) {
		try {
			NaverAddressRequest naverAddress = objectMapper.convertValue(
					addressData,
					NaverAddressRequest.class
			);
			
			return naverAddress.toAddressRequest();
		
		} catch (IllegalArgumentException e) {
			log.error("네이버 주소 파싱 실패: {}", e.getMessage(), e);
			throw new AddressParsingException(
					"네이버 주소 데이터 파싱에 실패했습니다: " + e.getMessage(),
					e
			);
		}
	}
}

/**
 * 수동 입력 주소 파싱 전략
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ManualAddressParsingStrategy implements AddressParsingStrategy {
	
	private final ObjectMapper objectMapper;
	
	@Override
	public AddressSource supports() {
		return AddressSource.MANUAL;
	}
	
	@Override
	public AddressRequest parse(Object addressData) {
		try {
			// 프론트에서 이미 파싱된 데이터를 받음
			return objectMapper.convertValue(addressData, AddressRequest.class);
		
		} catch (IllegalArgumentException e) {
			log.error("수동 입력 주소 파싱 실패: {}", e.getMessage(), e);
			throw new AddressParsingException(
					"수동 입력 주소 데이터 파싱에 실패했습니다: " + e.getMessage(),
					e
			);
		}
	}
}
```

**Context (AddressParser):**

```java
/**
 * 주소 데이터 파싱 유틸리티 (Context)
 * Strategy Pattern을 사용하여 다양한 주소 소스 지원
 */
@Component
@Slf4j
public class AddressParser {
	
	private final Map<AddressSource, AddressParsingStrategy> strategies;
	
	/**
	 * 생성자 주입으로 모든 전략을 받아 Map으로 구성
	 * 새로운 전략이 추가되면 자동으로 등록됨
	 */
	public AddressParser(List<AddressParsingStrategy> strategyList) {
		this.strategies = strategyList.stream()
				.collect(Collectors.toMap(
						AddressParsingStrategy::supports,
						Function.identity()
				));
		
		log.info("AddressParser 초기화 완료. 지원 소스: {}", strategies.keySet());
	}
	
	/**
	 * 주소 데이터 출처에 따라 적절한 전략으로 파싱
	 *
	 * @param from 주소 데이터 출처
	 * @param addressData 외부 API 응답 원본 데이터
	 * @return 파싱된 AddressRequest
	 * @throws IllegalArgumentException addressData가 null인 경우
	 * @throws UnsupportedAddressSourceException 지원하지 않는 주소 소스인 경우
	 */
	public AddressRequest parse(AddressSource from, Object addressData) {
		if (addressData == null) {
			throw new IllegalArgumentException("주소 데이터가 null입니다.");
		}
		
		AddressParsingStrategy strategy = strategies.get(from);
		if (strategy == null) {
			throw new UnsupportedAddressSourceException(
					"지원하지 않는 주소 소스입니다: " + from
			);
		}
		
		log.debug("주소 파싱 시작 - 소스: {}", from);
		return strategy.parse(addressData);
	}
}
```

**Exception:**

```java
/**
 * 지원하지 않는 주소 소스 예외
 */
public class UnsupportedAddressSourceException extends RuntimeException {
	
	private final AddressSource addressSource;
	
	public UnsupportedAddressSourceException(AddressSource addressSource) {
		super("지원하지 않는 주소 소스입니다: " + addressSource);
		this.addressSource = addressSource;
	}
	
	public AddressSource getAddressSource() {
		return addressSource;
	}
}

/**
 * 주소 파싱 실패 예외
 */
public class AddressParsingException extends RuntimeException {
	
	public AddressParsingException(String message) {
		super(message);
	}
	
	public AddressParsingException(String message, Throwable cause) {
		super(message, cause);
	}
}
```

**테스트:**

```java

@ExtendWith(MockitoExtension.class)
class KakaoAddressParsingStrategyTest {
	
	@Mock
	private ObjectMapper objectMapper;
	
	@InjectMocks
	private KakaoAddressParsingStrategy strategy;
	
	@Test
	@DisplayName("카카오 주소 데이터를 성공적으로 파싱한다")
	void parse_WithValidKakaoData_Success() {
		// Given
		Map<String, Object> kakaoData = Map.of(
				"address_name", "서울 강남구 역삼동",
				"road_address_name", "서울 강남구 테헤란로 123"
		);
		
		KakaoAddressRequest kakaoRequest = KakaoAddressRequest.builder()
				.addressName("서울 강남구 역삼동")
				.roadAddressName("서울 강남구 테헤란로 123")
				.build();
		
		given(objectMapper.convertValue(kakaoData, KakaoAddressRequest.class))
				.willReturn(kakaoRequest);
		
		// When
		AddressRequest result = strategy.parse(kakaoData);
		
		// Then
		assertThat(result).isNotNull();
		assertThat(result.getFullAddress()).contains("서울");
	}
	
	@Test
	@DisplayName("잘못된 데이터 형식이면 예외가 발생한다")
	void parse_WithInvalidData_ThrowsException() {
		// Given
		Object invalidData = "invalid";
		given(objectMapper.convertValue(invalidData, KakaoAddressRequest.class))
				.willThrow(new IllegalArgumentException("Invalid data"));
		
		// When & Then
		assertThatThrownBy(() -> strategy.parse(invalidData))
				.isInstanceOf(AddressParsingException.class)
				.hasMessageContaining("카카오 주소 데이터 파싱에 실패했습니다");
	}
}
```

**새로운 전략 추가 (예: Google Maps):**

```java
// 1. AddressSource Enum에 새로운 값 추가
public enum AddressSource {
	KAKAO, NAVER, MANUAL, GOOGLE  // 추가
}

// 2. 새로운 전략 구현 - 기존 코드 수정 불필요!
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleAddressParsingStrategy implements AddressParsingStrategy {
	
	private final ObjectMapper objectMapper;
	
	@Override
	public AddressSource supports() {
		return AddressSource.GOOGLE;
	}
	
	@Override
	public AddressRequest parse(Object addressData) {
		// Google Maps API 응답 파싱 로직
		GoogleAddressRequest googleAddress = objectMapper.convertValue(
				addressData,
				GoogleAddressRequest.class
		);
		return googleAddress.toAddressRequest();
	}
}

// AddressParser나 다른 코드는 수정할 필요 없음!
```

**권장 사항:** Strategy Pattern 적용 - OCP 준수 및 확장성 확보

---

### 5. Exception 계층 구조화

#### 목표

- Domain Exception vs Application Exception 명확히 구분
- 예외 처리 전략 일관성 확보
- 적절한 HTTP 상태 코드 매핑

#### 현재 문제점

**Before:**

```java
// 너무 단순한 Exception
public class CustomException extends RuntimeException {
	private final ErrorCode errorcode;
	
	public CustomException(ErrorCode errorcode) {
		super(errorcode.toString());
		this.errorcode = errorcode;
	}
}

// Controller에 하드코딩된 에러 처리
public ResponseEntity<Void> delete(@PathVariable String placeId) {
	commandService.deletePlace(placeId, "OWNER");  // "OWNER" 하드코딩
	return ResponseEntity.noContent().build();
}
```

#### 개선 방안: Exception 계층 구조화 (추천)

**장점:**

- 예외 유형별 명확한 처리 가능
- HTTP 상태 코드 자동 매핑
- Domain 예외와 Application 예외 구분
- GlobalExceptionHandler에서 일관된 처리

**After - 패키지 구조:**

```
common/exception/
├── PlaceException.java (Base)
├── domain/
│   ├── PlaceNotFoundException.java
│   ├── InvalidPlaceStateException.java
│   ├── CannotApprovePlaceException.java
│   ├── InvalidPlaceNameException.java
│   └── ...
├── application/
│   ├── InvalidRequestException.java
│   ├── UnauthorizedException.java
│   └── ...
├── ErrorCode.java
├── ErrorResponse.java
└── GlobalExceptionHandler.java
```

**Base Exception:**

```java
/**
 * Place 도메인의 기본 예외 클래스
 */
public abstract class PlaceException extends RuntimeException {
	
	private final ErrorCode errorCode;
	
	protected PlaceException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
	
	protected PlaceException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}
	
	public ErrorCode getErrorCode() {
		return errorCode;
	}
	
	/**
	 * HTTP 상태 코드 반환 (하위 클래스에서 구현)
	 */
	public abstract HttpStatus getHttpStatus();
}
```

**Domain Exceptions (비즈니스 규칙 위반):**

```java
/**
 * 업체를 찾을 수 없음
 */
public class PlaceNotFoundException extends PlaceException {
	
	private final String placeId;
	
	public PlaceNotFoundException(String placeId) {
		super(ErrorCode.PLACE_NOT_FOUND);
		this.placeId = placeId;
	}
	
	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.NOT_FOUND;
	}
	
	@Override
	public String getMessage() {
		return String.format("업체를 찾을 수 없습니다. (ID: %s)", placeId);
	}
}

/**
 * 잘못된 업체 상태
 */
public class InvalidPlaceStateException extends PlaceException {
	
	public InvalidPlaceStateException(String message) {
		super(ErrorCode.INVALID_PLACE_STATE);
	}
	
	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.CONFLICT;
	}
}

/**
 * 업체를 승인할 수 없음
 */
public class CannotApprovePlaceException extends PlaceException {
	
	public CannotApprovePlaceException(String reason) {
		super(ErrorCode.CANNOT_APPROVE_PLACE);
	}
	
	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}

/**
 * 이미 승인된 업체
 */
public class AlreadyApprovedException extends PlaceException {
	
	public AlreadyApprovedException(String message) {
		super(ErrorCode.ALREADY_APPROVED);
	}
	
	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.CONFLICT;
	}
}

/**
 * 잘못된 업체명
 */
public class InvalidPlaceNameException extends PlaceException {
	
	public InvalidPlaceNameException(String message) {
		super(ErrorCode.INVALID_PLACE_NAME);
	}
	
	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
```

**Application Exceptions (입력 검증, 권한 등):**

```java
/**
 * 잘못된 요청
 */
public class InvalidRequestException extends PlaceException {
	
	public InvalidRequestException(String message) {
		super(ErrorCode.INVALID_REQUEST);
	}
	
	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}

/**
 * 권한 없음
 */
public class UnauthorizedException extends PlaceException {
	
	public UnauthorizedException(String message) {
		super(ErrorCode.UNAUTHORIZED);
	}
	
	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.UNAUTHORIZED;
	}
}

/**
 * 접근 금지
 */
public class ForbiddenException extends PlaceException {
	
	public ForbiddenException(String message) {
		super(ErrorCode.FORBIDDEN);
	}
	
	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.FORBIDDEN;
	}
}
```

**ErrorCode 개선:**

```java

@Getter
@AllArgsConstructor
public enum ErrorCode {
	
	// Domain Errors (4xxx)
	PLACE_NOT_FOUND("PLACE_4001", "업체를 찾을 수 없습니다"),
	INVALID_PLACE_STATE("PLACE_4002", "잘못된 업체 상태입니다"),
	CANNOT_APPROVE_PLACE("PLACE_4003", "업체를 승인할 수 없습니다"),
	ALREADY_APPROVED("PLACE_4004", "이미 승인된 업체입니다"),
	ALREADY_DELETED("PLACE_4005", "이미 삭제된 업체입니다"),
	INVALID_PLACE_NAME("PLACE_4006", "잘못된 업체명입니다"),
	INCOMPLETE_PLACE_INFO("PLACE_4007", "필수 정보가 모두 입력되지 않았습니다"),
	
	// Application Errors (5xxx)
	INVALID_REQUEST("APP_5001", "잘못된 요청입니다"),
	UNAUTHORIZED("APP_5002", "인증이 필요합니다"),
	FORBIDDEN("APP_5003", "접근 권한이 없습니다"),
	
	// Internal Errors (9xxx)
	INTERNAL_SERVER_ERROR("SYS_9001", "서버 내부 오류가 발생했습니다");
	
	private final String code;
	private final String message;
}
```

**ErrorResponse 개선:**

```java

@Getter
@Builder
public class ErrorResponse {
	
	private final String code;
	private final String message;
	private final LocalDateTime timestamp;
	private final String path;
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final Map<String, String> fieldErrors;  // Validation 에러용
	
	public static ErrorResponse of(ErrorCode errorCode, String path) {
		return ErrorResponse.builder()
				.code(errorCode.getCode())
				.message(errorCode.getMessage())
				.timestamp(LocalDateTime.now())
				.path(path)
				.build();
	}
	
	public static ErrorResponse of(ErrorCode errorCode, String path,
	                               Map<String, String> fieldErrors) {
		return ErrorResponse.builder()
				.code(errorCode.getCode())
				.message(errorCode.getMessage())
				.timestamp(LocalDateTime.now())
				.path(path)
				.fieldErrors(fieldErrors)
				.build();
	}
}
```

**GlobalExceptionHandler 개선:**

```java

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	
	/**
	 * PlaceException 계층의 모든 예외 처리
	 */
	@ExceptionHandler(PlaceException.class)
	public ResponseEntity<ErrorResponse> handlePlaceException(
			PlaceException e, HttpServletRequest request) {
		
		log.warn("PlaceException 발생: {} - {}", e.getClass().getSimpleName(), e.getMessage());
		
		ErrorResponse errorResponse = ErrorResponse.of(
				e.getErrorCode(),
				request.getRequestURI()
		);
		
		return ResponseEntity
				.status(e.getHttpStatus())
				.body(errorResponse);
	}
	
	/**
	 * Validation 예외 처리
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
			MethodArgumentNotValidException e, HttpServletRequest request) {
		
		Map<String, String> fieldErrors = new HashMap<>();
		e.getBindingResult().getFieldErrors().forEach(error ->
				fieldErrors.put(error.getField(), error.getDefaultMessage())
		);
		
		ErrorResponse errorResponse = ErrorResponse.of(
				ErrorCode.INVALID_REQUEST,
				request.getRequestURI(),
				fieldErrors
		);
		
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(errorResponse);
	}
	
	/**
	 * 예상치 못한 예외 처리
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(
			Exception e, HttpServletRequest request) {
		
		log.error("예상치 못한 예외 발생", e);
		
		ErrorResponse errorResponse = ErrorResponse.of(
				ErrorCode.INTERNAL_SERVER_ERROR,
				request.getRequestURI()
		);
		
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
	}
}
```

**사용 예시:**

```java

@Service
@RequiredArgsConstructor
public class ApprovePlaceUseCase {
	
	@Transactional
	public void execute(String placeId) {
		PlaceInfo placeInfo = repository.findById(placeId)
				.orElseThrow(() -> new PlaceNotFoundException(placeId));  // 명확한 예외
		
		if (!placeInfo.isComplete()) {
			throw new CannotApprovePlaceException("필수 정보가 모두 입력되지 않았습니다.");
		}
		
		if (placeInfo.isApproved()) {
			throw new AlreadyApprovedException("이미 승인된 업체입니다.");
		}
		
		placeInfo.approve();
	}
}

// Entity에서도 사용
public class PlaceInfo extends BaseEntity {
	
	public void updatePlaceName(String newName) {
		if (newName == null || newName.isBlank()) {
			throw new InvalidPlaceNameException("업체명은 필수입니다.");
		}
		if (newName.length() > 100) {
			throw new InvalidPlaceNameException("업체명은 100자를 초과할 수 없습니다.");
		}
		this.placeName = newName;
	}
}
```

**권장 사항:** Exception 계층 구조화 적용 - 명확한 예외 처리 전략 확립

---

### 6. Hexagonal Architecture 적용 (장기 목표)

#### 목표

- Domain과 Infrastructure 완전 분리
- 테스트 용이성 극대화
- 외부 시스템 교체 용이
- DDD와의 완벽한 조화

#### 현재 문제점

- JPA 애노테이션이 Domain Entity에 직접 노출
- Repository가 JpaRepository에 의존
- 외부 API 연동이 Service Layer에 직접 결합

#### 개선 방안 A: Full Hexagonal Architecture (최종 목표)

**패키지 구조:**

```
src/main/java/com/teambind/placeinfoserver/place/
├── domain/
│   ├── model/              # 순수 도메인 모델 (JPA 애노테이션 없음)
│   │   ├── PlaceInfo.java
│   │   ├── PlaceContact.java
│   │   ├── PlaceLocation.java
│   │   └── ...
│   ├── vo/                 # Value Objects
│   │   ├── Address.java
│   │   ├── Coordinates.java
│   │   └── ...
│   ├── service/            # Domain Services
│   │   └── PlaceUpdateDomainService.java
│   ├── factory/            # Domain Factories
│   │   └── PlaceInfoFactory.java
│   └── exception/          # Domain Exceptions
│       └── PlaceNotFoundException.java
├── application/
│   ├── port/
│   │   ├── in/             # Input Ports (Use Case 인터페이스)
│   │   │   ├── RegisterPlaceUseCase.java
│   │   │   ├── UpdatePlaceUseCase.java
│   │   │   └── ...
│   │   └── out/            # Output Ports (Repository 인터페이스)
│   │       ├── PlaceRepository.java
│   │       ├── AddressGeocoder.java
│   │       └── EventPublisher.java
│   └── service/            # Use Case 구현
│       ├── RegisterPlaceService.java
│       ├── UpdatePlaceService.java
│       └── ...
└── adapter/
    ├── in/
    │   └── web/            # Web Adapter (Controller)
    │       ├── PlaceCommandController.java
    │       ├── PlaceQueryController.java
    │       └── dto/
    │           ├── request/
    │           └── response/
    └── out/
        ├── persistence/    # Persistence Adapter
        │   ├── PlaceJpaEntity.java
        │   ├── PlaceJpaRepository.java
        │   ├── PlaceRepositoryAdapter.java
        │   └── mapper/
        │       └── PlaceJpaMapper.java
        └── external/       # External Service Adapter
            ├── KakaoAddressAdapter.java
            └── NaverAddressAdapter.java
```

**Domain Model (순수 도메인):**

```java
// JPA 애노테이션 없음!
public class PlaceInfo {
	
	private String id;
	private String userId;
	private String placeName;
	private PlaceContact contact;
	private PlaceLocation location;
	// ...
	
	// 순수 비즈니스 로직만
	public void updateBasicInfo(String placeName, String description) {
		validatePlaceName(placeName);
		this.placeName = placeName;
		this.description = description;
	}
}
```

**Output Port (Repository 인터페이스):**

```java
/**
 * 업체 저장소 포트 (Output Port)
 * 도메인 모델용 인터페이스 (JPA 없음)
 */
public interface PlaceRepository {
	
	PlaceInfo save(PlaceInfo placeInfo);
	
	Optional<PlaceInfo> findById(String id);
	
	List<PlaceInfo> findByUserId(String userId);
	
	void delete(PlaceInfo placeInfo);
	
	boolean existsById(String id);
}
```

**Persistence Adapter:**

```java
// JPA Entity (Infrastructure)
@Entity
@Table(name = "place_info")
@Getter
@Setter
class PlaceJpaEntity {
	
	@Id
	private String id;
	
	@Column(name = "user_id")
	private String userId;
	
	@Column(name = "place_name")
	private String placeName;
	// ... JPA 매핑
}

// JPA Repository
interface PlaceJpaRepository extends JpaRepository<PlaceJpaEntity, String> {
	List<PlaceJpaEntity> findByUserId(String userId);
}

// Adapter 구현
@Repository
@RequiredArgsConstructor
class PlaceRepositoryAdapter implements PlaceRepository {
	
	private final PlaceJpaRepository jpaRepository;
	private final PlaceJpaMapper mapper;
	
	@Override
	public PlaceInfo save(PlaceInfo placeInfo) {
		PlaceJpaEntity entity = mapper.toJpaEntity(placeInfo);
		PlaceJpaEntity saved = jpaRepository.save(entity);
		return mapper.toDomain(saved);
	}
	
	@Override
	public Optional<PlaceInfo> findById(String id) {
		return jpaRepository.findById(id)
				.map(mapper::toDomain);
	}
	
	@Override
	public List<PlaceInfo> findByUserId(String userId) {
		return jpaRepository.findByUserId(userId).stream()
				.map(mapper::toDomain)
				.toList();
	}
}
```

**Input Port (Use Case 인터페이스):**

```java
/**
 * 업체 등록 Use Case (Input Port)
 */
public interface RegisterPlaceUseCase {
	
	/**
	 * 업체 등록 실행
	 */
	PlaceInfoResponse execute(RegisterPlaceCommand command);
}

/**
 * 업체 등록 Command
 */
@Getter
@Builder
public class RegisterPlaceCommand {
	private final String userId;
	private final String placeName;
	private final String description;
	private final ContactInfo contact;
	private final LocationInfo location;
	// ...
}
```

**Use Case 구현 (Application Service):**

```java

@Service
@RequiredArgsConstructor
class RegisterPlaceService implements RegisterPlaceUseCase {
	
	private final PlaceRepository placeRepository;  // Port
	private final PlaceInfoFactory factory;
	private final EventPublisher eventPublisher;    // Port
	
	@Override
	@Transactional
	public PlaceInfoResponse execute(RegisterPlaceCommand command) {
		// 도메인 모델 생성
		PlaceInfo placeInfo = factory.create(command);
		
		// 저장 (Port를 통해)
		PlaceInfo saved = placeRepository.save(placeInfo);
		
		// 이벤트 발행 (Port를 통해)
		eventPublisher.publish(new PlaceRegisteredEvent(saved.getId()));
		
		return PlaceInfoResponse.from(saved);
	}
}
```

**Web Adapter (Controller):**

```java

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
class PlaceCommandController {
	
	private final RegisterPlaceUseCase registerUseCase;  // Input Port
	
	@PostMapping
	public ResponseEntity<PlaceInfoResponse> register(
			@Valid @RequestBody PlaceRegisterRequest request) {
		
		// Request DTO -> Command 변환
		RegisterPlaceCommand command = RegisterPlaceCommand.builder()
				.userId(request.getPlaceOwnerId())
				.placeName(request.getPlaceName())
				.description(request.getDescription())
				// ...
				.build();
		
		PlaceInfoResponse response = registerUseCase.execute(command);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
```

**장점:**

- 완전한 계층 분리
- Domain이 Framework에 독립적
- 테스트 극도로 용이 (Mock 포트만 있으면 됨)
- 외부 시스템 교체 시 Adapter만 변경

**단점:**

- 코드량 대폭 증가
- 매퍼 로직 복잡도 증가
- 팀 학습 곡선 높음

#### 개선 방안 B: Layered Architecture 개선 (점진적 접근, 추천)

**현재 구조를 유지하면서 점진적으로 개선:**

1. **Repository Interface 분리 (1단계)**

```java
// domain/repository/PlaceRepository.java (인터페이스)
public interface PlaceRepository {
	PlaceInfo save(PlaceInfo placeInfo);
	
	Optional<PlaceInfo> findById(String id);
	// ...
}

// infrastructure/persistence/PlaceJpaRepository.java (구현)
@Repository
public interface PlaceJpaRepository extends JpaRepository<PlaceInfo, String>, PlaceRepository {
	// JPA 전용 메서드는 여기에
}
```

2. **Service Interface 추가 (2단계)**

```java
public interface RegisterPlaceUseCase {
	PlaceInfoResponse execute(PlaceRegisterRequest request);
}

@Service
@RequiredArgsConstructor
public class RegisterPlaceService implements RegisterPlaceUseCase {
	// 구현
}
```

3. **External Service Port 분리 (3단계)**

```java
// domain/port/AddressGeocoder.java
public interface AddressGeocoder {
	Coordinates geocode(Address address);
}

// infrastructure/external/KakaoAddressGeocoder.java
@Component
public class KakaoAddressGeocoder implements AddressGeocoder {
	// 카카오 API 연동
}
```

**권장 사항:** 방안 B로 시작하여 점진적으로 방안 A로 발전

---

### 7. 테스트 전략 개선

#### 목표

- 테스트 커버리지 70% 이상
- Test Pyramid 적용
- Given-When-Then 구조 일관성
- 독립적이고 반복 가능한 테스트

#### 현재 문제점

- 테스트 커버리지 약 26% (19/73)
- 통합 테스트와 단위 테스트 구분 불명확
- 테스트 픽스처 관리 미흡

#### 개선 방안: Test Pyramid 적용 (추천)

**테스트 계층 구조:**

```
E2E Tests (10%)
  └── Full API Flow Tests (TestContainers)

Integration Tests (20%)
  ├── Repository Tests (@DataJpaTest)
  ├── Service Integration Tests (@SpringBootTest)
  └── API Tests (@WebMvcTest or @SpringBootTest + MockMvc)

Unit Tests (70%)
  ├── Domain Model Tests
  ├── Value Object Tests
  ├── Factory Tests
  ├── Use Case Tests (with Mocks)
  └── Utility Tests
```

**1. Unit Tests (70%)**

**Domain Model Tests:**

```java

@DisplayName("PlaceInfo 도메인 모델 테스트")
class PlaceInfoTest {
	
	@Test
	@DisplayName("업체 생성 시 기본 상태가 올바르게 설정된다")
	void create_WithValidInfo_SetsDefaultState() {
		// Given
		String id = "place-1";
		String userId = "user-1";
		String placeName = "테스트 업체";
		
		// When
		PlaceInfo placeInfo = PlaceInfo.create(id, userId, placeName, null, null, null);
		
		// Then
		assertThat(placeInfo).isNotNull();
		assertThat(placeInfo.getId()).isEqualTo(id);
		assertThat(placeInfo.getIsActive()).isTrue();
		assertThat(placeInfo.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
		assertThat(placeInfo.getReviewCount()).isEqualTo(0);
	}
	
	@Test
	@DisplayName("업체명이 비어있으면 생성 시 예외가 발생한다")
	void create_WithEmptyName_ThrowsException() {
		// Given
		String emptyName = "";
		
		// When & Then
		assertThatThrownBy(() ->
				PlaceInfo.create("id", "userId", emptyName, null, null, null)
		)
				.isInstanceOf(InvalidPlaceNameException.class)
				.hasMessageContaining("업체명은 필수입니다");
	}
	
	@Test
	@DisplayName("업체명이 100자를 초과하면 예외가 발생한다")
	void create_WithTooLongName_ThrowsException() {
		// Given
		String tooLongName = "a".repeat(101);
		
		// When & Then
		assertThatThrownBy(() ->
				PlaceInfo.create("id", "userId", tooLongName, null, null, null)
		)
				.isInstanceOf(InvalidPlaceNameException.class)
				.hasMessageContaining("100자를 초과할 수 없습니다");
	}
	
	@Test
	@DisplayName("승인된 업체는 다시 승인할 수 없다")
	void approve_WhenAlreadyApproved_ThrowsException() {
		// Given
		PlaceInfo placeInfo = PlaceTestFactory.createApproved();
		
		// When & Then
		assertThatThrownBy(() -> placeInfo.approve())
				.isInstanceOf(AlreadyApprovedException.class);
	}
	
	@Nested
	@DisplayName("연락처 정보 업데이트")
	class UpdateContactTest {
		
		@Test
		@DisplayName("연락처가 없으면 새로 생성한다")
		void updateContact_WhenContactIsNull_CreatesNewContact() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createWithoutContact();
			List<Url> websites = List.of(Url.of("https://example.com"));
			
			// When
			placeInfo.updateContact("010-1234-5678", "test@example.com", websites, List.of());
			
			// Then
			assertThat(placeInfo.getContact()).isNotNull();
			assertThat(placeInfo.getContact().getContact()).isEqualTo("010-1234-5678");
		}
		
		@Test
		@DisplayName("연락처가 있으면 기존 연락처를 업데이트한다")
		void updateContact_WhenContactExists_UpdatesExistingContact() {
			// Given
			PlaceInfo placeInfo = PlaceTestFactory.createWithContact();
			String newContact = "010-9999-9999";
			
			// When
			placeInfo.updateContact(newContact, "new@example.com", List.of(), List.of());
			
			// Then
			assertThat(placeInfo.getContact().getContact()).isEqualTo(newContact);
		}
	}
}
```

**Value Object Tests:**

```java

@DisplayName("Address Value Object 테스트")
class AddressTest {
	
	@Test
	@DisplayName("동일한 주소 정보는 equals로 true를 반환한다")
	void equals_WithSameAddress_ReturnsTrue() {
		// Given
		Address address1 = Address.builder()
				.province("서울특별시")
				.city("강남구")
				.district("역삼동")
				.fullAddress("서울특별시 강남구 역삼동 123")
				.build();
		
		Address address2 = Address.builder()
				.province("서울특별시")
				.city("강남구")
				.district("역삼동")
				.fullAddress("서울특별시 강남구 역삼동 123")
				.build();
		
		// When & Then
		assertThat(address1).isEqualTo(address2);
	}
	
	@Test
	@DisplayName("짧은 주소는 도/시/구/군/동 형식으로 반환된다")
	void getShortAddress_ReturnsFormattedAddress() {
		// Given
		Address address = Address.builder()
				.province("서울특별시")
				.city("강남구")
				.district("역삼동")
				.fullAddress("서울특별시 강남구 역삼동 123")
				.build();
		
		// When
		String shortAddress = address.getShortAddress();
		
		// Then
		assertThat(shortAddress).isEqualTo("서울특별시 강남구 역삼동");
	}
}
```

**Use Case Tests (with Mocks):**

```java

@ExtendWith(MockitoExtension.class)
@DisplayName("업체 등록 Use Case 테스트")
class RegisterPlaceUseCaseTest {
	
	@Mock
	private PlaceRepository repository;
	
	@Mock
	private PlaceInfoFactory factory;
	
	@Mock
	private PlaceRegisteredEventPublisher eventPublisher;
	
	@InjectMocks
	private RegisterPlaceService useCase;
	
	@Test
	@DisplayName("유효한 요청으로 업체 등록 시 성공한다")
	void execute_WithValidRequest_Success() {
		// Given
		PlaceRegisterRequest request = PlaceRequestFactory.createValid();
		PlaceInfo mockPlace = PlaceTestFactory.create("place-1");
		
		given(factory.create(request)).willReturn(mockPlace);
		given(repository.save(any(PlaceInfo.class))).willReturn(mockPlace);
		
		// When
		PlaceInfoResponse response = useCase.execute(request);
		
		// Then
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo("place-1");
		
		verify(factory).create(request);
		verify(repository).save(mockPlace);
		verify(eventPublisher).publish(any(PlaceRegisteredEvent.class));
	}
	
	@Test
	@DisplayName("Factory에서 예외 발생 시 이벤트가 발행되지 않는다")
	void execute_WhenFactoryThrowsException_DoesNotPublishEvent() {
		// Given
		PlaceRegisterRequest request = PlaceRequestFactory.createValid();
		given(factory.create(request))
				.willThrow(new InvalidPlaceNameException("잘못된 업체명"));
		
		// When & Then
		assertThatThrownBy(() -> useCase.execute(request))
				.isInstanceOf(InvalidPlaceNameException.class);
		
		verify(eventPublisher, never()).publish(any());
	}
}
```

**Test Fixtures (Factory):**

```java
/**
 * 테스트용 PlaceInfo 생성 Factory
 */
public class PlaceTestFactory {
	
	public static PlaceInfo create(String id) {
		return PlaceInfo.builder()
				.id(id)
				.userId("test-user")
				.placeName("테스트 업체")
				.description("테스트 설명")
				.category("연습실")
				.placeType("음악")
				.isActive(true)
				.approvalStatus(ApprovalStatus.PENDING)
				.reviewCount(0)
				.build();
	}
	
	public static PlaceInfo createApproved() {
		PlaceInfo place = create("place-approved");
		place.approve();
		return place;
	}
	
	public static PlaceInfo createWithContact() {
		PlaceInfo place = create("place-with-contact");
		PlaceContact contact = PlaceContact.create(
				"010-1234-5678",
				"test@example.com",
				List.of(),
				List.of(),
				place
		);
		place.setContact(contact);
		return place;
	}
	
	public static PlaceInfo createComplete() {
		PlaceInfo place = create("place-complete");
		place.setContact(PlaceContactTestFactory.create(place));
		place.setLocation(PlaceLocationTestFactory.create(place));
		return place;
	}
}

public class PlaceRequestFactory {
	
	public static PlaceRegisterRequest createValid() {
		return PlaceRegisterRequest.builder()
				.placeOwnerId("user-1")
				.placeName("테스트 업체")
				.description("테스트 설명")
				.category("연습실")
				.placeType("음악")
				.contact(createValidContact())
				.location(createValidLocation())
				.build();
	}
	
	public static PlaceRegisterRequest createWithEmptyName() {
		return PlaceRegisterRequest.builder()
				.placeOwnerId("user-1")
				.placeName("")
				.build();
	}
	
	private static PlaceContactRequest createValidContact() {
		return PlaceContactRequest.builder()
				.contact("010-1234-5678")
				.email("test@example.com")
				.build();
	}
	
	private static PlaceLocationRequest createValidLocation() {
		return PlaceLocationRequest.builder()
				.from(AddressSource.KAKAO)
				.addressData(createKakaoAddressData())
				.latitude(37.5665)
				.longitude(126.9780)
				.build();
	}
}
```

**2. Integration Tests (20%)**

**Repository Tests:**

```java

@DataJpaTest
@DisplayName("PlaceInfo Repository 통합 테스트")
class PlaceInfoRepositoryTest {
	
	@Autowired
	private PlaceInfoRepository repository;
	
	@Test
	@DisplayName("업체 저장 시 ID로 조회할 수 있다")
	void save_ThenFindById_ReturnsPlace() {
		// Given
		PlaceInfo placeInfo = PlaceTestFactory.create("test-id");
		
		// When
		repository.save(placeInfo);
		Optional<PlaceInfo> found = repository.findById("test-id");
		
		// Then
		assertThat(found).isPresent();
		assertThat(found.get().getPlaceName()).isEqualTo("테스트 업체");
	}
	
	@Test
	@DisplayName("소프트 삭제된 업체는 조회되지 않는다")
	void delete_WithSoftDelete_NotFoundByQuery() {
		// Given
		PlaceInfo placeInfo = PlaceTestFactory.create("test-id");
		repository.save(placeInfo);
		
		// When
		placeInfo.softDelete("admin");
		repository.save(placeInfo);
		
		// Then
		Optional<PlaceInfo> found = repository.findById("test-id");
		assertThat(found).isEmpty();
	}
}
```

**API Tests:**

```java

@WebMvcTest(PlaceCommandController.class)
@DisplayName("업체 등록 API 통합 테스트")
class PlaceRegisterApiTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private RegisterPlaceUseCase registerUseCase;
	
	@Test
	@DisplayName("유효한 요청으로 업체 등록 시 201 Created를 반환한다")
	void register_WithValidRequest_Returns201() throws Exception {
		// Given
		PlaceRegisterRequest request = PlaceRequestFactory.createValid();
		PlaceInfoResponse response = PlaceInfoResponse.builder()
				.id("place-1")
				.placeName(request.getPlaceName())
				.build();
		
		given(registerUseCase.execute(any())).willReturn(response);
		
		// When & Then
		mockMvc.perform(post("/api/v1/places")
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value("place-1"))
				.andExpect(jsonPath("$.placeName").value(request.getPlaceName()));
	}
	
	@Test
	@DisplayName("업체명이 비어있으면 400 Bad Request를 반환한다")
	void register_WithEmptyName_Returns400() throws Exception {
		// Given
		PlaceRegisterRequest request = PlaceRequestFactory.createWithEmptyName();
		
		// When & Then
		mockMvc.perform(post("/api/v1/places")
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("APP_5001"));
	}
}
```

**3. E2E Tests (10%)**

```java

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("업체 등록 E2E 테스트")
class PlaceRegisterE2ETest {
	
	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
			.withDatabaseName("testdb");
	
	@Autowired
	private TestRestTemplate restTemplate;
	
	@Test
	@DisplayName("업체 등록부터 조회까지 전체 흐름이 정상 동작한다")
	void fullFlow_RegisterAndGet_Success() {
		// Given - 업체 등록 요청
		PlaceRegisterRequest registerRequest = PlaceRequestFactory.createValid();
		
		// When - 업체 등록
		ResponseEntity<PlaceInfoResponse> registerResponse = restTemplate.postForEntity(
				"/api/v1/places",
				registerRequest,
				PlaceInfoResponse.class
		);
		
		// Then - 등록 성공
		assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		String placeId = registerResponse.getBody().getId();
		
		// When - 업체 조회
		ResponseEntity<PlaceInfoResponse> getResponse = restTemplate.getForEntity(
				"/api/v1/places/" + placeId,
				PlaceInfoResponse.class
		);
		
		// Then - 조회 성공
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(getResponse.getBody().getPlaceName()).isEqualTo(registerRequest.getPlaceName());
	}
}
```

**테스트 커버리지 목표:**
| 레이어 | 목표 커버리지 | 측정 기준 |
|--------|--------------|-----------|
| Domain Layer | 90%+ | Line Coverage |
| Service Layer | 80%+ | Line Coverage |
| Controller Layer | 70%+ | Line Coverage |
| 전체 | 75%+ | Line Coverage |

**권장 사항:**

- Unit Tests부터 작성하여 70% 커버리지 확보
- 점진적으로 Integration Tests 추가
- E2E Tests는 핵심 Use Case만 작성

---

## 우선순위 및 로드맵

### Phase 1: 기반 개선 (즉시 시작 - 2주)

**목표:** SOLID 원칙 준수 및 코드 품질 개선

**작업 항목:**

1. ✅ **Factory Pattern 도입** (3일)
	- PlaceInfoFactory 구현
	- PlaceContactFactory, PlaceLocationFactory 구현
	- Service에서 Factory 사용으로 변경

2. ✅ **Exception 계층 구조화** (2일)
	- PlaceException 기본 클래스 구현
	- Domain Exception 구현 (PlaceNotFoundException 등)
	- GlobalExceptionHandler 개선

3. ✅ **Domain Model @Setter 제거 시작** (5일)
	- PlaceInfo 비즈니스 메서드 추가
	- PlaceContact, PlaceLocation 개선
	- Mapper에서 Setter 사용 제거

**예상 결과:**

- 코드 가독성 30% 향상
- 테스트 작성 용이성 증가
- 버그 발생률 감소

---

### Phase 2: 아키텍처 개선 (1개월)

**목표:** Service Layer 구조 개선 및 확장성 확보

**작업 항목:**

1. ✅ **Use Case별 Service 분리** (2주)
	- Command Use Cases 구현 (Register, Update, Delete 등)
	- Query Use Cases 구현 (GetDetail, Search 등)
	- Controller를 Command/Query로 분리

2. ✅ **Strategy Pattern 적용** (3일)
	- AddressParsingStrategy 인터페이스 정의
	- Concrete Strategies 구현 (Kakao, Naver, Manual)
	- AddressParser Context 개선

3. ✅ **Use Case 단위 테스트 작성** (1주)
	- 각 Use Case별 단위 테스트
	- Test Fixtures 구현
	- Mocking 전략 수립

**예상 결과:**

- SRP, OCP 원칙 준수
- 테스트 커버리지 50% 달성
- 새로운 기능 추가 시간 단축

---

### Phase 3: 테스트 및 품질 개선 (2개월)

**목표:** 테스트 커버리지 70% 달성 및 안정성 확보

**작업 항목:**

1. ✅ **Domain Model 테스트** (1주)
	- Entity 비즈니스 로직 테스트
	- Value Object 테스트
	- Factory 테스트

2. ✅ **Integration Tests 작성** (2주)
	- Repository 통합 테스트
	- API 통합 테스트
	- TestContainers 설정

3. ✅ **Repository Interface 분리** (1주)
	- PlaceRepository 인터페이스 정의
	- JPA Repository와 분리
	- Service에서 인터페이스 사용

4. ✅ **E2E Tests 작성** (1주)
	- 핵심 Use Case E2E 테스트
	- TestContainers 기반 통합 테스트

**예상 결과:**

- 테스트 커버리지 70% 달성
- CI/CD 파이프라인 안정화
- 리그레션 버그 감소

---

### Phase 4: Hexagonal Architecture (장기 - 3개월)

**목표:** 완전한 계층 분리 및 MSA 준비

**작업 항목:**

1. ✅ **Port & Adapter 구조 설계** (1주)
	- Input Ports (Use Case 인터페이스) 정의
	- Output Ports (Repository 인터페이스) 정의
	- 패키지 구조 재설계

2. ✅ **Domain Model 순수화** (2주)
	- JPA 애노테이션 제거
	- PlaceJpaEntity 별도 구현
	- JpaMapper 구현

3. ✅ **Adapter 구현** (3주)
	- Persistence Adapter
	- Web Adapter
	- External Service Adapter

4. ✅ **점진적 마이그레이션** (4주)
	- 기존 코드를 새 구조로 이동
	- 테스트 재작성
	- 문서화

**예상 결과:**

- 완전한 계층 분리
- Framework 독립성 확보
- MSA 분리 준비 완료

---

## 리팩토링 체크리스트

### Phase 1 체크리스트

- [ ] PlaceInfoFactory 구현
- [ ] PlaceContactFactory 구현
- [ ] PlaceLocationFactory 구현
- [ ] PlaceException 기본 클래스 구현
- [ ] Domain Exception 6개 이상 구현
- [ ] GlobalExceptionHandler 개선
- [ ] PlaceInfo @Setter 제거 시작
- [ ] 비즈니스 메서드 5개 이상 추가
- [ ] Code Review 완료

### Phase 2 체크리스트

- [ ] RegisterPlaceUseCase 구현
- [ ] UpdatePlaceUseCase 구현
- [ ] DeletePlaceUseCase 구현
- [ ] ApprovePlaceUseCase 구현
- [ ] GetPlaceDetailUseCase 구현
- [ ] SearchPlacesUseCase 구현
- [ ] Controller 분리 (Command/Query)
- [ ] AddressParsingStrategy 인터페이스
- [ ] 3개 Strategy 구현 (Kakao, Naver, Manual)
- [ ] Use Case 단위 테스트 10개 이상
- [ ] Test Fixtures 구현
- [ ] Code Review 완료

### Phase 3 체크리스트

- [ ] Domain Model 테스트 20개 이상
- [ ] Value Object 테스트 10개 이상
- [ ] Repository 통합 테스트 10개 이상
- [ ] API 통합 테스트 15개 이상
- [ ] PlaceRepository 인터페이스 정의
- [ ] TestContainers 설정
- [ ] E2E 테스트 5개 이상
- [ ] 테스트 커버리지 70% 달성
- [ ] Code Review 완료

### Phase 4 체크리스트

- [ ] Input Ports 정의
- [ ] Output Ports 정의
- [ ] 패키지 구조 재설계
- [ ] PlaceJpaEntity 구현
- [ ] JpaMapper 구현
- [ ] Persistence Adapter 구현
- [ ] Web Adapter 구현
- [ ] External Service Adapter 구현
- [ ] 기존 코드 마이그레이션 100%
- [ ] 테스트 재작성 완료
- [ ] 문서화 완료
- [ ] Code Review 완료

---

## 참고 자료

### 서적

- "Domain-Driven Design" by Eric Evans
- "Implementing Domain-Driven Design" by Vaughn Vernon
- "Clean Architecture" by Robert C. Martin
- "Get Your Hands Dirty on Clean Architecture" by Tom Hombergs

### 아티클

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [SOLID Principles](https://www.baeldung.com/solid-principles)
- [Test Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html)

### 코드 예제

- [Spring Boot Hexagonal Architecture Example](https://github.com/thombergs/buckpal)

---

## 문의 및 피드백

리팩토링 진행 중 문의사항이나 개선 제안이 있으시면 팀 채널로 공유해주세요.

**마지막 업데이트:** 2025-11-03
**작성자:** Development Team
