# Quick Start Guide

Get up and running with Config Server in 5 minutes!

## Prerequisites

✓ Java 21
✓ Git

## Option 1: Run with Maven (Recommended for Development)

```bash
# 1. Clone the repository
git clone https://github.com/cysongs/platform-config-server.git
cd platform-config-server

# 2. Set credentials (optional - only for private repos)
export GIT_USERNAME=your-username
export GIT_TOKEN=ghp_your-token

# 3. Run the application
./mvnw spring-boot:run

# Or use the helper script
./scripts/run-local.sh
```

**That's it!** The server is running at http://localhost:8080/config

## Option 2: Run with Docker

```bash
# 1. Build the image
docker build -t config-server:latest .

# 2. Run the container
docker run --rm -p 8080:8080 \
  -e GIT_USERNAME=your-username \
  -e GIT_TOKEN=ghp_your-token \
  config-server:latest

# Or use helper scripts
./scripts/docker-build.sh
./scripts/docker-run.sh
```

## Option 3: IntelliJ IDEA

1. **Open the project** in IntelliJ IDEA
2. Wait for Maven import to complete
3. Click the **Run** button and select **ConfigServerApplication**
4. Edit configuration to add environment variables if needed

## Test the Endpoints

```bash
# Custom YAML endpoint (downloads as application.yaml)
curl -L -o application.yaml \
  "http://localhost:8080/config/wallet-gateway-api/dev"

# Standard JSON endpoint
curl "http://localhost:8080/config/wallet-gateway-api-application/dev"

# Health check
curl "http://localhost:8080/config/actuator/health"
```

## Expected Response

If everything works, you should see:
- Server starts on port 8080
- Git repository cloned at startup
- Health endpoint returns `{"status":"UP"}`

## Troubleshooting

**Port already in use?**
```bash
# Change port in application.yml or use environment variable
SERVER_PORT=8081 ./mvnw spring-boot:run
```

**Git clone fails?**
- For private repos: Make sure GIT_USERNAME and GIT_TOKEN are set
- For public repos: No credentials needed
- Check repository URL in `src/main/resources/application.yml`

**Configuration file not found (404)?**
- Verify the config file exists in the GitHub repo
- File should be named: `{service}-application-{env}.yaml`
- Example: `wallet-gateway-api-application-dev.yaml`
- Check server logs for Git errors

## Next Steps

- **Development Guide**: See [DEVELOPMENT.md](DEVELOPMENT.md) for detailed setup
- **Full Documentation**: See [README.md](README.md)
- **Architecture**: See [CLAUDE.md](CLAUDE.md)

## Helper Scripts

The `scripts/` directory contains useful development scripts:

- `build.sh` - Quick build
- `run-local.sh` - Run locally with checks
- `test.sh` - Run tests
- `docker-build.sh` - Build Docker image
- `docker-run.sh` - Run Docker container

Make them executable: `chmod +x scripts/*.sh`

## Common Commands

```bash
# Build without tests
./mvnw clean package -DskipTests

# Run tests
./mvnw test

# Full build with tests
./mvnw clean install

# Run with debug logging
./mvnw spring-boot:run -Dlogging.level.org.springframework.cloud.config=DEBUG
```

## Configuration Repository Setup

Your config repository should contain files like:

```
platform-config/
├── wallet-gateway-api-application-dev.yaml
├── wallet-gateway-api-application-staging.yaml
├── wallet-gateway-api-application-prod.yaml
└── other-service-application-dev.yaml
```

Each file contains YAML configuration for a specific service and environment.
