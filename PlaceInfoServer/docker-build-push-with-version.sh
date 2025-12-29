#!/bin/bash

# Docker Multi-Architecture Build and Push Script with Version Support
# Supports: linux/amd64, linux/arm64

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
IMAGE_NAME="ddingsh9/placeinfoserver"
PLATFORMS="linux/amd64,linux/arm64"

# Parse arguments
VERSION=${1:-""}

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Docker Multi-Arch Build & Push${NC}"
echo -e "${GREEN}========================================${NC}"

# Check if version is provided
if [ -z "$VERSION" ]; then
    echo -e "${YELLOW}Usage: $0 <version>${NC}"
    echo -e "${YELLOW}Example: $0 1.0.0${NC}"
    echo -e "\n${BLUE}Building with 'latest' tag only...${NC}"
    TAGS="-t ${IMAGE_NAME}:latest"
else
    echo -e "${BLUE}Building with version ${VERSION} and latest tag...${NC}"
    TAGS="-t ${IMAGE_NAME}:latest -t ${IMAGE_NAME}:${VERSION}"
fi

# Step 1: Check Docker login status
echo -e "\n${YELLOW}Step 1: Checking Docker login status...${NC}"
if ! docker info >/dev/null 2>&1; then
    echo -e "${RED}Docker is not running or not installed${NC}"
    exit 1
fi

if ! docker buildx version >/dev/null 2>&1; then
    echo -e "${RED}Docker buildx is not available${NC}"
    exit 1
fi

# Check if user is logged in to Docker Hub
if ! docker system info 2>/dev/null | grep -q "Username"; then
    echo -e "${YELLOW}You are not logged in to Docker Hub${NC}"
    echo -e "${YELLOW}Please run: docker login${NC}"
    read -p "Do you want to login now? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker login
    else
        echo -e "${RED}Cannot push without login. Exiting...${NC}"
        exit 1
    fi
fi

# Step 2: Setup buildx builder (if not exists)
echo -e "\n${YELLOW}Step 2: Setting up buildx builder...${NC}"
BUILDER_NAME="multiarch-builder"

if ! docker buildx ls | grep -q ${BUILDER_NAME}; then
    echo "Creating new buildx builder: ${BUILDER_NAME}"
    docker buildx create --name ${BUILDER_NAME} --use
    docker buildx inspect --bootstrap
else
    echo "Using existing buildx builder: ${BUILDER_NAME}"
    docker buildx use ${BUILDER_NAME}
fi

# Step 3: Build application JAR
echo -e "\n${YELLOW}Step 3: Building application JAR...${NC}"
if [ -f "./gradlew" ]; then
    ./gradlew clean bootJar
    echo -e "${GREEN}✓ JAR build completed${NC}"
else
    echo -e "${RED}gradlew not found in current directory${NC}"
    exit 1
fi

# Step 4: Build and push Docker image
echo -e "\n${YELLOW}Step 4: Building and pushing Docker image...${NC}"
echo "Image: ${IMAGE_NAME}"
echo "Tags: latest${VERSION:+, $VERSION}"
echo "Platforms: ${PLATFORMS}"

# Build with progress output
docker buildx build \
    --platform ${PLATFORMS} \
    ${TAGS} \
    --push \
    --progress=plain \
    .

if [ $? -eq 0 ]; then
    echo -e "\n${GREEN}✓ Build and push successful!${NC}"

    # Step 5: Verify the pushed image
    echo -e "\n${YELLOW}Step 5: Verifying pushed image...${NC}"
    docker buildx imagetools inspect ${IMAGE_NAME}:latest

    # Display summary
    echo -e "\n${GREEN}========================================${NC}"
    echo -e "${GREEN}  Build Complete!${NC}"
    echo -e "${GREEN}  Image: ${IMAGE_NAME}${NC}"
    echo -e "${GREEN}  Tags: latest${VERSION:+, $VERSION}${NC}"
    echo -e "${GREEN}  Platforms: ${PLATFORMS}${NC}"
    echo -e "${GREEN}========================================${NC}"

    # Show pull commands
    echo -e "\n${BLUE}Pull commands:${NC}"
    echo -e "  docker pull ${IMAGE_NAME}:latest"
    [ ! -z "$VERSION" ] && echo -e "  docker pull ${IMAGE_NAME}:${VERSION}"
else
    echo -e "\n${RED}✗ Build failed${NC}"
    exit 1
fi