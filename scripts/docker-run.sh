#!/bin/bash
# Run Docker container

set -e

IMAGE_NAME=${1:-config-server}
IMAGE_TAG=${2:-latest}

echo "Running Docker container: ${IMAGE_NAME}:${IMAGE_TAG}"

# Check if credentials are set
if [ -z "$GIT_USERNAME" ] && [ -z "$GIT_TOKEN" ]; then
    echo "⚠️  GIT_USERNAME and GIT_TOKEN not set"
    echo "   For private repos, set them first"
    echo ""
fi

docker run --rm -p 8080:8080 \
  -e GIT_USERNAME="${GIT_USERNAME}" \
  -e GIT_TOKEN="${GIT_TOKEN}" \
  --name config-server \
  ${IMAGE_NAME}:${IMAGE_TAG}
