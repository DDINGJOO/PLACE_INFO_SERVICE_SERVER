# PLACE_INFO_SERVICE_SERVER 프로덕션 인프라 구축 계획

## 개요

NOTIFICATION 프로젝트의 아키텍처 패턴을 참조하여 PLACE_INFO_SERVICE_SERVER에 프로덕션 레벨 인프라를 구축합니다.

**목표:**
1. `prod/` 디렉토리 구조 구축
2. PostgreSQL Master/Slave 복제 설정
3. 인프라 분리 (앱 vs 공유 인프라)
4. 배포 자동화

---

## 현재 상태 분석

### PLACE_INFO 현재 구조
```
PLACE_INFO_SERVICE_SERVER/
├── PlaceInfoServer/          # Spring Boot 앱
├── nginx/                    # Nginx 설정
├── docker-compose.yml        # 단일 compose 파일
├── Dockerfile
└── init-postgres.sh          # DB 초기화 스크립트
```

### 개선이 필요한 부분

| 항목 | 현재 상태 | 목표 상태 |
|------|----------|----------|
| 디렉토리 구조 | 루트에 모든 파일 혼재 | prod/ 디렉토리 분리 |
| DB 구성 | 단일 PostgreSQL | Master/Slave 복제 |
| Docker Compose | 단일 파일 | 앱용 + 인프라용 분리 |
| 배포 | 수동 | deploy.sh 자동화 |
| 로그 관리 | 기본 | log-cleaner 컨테이너 |
| 네트워크 | 단일 | 서비스별 + 공유 인프라 분리 |

---

## 목표 아키텍처

### 디렉토리 구조
```
PLACE_INFO_SERVICE_SERVER/
├── prod/                              # NEW: 프로덕션 전용
│   ├── docker-compose.yml             # 앱 + DB 스택
│   ├── docker-compose.infra.yml       # 공유 인프라 (Kafka, Redis, Zipkin)
│   ├── init-scripts/                  # DB 초기화 SQL
│   │   └── init-place-info-db.sql
│   ├── nginx/                         # Nginx 설정
│   │   └── nginx.conf
│   ├── deploy.sh                      # 배포 자동화
│   └── .env.example                   # 환경변수 예시
├── PlaceInfoServer/                   # 기존 유지
├── docs/                              # 기존 유지
├── docker-compose.yml                 # 로컬 개발용으로 유지
└── Dockerfile                         # 기존 유지 (멀티플랫폼 개선)
```

### 네트워크 토폴로지
```
                    Internet
                        │
                        ▼
                  ┌─────────────┐
                  │   Nginx     │ Port 9412
                  │ (외부 노출) │
                  └─────┬───────┘
                        │
          ┌─────────────┼─────────────┐
          │     place-info-network    │ (내부 전용)
          │             │             │
          ▼             ▼             ▼
    ┌──────────┐  ┌──────────┐  ┌──────────┐
    │ App      │  │ Postgres │  │ Postgres │
    │ Server   │  │ Master   │  │ Slave    │
    └────┬─────┘  └──────────┘  └──────────┘
         │
         │ (infra-network - 외부)
         ▼
    ┌──────────────────────────────────────┐
    │        공유 인프라 (infra-network)     │
    │  ┌───────┐  ┌───────┐  ┌───────┐     │
    │  │Kafka  │  │Redis  │  │Zipkin │     │
    │  │Cluster│  │       │  │       │     │
    │  └───────┘  └───────┘  └───────┘     │
    └──────────────────────────────────────┘
```

---

## 구현 계획

### Phase 1: 디렉토리 구조 생성
- [ ] `prod/` 디렉토리 생성
- [ ] `prod/init-scripts/` 디렉토리 생성
- [ ] `prod/nginx/` 디렉토리 생성
- [ ] `.env.example` 파일 생성

**Parallelizable**: YES (모든 디렉토리/파일 생성은 독립적)

### Phase 2: PostgreSQL Master/Slave 설정
- [ ] `prod/docker-compose.yml` 작성 - Master/Slave DB 포함
- [ ] `prod/init-scripts/init-place-info-db.sql` 작성
- [ ] Bitnami PostgreSQL 이미지 사용 (복제 기능 내장)

