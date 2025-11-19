#!/bin/bash

set -e

echo "=================================="
echo "PostgreSQL 초기화 스크립트 시작"
echo "=================================="

# 스크립트 실행 디렉토리로 이동
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "현재 디렉토리: $(pwd)"
echo ""

# 환경 변수 설정 (필요시 수정)
DB_NAME="place"
DB_USER="placeuser"
DB_PASSWORD="pass123"

# 1. 데이터베이스 및 사용자 생성
echo "1. 데이터베이스 및 사용자 생성 중..."
docker exec -i postgres psql -U postgres <<EOF
CREATE DATABASE ${DB_NAME} WITH ENCODING 'UTF8';
CREATE USER ${DB_USER} WITH PASSWORD '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER};
EOF

echo "데이터베이스 및 사용자 생성 완료!"

# 2. 스키마 권한 및 Extension 설정
echo "2. 스키마 권한 및 Extension 설정 중..."
docker exec -i postgres psql -U postgres -d ${DB_NAME} <<EOF
GRANT ALL ON SCHEMA public TO ${DB_USER};
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ${DB_USER};
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ${DB_USER};

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";
CREATE EXTENSION IF NOT EXISTS "btree_gist";
EOF

echo "스키마 권한 및 Extension 설정 완료!"

# 3. 스키마 파일 복사 및 적용
echo "3. 스키마 적용 중..."
docker cp PlaceInfoServer/src/main/resources/sql/schema.sql postgres:/tmp/schema.sql
docker exec -i postgres psql -U ${DB_USER} -d ${DB_NAME} -f /tmp/schema.sql

echo "스키마 적용 완료!"

# 4. 초기 데이터 적용
echo "4. 초기 데이터 적용 중..."
docker cp PlaceInfoServer/src/main/resources/sql/data-keywords.sql postgres:/tmp/data-keywords.sql
docker exec -i postgres psql -U ${DB_USER} -d ${DB_NAME} -f /tmp/data-keywords.sql

echo "초기 데이터 적용 완료!"

# 5. 확인
echo "5. 설정 확인 중..."
echo "Extension 목록:"
docker exec -i postgres psql -U ${DB_USER} -d ${DB_NAME} -c "\dx"

echo ""
echo "테이블 목록:"
docker exec -i postgres psql -U ${DB_USER} -d ${DB_NAME} -c "\dt"

echo ""
echo "Keywords 데이터 확인:"
docker exec -i postgres psql -U ${DB_USER} -d ${DB_NAME} -c "SELECT count(*) as keyword_count FROM keywords;"

echo ""
echo "=================================="
echo "PostgreSQL 초기화 완료!"
echo "=================================="
echo ""
echo "연결 정보:"
echo "  Host: localhost"
echo "  Port: 5432"
echo "  Database: ${DB_NAME}"
echo "  User: ${DB_USER}"
echo "  Password: ${DB_PASSWORD}"
echo ""
echo ".env 파일에 DB_PASSWORD=${DB_PASSWORD} 설정을 추가하세요!"