#!/bin/bash
# Build Docker image

set -e

IMAGE_NAME=${1:-config-server}
IMAGE_TAG=${2:-latest}

echo "Building Docker image: ${IMAGE_NAME}:${IMAGE_TAG}"
docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .

echo ""
echo "✅ Docker image built successfully!"
echo ""
echo "To run the container:"
echo "  docker run --rm -p 8080:8080 \\"
echo "    -e GIT_USERNAME=\$GIT_USERNAME \\"
echo "    -e GIT_TOKEN=\$GIT_TOKEN \\"
echo "    ${IMAGE_NAME}:${IMAGE_TAG}"