**Parallelizable**: NO (Phase 1 완료 후 진행)

### Phase 3: 공유 인프라 분리
- [ ] `prod/docker-compose.infra.yml` 작성
  - Kafka 3-node 클러스터 (KRaft 모드)
  - Redis (영구 저장소)
  - Zipkin (분산 추적)
- [ ] 네트워크 설정 (infra-network external)

**Parallelizable**: NO (Phase 2와 연계 필요)

### Phase 4: Nginx 및 로그 관리
- [ ] `prod/nginx/nginx.conf` 작성 (헬스체크, 프록시 설정)
- [ ] log-cleaner 컨테이너 추가

**Parallelizable**: YES (Phase 2와 병렬 가능)

### Phase 5: 배포 자동화
- [ ] `prod/deploy.sh` 스크립트 작성
- [ ] Dockerfile 멀티플랫폼 빌드 개선

**Parallelizable**: YES (Phase 2, 3, 4와 병렬 가능)

### Phase 6: 애플리케이션 설정 개선
- [ ] `application-prod.yaml` 개선
  - Master/Slave DataSource 라우팅 (선택적)
  - 분산 추적 설정 추가
  - 헬스체크 엔드포인트 강화

**Parallelizable**: NO (Phase 2 완료 후 진행)

---

## 상세 설계

### 1. PostgreSQL Master/Slave 구성

```yaml
# prod/docker-compose.yml (일부)
services:
  postgres-master:
    image: bitnami/postgresql:16
    environment:
      POSTGRESQL_REPLICATION_MODE: master
      POSTGRESQL_REPLICATION_USER: repl_user
      POSTGRESQL_REPLICATION_PASSWORD: ${REPLICATION_PASSWORD}
      POSTGRESQL_USERNAME: ${DB_USERNAME}
      POSTGRESQL_PASSWORD: ${DB_PASSWORD}
      POSTGRESQL_DATABASE: ${DB_NAME}
      POSTGRESQL_EXTRA_FLAGS: "-c max_connections=200"
    volumes:
      - postgres-master-data:/bitnami/postgresql
      - ./init-scripts:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME} -d ${DB_NAME}"]
      interval: 10s
      timeout: 5s
      retries: 5

  postgres-slave:
    image: bitnami/postgresql:16
    depends_on:
      postgres-master:
        condition: service_healthy
    environment:
      POSTGRESQL_REPLICATION_MODE: slave
      POSTGRESQL_MASTER_HOST: postgres-master
      POSTGRESQL_MASTER_PORT_NUMBER: 5432
      POSTGRESQL_REPLICATION_USER: repl_user
      POSTGRESQL_REPLICATION_PASSWORD: ${REPLICATION_PASSWORD}
      POSTGRESQL_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-slave-data:/bitnami/postgresql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME}"]
      interval: 10s
      timeout: 5s
      retries: 5
```

### 2. 환경변수 (.env.example)

```bash
# Database - Master
DB_HOST=postgres-master
DB_PORT=5432
DB_NAME=place
DB_USERNAME=placeuser
DB_PASSWORD=your_secure_password

# Replication
REPLICATION_PASSWORD=replication_secure_password

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# Kafka
KAFKA_BROKER1=kafka1:29091
KAFKA_BROKER2=kafka2:29092
KAFKA_BROKER3=kafka3:29093

# Application
SPRING_PROFILES_ACTIVE=prod
LOG_LEVEL=INFO

# Tracing
ZIPKIN_ENDPOINT=http://zipkin:9411/api/v2/spans
TRACING_SAMPLING_PROBABILITY=0.1
```

### 3. 배포 스크립트 (deploy.sh)

