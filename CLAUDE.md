# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Platform Config Server is a centralized configuration management service built with Spring Boot 3.4.0 and Spring Cloud Config Server 2024.0.0. It serves configuration files from a Git repository (https://github.com/dsrvlabs/platform-config) and provides both standard Spring Cloud Config endpoints and a custom YAML download endpoint.

## Technology Stack

- Java 21
- Spring Boot 3.4.0
- Spring Cloud 2024.0.0 (spring-cloud-config-server)
- Maven build system
- Docker with Eclipse Temurin base image

## Build and Development Commands

### Build
```bash
./mvnw clean package          # Build with tests
./mvnw package -DskipTests    # Build without tests
```

### Run Locally
```bash
# With environment variables for private repo access
export GIT_USERNAME=your-username
export GIT_TOKEN=ghp_your-token
./mvnw spring-boot:run

# Without credentials (for public repos)
./mvnw spring-boot:run
```

### Test
```bash
./mvnw test                   # Run all tests
# Integration tests are @Disabled by default - requires GitHub credentials
```

### Docker
```bash
docker build -t config-server:latest .
docker run --rm -p 8080:8080 \
  -e GIT_USERNAME=$GIT_USERNAME \
  -e GIT_TOKEN=$GIT_TOKEN \
  config-server:latest
```

## Architecture Overview

### Core Components

1. **ConfigServerApplication** (src/main/java/com/example/configserver/ConfigServerApplication.java:1)
   - Main application class annotated with @EnableConfigServer
   - Entry point for the Spring Boot application

2. **ConfigAsYamlController** (src/main/java/com/example/configserver/web/ConfigAsYamlController.java:1)
   - Custom REST controller providing YAML download endpoint
   - Handles GET requests to `/{service}/{env}`
   - Maps service name to directory structure: `{service}/application-{env}.yaml`
   - Uses EnvironmentRepository to fetch configuration
   - Merges property sources and converts to nested YAML structure
   - Returns file as `application.yaml` download

3. **PropertyTree** (src/main/java/com/example/configserver/util/PropertyTree.java:1)
   - Utility class for converting dot-notation keys to nested Map structure
   - Example: "server.port" → {"server": {"port": value}}
   - Used by ConfigAsYamlController for YAML serialization

### Configuration Flow

1. Request arrives at `GET /config/{service}/{env}?label=main`
2. Controller maps `{service}` to `{service}/application` name
3. EnvironmentRepository.findOne() fetches from Git repo (looks for `{service}/application-{env}.yaml`)
4. Multiple PropertySource objects are merged into flat map
5. PropertyTree converts flat map to nested structure
6. SnakeYAML serializes to YAML string
7. Response returned with `application.yaml` filename

### Git Repository Integration

- Repository: https://github.com/dsrvlabs/platform-config
- Directory structure: Each service has its own directory
  ```
  {service}/
    |- application-{env}.yaml
  ```
  - Example: `wallet-gateway-api/application-dev.yaml`
- Clone on start: enabled
- Force pull: enabled (always gets latest)
- Search path: root directory
- Authentication: via GIT_USERNAME and GIT_TOKEN environment variables

## Key Patterns and Conventions

### Directory Structure Convention
Configuration files must follow the directory structure:
```
{service}/
  |- application-{env}.yaml
```
- service: The service name (e.g., "wallet-gateway-api")
- env: The environment (e.g., "dev", "staging", "prod")

### Endpoint Mapping
Custom endpoint `/{service}/{env}` internally maps to:
- Application name: `{service}/application`
- Profile: `{env}`
- Spring Cloud Config resolves to: `{service}/application-{env}.yaml`

### Environment Variables
- `GIT_USERNAME`: GitHub username (optional, only for private repos)
- `GIT_TOKEN`: GitHub Personal Access Token (optional, only for private repos)
- Both default to empty string if not provided

### API Endpoints

**Custom YAML Download**:
- `GET /config/{service}/{env}?label=main`
- Returns: merged YAML as `application.yaml` file

**Standard Spring Cloud Config**:
- `GET /config/{name}/{profile}` - JSON format with property sources
  - Example: `/config/wallet-gateway-api/application/dev`
- `GET /config/{name}-{profile}.yml` - YAML format
  - Example: `/config/wallet-gateway-api/application-dev.yml`

**Health Check**:
- `GET /config/actuator/health`

## Configuration Structure

The application.yml (src/main/resources/application.yml:1) defines:
- Server runs on port 8080 with context path `/config`
- Git repository settings (URI, branch, credentials)
- Actuator endpoints (health, info)
- Debug logging for Spring Cloud Config

## Testing Strategy

### Unit Tests
- PropertyTreeTest: validates dot-notation to nested map conversion

### Integration Tests
- ConfigServerIT: tests actual Git repository access
- Disabled by default (@Disabled annotation)
- Requires GIT_USERNAME and GIT_TOKEN to be set
- Tests custom endpoint, standard endpoints, and error cases

## Important Notes

- The custom endpoint merges ALL property sources into a single YAML file
- Label parameter allows specifying Git branch/tag/commit SHA
- 404 returned when configuration file doesn't exist
- Logs at DEBUG level show property source processing details
- Force pull ensures latest config is always fetched from Git
