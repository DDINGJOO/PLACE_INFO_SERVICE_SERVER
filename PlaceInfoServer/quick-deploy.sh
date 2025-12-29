#!/bin/bash

# Quick Deploy Script - Build and Push in one command
# Usage: ./quick-deploy.sh [version]

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

VERSION=${1:-"latest"}

echo -e "${GREEN}🚀 Quick Deploy - PlaceInfoServer${NC}"
echo -e "${YELLOW}Version: ${VERSION}${NC}\n"

# Run the appropriate build script
if [ "$VERSION" == "latest" ]; then
    ./docker-build-push.sh
else
    ./docker-build-push-with-version.sh ${VERSION}
fi

echo -e "\n${GREEN}✅ Deployment complete!${NC}"