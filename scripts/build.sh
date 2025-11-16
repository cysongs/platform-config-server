#!/bin/bash
# Quick build script for development

set -e

echo "Building config-server..."
./mvnw clean package -DskipTests

echo ""
echo "Build successful! JAR location:"
ls -lh target/*.jar

echo ""
echo "To run the application:"
echo "  ./mvnw spring-boot:run"
echo "  OR"
echo "  java -jar target/config-server-1.0.0-SNAPSHOT.jar"
