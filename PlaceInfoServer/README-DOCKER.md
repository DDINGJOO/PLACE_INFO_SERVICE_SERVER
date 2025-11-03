# Docker 개발 환경 가이드

## 📦 포함된 서비스

| 서비스                  | 포트                    | 설명                                       |
|----------------------|-----------------------|------------------------------------------|
| PostgreSQL (PostGIS) | 5432                  | 메인 데이터베이스 (공간 데이터 지원)                    |
| Redis                | 6379                  | 캐시 및 세션 저장소                              |
| Kafka                | 9092 (외부), 29092 (내부) | 메시지 브로커 (단일 브로커)                         |
| Zookeeper            | 2181                  | Kafka 의존성                                |
| Kafka UI             | 8989                  | Kafka 모니터링 (http://localhost:8989)       |
| pgAdmin              | 5050                  | PostgreSQL 관리 도구 (http://localhost:5050) |

## 🚀 빠른 시작

### 1. 전체 인프라 시작

```bash
docker-compose -f docker-compose.dev.yml up -d
```

### 2. 로그 확인

```bash
# 전체 로그
docker-compose -f docker-compose.dev.yml logs -f

# 특정 서비스 로그
docker-compose -f docker-compose.dev.yml logs -f postgres
docker-compose -f docker-compose.dev.yml logs -f kafka
```

### 3. 상태 확인

```bash
docker-compose -f docker-compose.dev.yml ps
```

### 4. 인프라 중지

```bash
# 컨테이너 중지 (데이터 유지)
docker-compose -f docker-compose.dev.yml stop

# 컨테이너 삭제 (데이터 유지)
docker-compose -f docker-compose.dev.yml down

# 컨테이너 + 볼륨 삭제 (데이터 초기화)
docker-compose -f docker-compose.dev.yml down -v
```

## 🔧 개별 서비스 시작/중지

```bash
# PostgreSQL만 시작
docker-compose -f docker-compose.dev.yml up -d postgres

# Kafka 제외하고 시작
docker-compose -f docker-compose.dev.yml up -d postgres redis

# 특정 서비스 재시작
docker-compose -f docker-compose.dev.yml restart kafka
```

## 📊 서비스 접속 정보

### PostgreSQL

```yaml
Host: localhost
Port: 5432
Database: place
Username: user
Password: pass123#
```

**psql 접속:**

```bash
docker exec -it place-postgres-dev psql -U user -d place
```

**테이블 확인:**

```sql
\dt
\d place_info
```

### Redis

```yaml
Host: localhost
Port: 6379
```

**redis-cli 접속:**

```bash
docker exec -it place-redis-dev redis-cli
```

**명령어 테스트:**

```bash
PING
KEYS *
```

### Kafka

**Spring Boot application.yml 설정:**

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

**토픽 생성:**

```bash
docker exec -it place-kafka-dev kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic place-image-changed \
  --partitions 3 \
  --replication-factor 1
```

**토픽 목록 확인:**

```bash
docker exec -it place-kafka-dev kafka-topics \
  --bootstrap-server localhost:9092 \
  --list
```

**메시지 프로듀서 테스트:**

```bash
docker exec -it place-kafka-dev kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic place-image-changed
```

**메시지 컨슈머 테스트:**

```bash
docker exec -it place-kafka-dev kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic place-image-changed \
  --from-beginning
```

### Kafka UI

- URL: http://localhost:8989
- 브라우저에서 Kafka 토픽, 메시지, 컨슈머 그룹 모니터링 가능

### pgAdmin

- URL: http://localhost:5050
- Email: admin@place.com
- Password: admin123

**서버 등록 방법:**

1. pgAdmin 접속
2. "Add New Server" 클릭
3. General 탭: Name = "Place DB Dev"
4. Connection 탭:
	- Host: place-postgres-dev (또는 host.docker.internal)
	- Port: 5432
	- Username: user
	- Password: pass123#

## 🔍 헬스 체크

모든 서비스가 정상인지 확인:

```bash
# PostgreSQL
docker exec place-postgres-dev pg_isready -U user -d place

# Redis
docker exec place-redis-dev redis-cli ping

# Kafka
docker exec place-kafka-dev kafka-broker-api-versions --bootstrap-server localhost:9092
```

## 🛠 트러블슈팅

### 1. 포트 충돌

**증상:** "port is already allocated" 오류

**해결:**

```bash
# 포트 사용 중인 프로세스 확인 (Mac)
lsof -i :5432
lsof -i :6379
lsof -i :9092

# 프로세스 종료
kill -9 <PID>
```

### 2. Kafka 연결 실패

**증상:** "Connection refused" 또는 "Broker may not be available"

**해결:**

```bash
# Kafka 로그 확인
docker-compose -f docker-compose.dev.yml logs kafka

# Kafka 재시작
docker-compose -f docker-compose.dev.yml restart kafka

# Zookeeper 상태 확인
docker exec place-zookeeper-dev nc -z localhost 2181
```

### 3. PostgreSQL 연결 실패

**증상:** "Connection to database failed"

**해결:**

```bash
# PostgreSQL 로그 확인
docker-compose -f docker-compose.dev.yml logs postgres

# 컨테이너 내부에서 연결 테스트
docker exec -it place-postgres-dev psql -U user -d place -c "SELECT version();"
```

### 4. 메모리 부족 (Mac M1/M2)

**증상:** 컨테이너가 자주 재시작되거나 느림

**해결:**

1. Docker Desktop 설정에서 메모리 증가 (최소 4GB 권장)
2. Kafka 메모리 설정 조정:
   ```yaml
   KAFKA_HEAP_OPTS: "-Xmx512M -Xms256M"  # 더 낮춤
   ```

### 5. 볼륨 데이터 초기화

```bash
# 모든 데이터 삭제하고 재시작
docker-compose -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.dev.yml up -d

# 특정 볼륨만 삭제
docker volume rm place-info-service_postgres-data
```

## 🎯 Spring Boot 애플리케이션 연동

### application-dev.yaml 설정

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/place
    username: user
    password: pass123#
    driver-class-name: org.postgresql.Driver

  data:
    redis:
      host: localhost
      port: 6379

  kafka:
    bootstrap-servers: localhost:9092
    producer:
      retries: 3
    consumer:
      group-id: place-consumer-group
      auto-offset-reset: earliest
```

### 애플리케이션 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 📝 유용한 명령어

```bash
# 모든 컨테이너 리소스 사용량 확인
docker stats

# 특정 컨테이너 쉘 접속
docker exec -it place-postgres-dev /bin/bash
docker exec -it place-kafka-dev /bin/bash

# 컨테이너 재시작 (설정 변경 후)
docker-compose -f docker-compose.dev.yml restart

# 이미지 최신 버전으로 업데이트
docker-compose -f docker-compose.dev.yml pull
docker-compose -f docker-compose.dev.yml up -d

# 사용하지 않는 볼륨/이미지 정리
docker system prune -a --volumes
```

## 🔐 보안 주의사항

⚠️ **개발 환경 전용입니다!**

- 프로덕션에서는 비밀번호 변경 필수
- `.env.dev` 파일을 `.gitignore`에 추가
- 외부 접속이 필요한 경우 방화벽 설정 확인

## 📚 추가 리소스

- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
- [PostGIS 도커 이미지](https://hub.docker.com/r/postgis/postgis)
- [Confluent Kafka 가이드](https://docs.confluent.io/platform/current/installation/docker/installation.html)
- [Redis 도커 가이드](https://hub.docker.com/_/redis)
