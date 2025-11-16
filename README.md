# Platform Config Server

Spring Boot 3.x + Spring Cloud Config Server for centralized configuration management.

## Documentation

- **[QUICKSTART.md](QUICKSTART.md)** - Get started in 5 minutes
- **[DEVELOPMENT.md](DEVELOPMENT.md)** - Complete development guide for IntelliJ IDEA
- **[CLAUDE.md](CLAUDE.md)** - Architecture and technical details
- **[README.md](README.md)** - This file (comprehensive reference)

## Features

- **Spring Cloud Config Server** with Git backend
- **Custom YAML endpoint** that returns merged configuration as `application.yaml`
- **GitHub authentication** support via environment variables
- **Standard Config Server endpoints** (JSON and YAML)
- **Health checks** via Spring Actuator
- **Docker support** with multi-stage build

## Configuration Repository

- Git Repository: `https://github.com/cysongs/platform-config`
- File naming convention: `{service}-application-{env}.yaml`
  - Example: `wallet-gateway-api-application-dev.yaml`

## Prerequisites

- Java 21
- Maven 3.9+ (or use included Maven wrapper)
- Docker (optional, for containerized deployment)

## Getting Started

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/cysongs/platform-config-server.git
   cd platform-config-server
   ```

2. **Set environment variables** (optional, for private repositories)
   ```bash
   export GIT_USERNAME=your-github-username
   export GIT_TOKEN=ghp_your_personal_access_token
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

   The server will start on `http://localhost:8080/config`

### Docker Deployment

1. **Build Docker image**
   ```bash
   docker build -t config-server:latest .
   ```

2. **Run container**
   ```bash
   docker run --rm -p 8080:8080 \
     -e GIT_USERNAME=your-github-username \
     -e GIT_TOKEN=ghp_your_token \
     config-server:latest
   ```

## API Endpoints

### Custom YAML Endpoint

Returns merged configuration as a downloadable `application.yaml` file.

```bash
# Download configuration for wallet-gateway-api service in dev environment
curl -L -o application.yaml \
  "http://localhost:8080/config/wallet-gateway-api/dev"

# With custom git label (branch/tag/commit)
curl -L -o application.yaml \
  "http://localhost:8080/config/wallet-gateway-api/dev?label=main"
```

**Mapping**: Request path `/{service}/{env}` maps to file `{service}-application-{env}.yaml`

### Standard Spring Cloud Config Endpoints

#### JSON Format
```bash
# Returns full environment with property sources
curl "http://localhost:8080/config/wallet-gateway-api-application/dev"
```

#### YAML Format
```bash
# Returns configuration in YAML format
curl "http://localhost:8080/config/wallet-gateway-api-application-dev.yml"
```

### Health Check

```bash
curl "http://localhost:8080/config/actuator/health"
```

## Testing

### Run all tests
```bash
./mvnw test
```

### Run integration tests

Integration tests are disabled by default as they require GitHub access. To run them:

1. Set environment variables:
   ```bash
   export GIT_USERNAME=your-username
   export GIT_TOKEN=your-token
   ```

2. Enable tests by removing `@Disabled` annotation in `ConfigServerIT.java`

3. Run tests:
   ```bash
   ./mvnw test
   ```

## Configuration

Configuration is managed in `src/main/resources/application.yml`:

- **Server Port**: 8080
- **Context Path**: `/config`
- **Git URI**: `https://github.com/cysongs/platform-config`
- **Default Branch**: `main`
- **Clone on Start**: `true` (downloads repo on startup)
- **Force Pull**: `true` (always pulls latest changes)

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `GIT_USERNAME` | GitHub username | No (only for private repos) |
| `GIT_TOKEN` | GitHub Personal Access Token | No (only for private repos) |

### Creating a GitHub Personal Access Token

For private repositories, create a Personal Access Token:

1. Go to GitHub Settings → Developer settings → Personal access tokens
2. Generate new token (classic)
3. Select scopes: `repo` (Full control of private repositories)
4. Copy the token and use it as `GIT_TOKEN`

## Project Structure

```
config-server/
├── src/
│   ├── main/
│   │   ├── java/com/example/configserver/
│   │   │   ├── ConfigServerApplication.java      # Main application class
│   │   │   ├── web/
│   │   │   │   └── ConfigAsYamlController.java   # Custom YAML endpoint
│   │   │   └── util/
│   │   │       └── PropertyTree.java             # Utility for nested maps
│   │   └── resources/
│   │       └── application.yml                   # Application configuration
│   └── test/
│       └── java/com/example/configserver/
│           ├── ConfigServerIT.java               # Integration tests
│           └── util/
│               └── PropertyTreeTest.java         # Unit tests
├── Dockerfile                                    # Multi-stage Docker build
├── pom.xml                                       # Maven configuration
└── README.md                                     # This file
```

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.3.5
- **Spring Cloud**: 2024.0.0
- **Build Tool**: Maven
- **Container**: Docker with Eclipse Temurin

## Troubleshooting

### Configuration not found (404)

- Verify the configuration file exists in the Git repository
- Check file naming: `{service}-application-{env}.yaml`
- Ensure the repository is accessible (check credentials for private repos)
- Check logs for Git clone/pull errors

### Git authentication errors

- Verify `GIT_USERNAME` and `GIT_TOKEN` are set correctly
- Ensure the token has `repo` scope
- For public repositories, credentials are not required

### Application fails to start

- Check if port 8080 is already in use
- Verify Git repository URL is accessible
- Check application logs for detailed error messages

## Development

### Build the project
```bash
./mvnw clean package
```

### Run tests
```bash
./mvnw test
```

### Skip tests during build
```bash
./mvnw package -DskipTests
```

## License

This project is for internal use.
