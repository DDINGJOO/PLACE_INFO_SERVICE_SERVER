# PostgreSQL 초기 설정 가이드

## 1. 데이터베이스 및 사용자 생성

PostgreSQL 컨테이너에 접속하여 데이터베이스와 사용자를 생성해야 합니다.

### 컨테이너 접속

```bash
docker exec -it postgres psql -U postgres
```

### SQL 실행

```sql
-- 1. 데이터베이스 생성
CREATE DATABASE place WITH ENCODING 'UTF8';

-- 2. 사용자 생성
CREATE USER placeuser WITH PASSWORD 'your_password_here';

-- 3. 권한 부여
GRANT ALL PRIVILEGES ON DATABASE place TO placeuser;

-- 4. 데이터베이스 연결
\c place

-- 5. 스키마 권한 부여 (PostgreSQL 15+에서 필요)
GRANT ALL ON SCHEMA public TO placeuser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO placeuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO placeuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO placeuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO placeuser;

-- 6. 필수 Extension 설치
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";
CREATE EXTENSION IF NOT EXISTS "btree_gist";

-- 확인
\dx
```

### 빠른 실행 (한 줄로)

```bash
docker exec -it postgres psql -U postgres -c "
CREATE DATABASE place WITH ENCODING 'UTF8';
CREATE USER placeuser WITH PASSWORD 'your_password_here';
GRANT ALL PRIVILEGES ON DATABASE place TO placeuser;
"

docker exec -it postgres psql -U postgres -d place -c "
GRANT ALL ON SCHEMA public TO placeuser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO placeuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO placeuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO placeuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO placeuser;
CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";
CREATE EXTENSION IF NOT EXISTS \"postgis\";
CREATE EXTENSION IF NOT EXISTS \"btree_gist\";
"
```

## 2. 스키마 적용

### 방법 1: 파일 복사 후 실행

```bash
# 스키마 파일을 컨테이너로 복사
docker cp PlaceInfoServer/src/main/resources/sql/schema.sql postgres:/tmp/schema.sql

# 스키마 실행
docker exec -it postgres psql -U placeuser -d place -f /tmp/schema.sql
```

### 방법 2: 직접 실행

```bash
docker exec -i postgres psql -U placeuser -d place < PlaceInfoServer/src/main/resources/sql/schema.sql
```

## 3. 초기 데이터 적용 (선택사항)

### Keywords 마스터 데이터

```bash
docker cp PlaceInfoServer/src/main/resources/sql/data-keywords.sql postgres:/tmp/data-keywords.sql
docker exec -it postgres psql -U placeuser -d place -f /tmp/data-keywords.sql
```

### 샘플 데이터 (개발/테스트용)

```bash
docker cp PlaceInfoServer/src/main/resources/sql/data-sample.sql postgres:/tmp/data-sample.sql
docker exec -it postgres psql -U placeuser -d place -f /tmp/data-sample.sql
```

## 4. 연결 확인

```bash
# 테이블 목록 확인
docker exec -it postgres psql -U placeuser -d place -c "\dt"

# Extension 확인
docker exec -it postgres psql -U placeuser -d place -c "\dx"

# 샘플 쿼리 실행
docker exec -it postgres psql -U placeuser -d place -c "SELECT count(*) FROM keywords;"
```

## 5. 환경 변수 설정

`.env` 파일을 생성하여 데이터베이스 비밀번호를 설정하세요:

```bash
# .env
DB_PASSWORD=your_password_here
REDIS_PASSWORD=
```

## 6. 문제 해결

### PostGIS Extension 설치 실패 시

PostGIS 이미지를 사용하고 있는지 확인:

```yaml
postgres:
  image: postgis/postgis:16-3.4-alpine
```

### 권한 오류 발생 시

```sql
-- postgres 사용자로 접속하여 실행
\c place postgres
GRANT ALL ON SCHEMA public TO placeuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO placeuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO placeuser;
```

### 연결 테스트

```bash
docker exec -it postgres psql -U placeuser -d place -c "SELECT version();"
```

## 7. 전체 초기화 스크립트

모든 작업을 한 번에 실행:

```bash
#!/bin/bash

# 1. 데이터베이스 및 사용자 생성
docker exec -it postgres psql -U postgres -c "
CREATE DATABASE place WITH ENCODING 'UTF8';
CREATE USER placeuser WITH PASSWORD 'pass123';
GRANT ALL PRIVILEGES ON DATABASE place TO placeuser;
"

# 2. 스키마 권한 및 Extension 설정
docker exec -it postgres psql -U postgres -d place -c "
GRANT ALL ON SCHEMA public TO placeuser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO placeuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO placeuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO placeuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO placeuser;
CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";
CREATE EXTENSION IF NOT EXISTS \"postgis\";
CREATE EXTENSION IF NOT EXISTS \"btree_gist\";
"

# 3. 스키마 적용
docker cp PlaceInfoServer/src/main/resources/sql/schema.sql postgres:/tmp/schema.sql
docker exec -it postgres psql -U placeuser -d place -f /tmp/schema.sql

# 4. 초기 데이터 적용
docker cp PlaceInfoServer/src/main/resources/sql/data-keywords.sql postgres:/tmp/data-keywords.sql
docker exec -it postgres psql -U placeuser -d place -f /tmp/data-keywords.sql

echo "PostgreSQL 초기화 완료!"
```
