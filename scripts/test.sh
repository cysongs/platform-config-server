#!/bin/bash
# Run tests

set -e

echo "Running unit tests..."
./mvnw test

echo ""
echo "✅ All tests passed!"
echo ""
echo "To run integration tests (requires GitHub credentials):"
echo "  1. Set GIT_USERNAME and GIT_TOKEN"
echo "  2. Remove @Disabled from ConfigServerIT.java"
echo "  3. Run: ./mvnw test"
