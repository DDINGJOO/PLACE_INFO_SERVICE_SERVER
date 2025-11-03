#!/bin/bash

# Place Info Server - 개발 환경 중지 스크립트

set -e

echo "🛑 Place Info Server - 개발 환경 중지"
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

# 중지 옵션 선택
echo "중지 옵션을 선택하세요:"
echo "1) 컨테이너만 중지 (데이터 유지)"
echo "2) 컨테이너 삭제 (데이터 유지)"
echo "3) 컨테이너 + 볼륨 삭제 (데이터 초기화)"
read -p "선택 (1-3): " -n 1 -r
echo

case $REPLY in
    1)
        echo -e "${YELLOW}⏸  컨테이너 중지 중...${NC}"
        docker-compose -f docker-compose.dev.yml stop
        echo -e "${GREEN}✅ 컨테이너가 중지되었습니다.${NC}"
        echo "재시작: docker-compose -f docker-compose.dev.yml start"
        ;;
    2)
        echo -e "${YELLOW}🗑  컨테이너 삭제 중...${NC}"
        docker-compose -f docker-compose.dev.yml down
        echo -e "${GREEN}✅ 컨테이너가 삭제되었습니다. (데이터는 보존됨)${NC}"
        echo "재시작: ./start-dev.sh"
        ;;
    3)
        echo -e "${RED}⚠️  주의: 모든 데이터가 삭제됩니다!${NC}"
        read -p "정말 삭제하시겠습니까? (yes/no): " -r
        if [[ $REPLY == "yes" ]]; then
            echo -e "${YELLOW}🗑  컨테이너 및 볼륨 삭제 중...${NC}"
            docker-compose -f docker-compose.dev.yml down -v
            echo -e "${GREEN}✅ 컨테이너와 볼륨이 삭제되었습니다.${NC}"
            echo "재시작: ./start-dev.sh"
        else
            echo "취소되었습니다."
        fi
        ;;
    *)
        echo -e "${RED}잘못된 선택입니다.${NC}"
        exit 1
        ;;
esac