```bash
#!/bin/bash
set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Configuration
COMPOSE_FILE="docker-compose.yml"
INFRA_COMPOSE_FILE="docker-compose.infra.yml"
SERVICE_NAME="place-info-server"

# Functions
log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    command -v docker >/dev/null 2>&1 || { log_error "Docker not found"; exit 1; }
    command -v docker-compose >/dev/null 2>&1 || { log_error "Docker Compose not found"; exit 1; }

    if [ ! -f ".env" ]; then
        log_error ".env file not found. Copy .env.example to .env and configure."
        exit 1
    fi
}

# Ensure infra network exists
ensure_network() {
    log_info "Ensuring infra-network exists..."
    docker network create infra-network 2>/dev/null || true
}

# Deploy infrastructure (if needed)
deploy_infra() {
    log_info "Deploying infrastructure..."
    docker-compose -f $INFRA_COMPOSE_FILE up -d
}

# Deploy application
deploy_app() {
    log_info "Deploying application..."
    docker-compose -f $COMPOSE_FILE pull
    docker-compose -f $COMPOSE_FILE up -d
}

# Health check
health_check() {
    log_info "Waiting for services to be healthy..."
    sleep 10

    # Check app health
    for i in {1..30}; do
        if curl -sf http://localhost:9412/actuator/health > /dev/null 2>&1; then
            log_info "Application is healthy!"
            return 0
        fi
        sleep 2
    done

    log_error "Health check failed"
    return 1
}

# Main
main() {
    check_prerequisites
    ensure_network

    case "${1:-deploy}" in
        infra)
            deploy_infra
            ;;
        app)
            deploy_app
            health_check
            ;;
        deploy|*)
            deploy_infra
            deploy_app
            health_check
            ;;
    esac

    log_info "Deployment completed!"
}

main "$@"
```

---

## 분산 트레이싱 (Distributed Tracing) 설계

### 아키텍처

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│ Place Info  │───▶│   Kafka     │
│   Request   │    │   Service   │    │  (Events)   │
└─────────────┘    └──────┬──────┘    └─────────────┘
                         │
                         │ Trace Data (B3 Propagation)
                         ▼
                   ┌─────────────┐
                   │   Zipkin    │
                   │   Server    │
                   └──────┬──────┘
                         │
                         ▼
                   ┌─────────────┐
                   │   Zipkin    │
                   │     UI      │
                   │ :9411/zipkin│
                   └─────────────┘
