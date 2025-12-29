#!/bin/bash

# Docker Multi-Architecture Build and Push Script
# Supports: linux/amd64, linux/arm64

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
IMAGE_NAME="ddingsh9/placeinfoserver"
IMAGE_TAG="latest"
PLATFORMS="linux/amd64,linux/arm64"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Docker Multi-Arch Build & Push${NC}"
echo -e "${GREEN}========================================${NC}"

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
echo "Image: ${IMAGE_NAME}:${IMAGE_TAG}"
echo "Platforms: ${PLATFORMS}"

docker buildx build \
    --platform ${PLATFORMS} \
    -t ${IMAGE_NAME}:${IMAGE_TAG} \
    --push \
    .

if [ $? -eq 0 ]; then
    echo -e "\n${GREEN}✓ Build and push successful!${NC}"

    # Step 5: Verify the pushed image
    echo -e "\n${YELLOW}Step 5: Verifying pushed image...${NC}"
    docker buildx imagetools inspect ${IMAGE_NAME}:${IMAGE_TAG}

    echo -e "\n${GREEN}========================================${NC}"
    echo -e "${GREEN}  Build Complete!${NC}"
    echo -e "${GREEN}  Image: ${IMAGE_NAME}:${IMAGE_TAG}${NC}"
    echo -e "${GREEN}========================================${NC}"
else
    echo -e "\n${RED}✗ Build failed${NC}"
    exit 1
fi