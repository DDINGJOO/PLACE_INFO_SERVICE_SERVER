#!/bin/bash

# Place Info Server - 개발 환경 시작 스크립트
# Mac 환경 최적화

set -e

echo "🚀 Place Info Server - 개발 환경 시작"
echo "======================================"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Docker Compose 파일 확인
if [ ! -f "docker-compose.dev.yml" ]; then
    echo -e "${RED}❌ docker-compose.dev.yml 파일을 찾을 수 없습니다.${NC}"
    exit 1
fi

# 기존 컨테이너 중지 (선택)
read -p "기존 컨테이너를 중지하시겠습니까? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}⏸  기존 컨테이너 중지 중...${NC}"
    docker-compose -f docker-compose.dev.yml down
fi

# Docker Compose 시작
echo -e "${GREEN}🐳 Docker 컨테이너 시작 중...${NC}"
docker-compose -f docker-compose.dev.yml up -d

# 서비스 준비 대기
echo -e "${YELLOW}⏳ 서비스 준비 중...${NC}"
sleep 5

# 헬스 체크
echo ""
echo "🔍 서비스 상태 확인"
echo "===================="

# PostgreSQL 체크
if docker exec place-postgres-dev pg_isready -U user -d place > /dev/null 2>&1; then
    echo -e "${GREEN}✅ PostgreSQL: 정상${NC}"
else
    echo -e "${RED}❌ PostgreSQL: 연결 실패${NC}"
fi

# Redis 체크
if docker exec place-redis-dev redis-cli ping > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Redis: 정상${NC}"
else
    echo -e "${RED}❌ Redis: 연결 실패${NC}"
fi

# Kafka 체크 (최대 30초 대기)
echo -e "${YELLOW}⏳ Kafka 시작 대기 중 (최대 30초)...${NC}"
for i in {1..6}; do
    if docker exec place-kafka-dev kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Kafka: 정상${NC}"
        break
    fi
    if [ $i -eq 6 ]; then
        echo -e "${RED}❌ Kafka: 연결 실패 (재시작 필요할 수 있음)${NC}"
    else
        sleep 5
    fi
done

echo ""
echo "📊 서비스 접속 정보"
echo "===================="
echo "PostgreSQL:  localhost:5432 (user: user, db: place)"
echo "Redis:       localhost:6379"
echo "Kafka:       localhost:9092"
echo "Kafka UI:    http://localhost:8989"
echo "pgAdmin:     http://localhost:5050"

echo ""
echo -e "${GREEN}✨ 개발 환경 시작 완료!${NC}"
echo ""
echo "📝 유용한 명령어:"
echo "  ./stop-dev.sh                           # 전체 중지"
echo "  docker-compose -f docker-compose.dev.yml logs -f    # 로그 확인"
echo "  docker-compose -f docker-compose.dev.yml ps         # 상태 확인"
echo ""
echo "🚀 Spring Boot 애플리케이션 실행:"
echo "  ./gradlew bootRun --args='--spring.profiles.active=dev'"