```

### 구현 내용

#### 1. 의존성 추가 (build.gradle)
```gradle
// Micrometer Tracing + Zipkin
implementation 'io.micrometer:micrometer-tracing-bridge-brave'
implementation 'io.zipkin.reporter2:zipkin-reporter-brave'
```

#### 2. application-prod.yaml 설정
```yaml
management:
  tracing:
    sampling:
      probability: ${TRACING_SAMPLING_PROBABILITY:0.1}  # 프로덕션: 10% 샘플링
    propagation:
      type: b3  # Kafka 등 외부 서비스와 호환
  zipkin:
    tracing:
      endpoint: ${ZIPKIN_ENDPOINT:http://zipkin:9411/api/v2/spans}
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

#### 3. Zipkin 인프라 (docker-compose.infra.yml)
```yaml
zipkin:
  image: openzipkin/zipkin:latest
  container_name: zipkin
  ports:
    - "9411:9411"
  environment:
    - STORAGE_TYPE=mem  # 프로덕션에서는 Elasticsearch 권장
  networks:
    - infra-network
  healthcheck:
    test: ["CMD", "wget", "-q", "--spider", "http://localhost:9411/health"]
    interval: 10s
    timeout: 5s
    retries: 3
```

### Trace 전파 흐름

1. **HTTP 요청 수신** → TraceId 생성 (또는 헤더에서 추출)
2. **서비스 내부 처리** → SpanId로 각 단계 추적
3. **Kafka 이벤트 발행** → B3 헤더로 TraceId 전파
4. **외부 서비스 호출** → 동일 TraceId 유지
5. **Zipkin으로 전송** → 비동기 리포팅

### 샘플링 전략

| 환경 | 샘플링 비율 | 이유 |
|------|------------|------|
| **개발** | 1.0 (100%) | 모든 요청 추적 |
| **프로덕션** | 0.1 (10%) | 성능 영향 최소화 |
| **디버깅 시** | 1.0 (100%) | 임시로 전체 추적 |

---

## 코드 개선점 (분석 결과)

### 발견된 개선 영역

| 영역 | 현재 상태 | 개선 제안 | 우선순위 |
|------|----------|----------|---------|
| **분산 추적** | 없음 | Zipkin + Micrometer 연동 | **높음** |
| **캐싱** | Redis 연결만 설정, 실제 캐싱 미사용 | Keyword 등 정적 데이터 캐싱 추가 | 중 |
| **Read Replica 활용** | 없음 | CQRS Query에 Slave 연결 (선택적) | 낮음 |
| **메트릭** | 기본 Actuator | Prometheus 메트릭 노출 | 중 |
| **로그 구조화** | 기본 | JSON 로그 포맷 (ELK 연동 대비) | 낮음 |
| **API 문서** | Swagger 분리됨 | 유지 (이미 양호) | - |
| **CQRS 패턴** | 잘 분리됨 | 유지 (이미 양호) | - |
| **DDD 패턴** | 잘 적용됨 | 유지 (이미 양호) | - |

### 즉시 개선 가능한 항목 (이번 작업 범위)

1. **분산 추적 설정 추가**
   - `build.gradle`에 Micrometer + Zipkin 의존성 추가
   - `application-prod.yaml`에 트레이싱 설정 추가
   - `docker-compose.infra.yml`에 Zipkin 서버 추가
2. **Prometheus 메트릭 노출** (Actuator 설정)
3. **캐싱 어노테이션 추가** (Keyword 서비스)

### 향후 개선 고려 사항 (이번 작업 범위 외)

1. Read/Write DataSource 분리 (AbstractRoutingDataSource)
2. Circuit Breaker 패턴 (Resilience4j)
3. Rate Limiting

---

## 예상 결과물

### 생성될 파일 목록

```
prod/
├── docker-compose.yml           # 앱 + DB (M/S)
├── docker-compose.infra.yml     # Kafka, Redis, Zipkin
├── init-scripts/
│   └── init-place-info-db.sql   # PostGIS 확장, 테이블 생성
├── nginx/
│   └── nginx.conf               # 리버스 프록시 설정
├── deploy.sh                    # 배포 자동화
└── .env.example                 # 환경변수 템플릿
```

### 수정될 파일 목록

```
PlaceInfoServer/
├── build.gradle                 # Micrometer + Zipkin 의존성 추가
└── src/main/resources/
    └── application-prod.yaml    # 분산 추적, 메트릭 설정 추가
```

---

## 작업 순서 (의존성 고려)

```
Phase 1 (독립)
    │
    ├── [Task 1] prod/ 디렉토리 구조 생성
    │
    ▼
Phase 2 (순차)
    │
    ├── [Task 2] prod/docker-compose.yml 작성 (M/S DB)
    ├── [Task 3] prod/init-scripts/init-place-info-db.sql 작성
    │
    ▼
Phase 3 (Phase 2 완료 후)
    │
    ├── [Task 4] prod/docker-compose.infra.yml 작성 (Kafka, Redis, Zipkin 포함)
    │
    ▼
Phase 4 (Phase 2와 병렬 가능)
    │
    ├── [Task 5] prod/nginx/nginx.conf 작성
    ├── [Task 6] log-cleaner 설정 추가
    │
    ▼
Phase 5 (Phase 2, 3, 4와 병렬 가능)
    │
    ├── [Task 7] prod/deploy.sh 작성
    ├── [Task 8] prod/.env.example 작성
    │
    ▼
Phase 6 (분산 트레이싱 설정)
    │
    ├── [Task 9] build.gradle에 Micrometer + Zipkin 의존성 추가
    ├── [Task 10] application-prod.yaml 개선 (트레이싱, 메트릭 설정)
    │
    ▼
Phase 7 (검증)
    │
    └── [Task 11] 전체 구성 검증 및 문서화
```

---

## 승인 요청

위 기획대로 진행해도 될까요?

**주요 결정 사항:**
1. PostgreSQL Master/Slave에 bitnami/postgresql 이미지 사용
2. 공유 인프라(Kafka, Redis, Zipkin)는 별도 compose 파일로 분리
3. 기존 루트의 docker-compose.yml은 로컬 개발용으로 유지
4. Read Replica를 활용한 DataSource 라우팅은 이번 범위에서 제외 (추후 선택적 구현)

진행 확인 후 구현을 시작하겠습니다.
