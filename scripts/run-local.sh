#!/bin/bash
# Run the application locally with optional environment variables

echo "Starting Config Server..."
echo "If you need GitHub credentials, set GIT_USERNAME and GIT_TOKEN environment variables"
echo ""

# Check if credentials are set
if [ -z "$GIT_USERNAME" ] && [ -z "$GIT_TOKEN" ]; then
    echo "⚠️  GIT_USERNAME and GIT_TOKEN not set"
    echo "   For public repos, this is fine."
    echo "   For private repos, set them first:"
    echo "   export GIT_USERNAME=your-username"
    echo "   export GIT_TOKEN=ghp_your-token"
    echo ""
fi

# Run with local profile if application-local.yml exists
if [ -f "src/main/resources/application-local.yml" ]; then
    echo "Using local profile (application-local.yml)"
    exec ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
else
    exec ./mvnw spring-boot:run
fi
