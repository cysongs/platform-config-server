# Development Guide

This guide helps you set up and work with the Config Server project in IntelliJ IDEA.

## Prerequisites

- **Java 21** (Temurin, Oracle, or any compatible JDK)
- **IntelliJ IDEA** (Community or Ultimate Edition)
- **Maven 3.9+** (included via Maven wrapper)
- **Git** (for cloning the project)

## Project Setup in IntelliJ IDEA

### 1. Open the Project

1. Open IntelliJ IDEA
2. **File → Open**
3. Navigate to the project directory and select the root folder
4. Click **Open**
5. IntelliJ will automatically detect this as a Maven project and import it

### 2. Configure JDK

1. **File → Project Structure → Project**
2. Set **SDK** to Java 21
3. Set **Language level** to 21
4. Click **OK**

### 3. Maven Sync

IntelliJ should automatically sync Maven dependencies. If not:
- Open the **Maven** tool window (View → Tool Windows → Maven)
- Click the **Reload All Maven Projects** button (circular arrows icon)

### 4. Run Configurations

The project includes pre-configured run configurations in the `.run/` folder:

- **ConfigServerApplication** - Run the app with default settings
- **ConfigServerApplication (Local Profile)** - Run with `application-local.yml`
- **All Tests** - Run all unit tests
- **Maven - Clean Install** - Build and install to local Maven repo
- **Maven - Package (Skip Tests)** - Quick build without tests

These will appear in your Run/Debug configurations dropdown automatically.

### 5. Set Environment Variables (Optional)

For private GitHub repositories, configure environment variables:

1. Open **Run → Edit Configurations**
2. Select **ConfigServerApplication**
3. In **Environment variables**, add:
   - `GIT_USERNAME=your-github-username`
   - `GIT_TOKEN=ghp_your_personal_access_token`
4. Click **OK**

## Development Workflow

### Quick Start

```bash
# Run the application
./mvnw spring-boot:run

# Or use the helper script
./scripts/run-local.sh
```

The server will start at `http://localhost:8080/config`

### Building the Project

```bash
# Full build with tests
./mvnw clean install

# Quick build without tests
./mvnw clean package -DskipTests

# Or use the helper script
./scripts/build.sh
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=PropertyTreeTest

# Or use the helper script
./scripts/test.sh
```

### Using IntelliJ Run Configurations

Simply click the **Run** button (green play icon) in the toolbar and select:
- **ConfigServerApplication** to run the app
- **All Tests** to run tests
- Any Maven configuration to build

### Hot Reload (Spring Boot DevTools)

To enable automatic restart on code changes:

1. Add Spring Boot DevTools to `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-devtools</artifactId>
       <scope>runtime</scope>
       <optional>true</optional>
   </dependency>
   ```

2. In IntelliJ:
   - **Preferences → Build, Execution, Deployment → Compiler**
   - Check **Build project automatically**
   - **Preferences → Advanced Settings**
   - Check **Allow auto-make to start even if developed application is currently running**

## Project Structure

```
src/
├── main/
│   ├── java/com/example/configserver/
│   │   ├── ConfigServerApplication.java      # Main Spring Boot application
│   │   ├── web/
│   │   │   └── ConfigAsYamlController.java   # Custom YAML endpoint
│   │   └── util/
│   │       └── PropertyTree.java             # Utility for nested maps
│   └── resources/
│       ├── application.yml                   # Production config
│       └── application-local.yml             # Local development config
└── test/
    └── java/com/example/configserver/
        ├── ConfigServerIT.java               # Integration tests
        └── util/
            └── PropertyTreeTest.java         # Unit tests
```

## Common Tasks

### Add a New Dependency

1. Edit `pom.xml`
2. Add the dependency in the `<dependencies>` section
3. Reload Maven (Maven tool window → Reload button)

### Create a New Controller

1. Create a new class in `src/main/java/com/example/configserver/web/`
2. Annotate with `@RestController`
3. Add request mappings
4. Run the application to test

### Modify Configuration

- **Production**: Edit `src/main/resources/application.yml`
- **Local Development**: Edit `src/main/resources/application-local.yml`
- Run with local profile: `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`

### Debug the Application

1. Click the **Debug** button (bug icon) instead of Run
2. Set breakpoints by clicking in the left gutter next to line numbers
3. Use the Debug tool window to step through code

### View Application Logs

Logs appear in the **Run** tool window when using IntelliJ run configurations.

For external runs:
```bash
./mvnw spring-boot:run > app.log 2>&1
tail -f app.log
```

## Testing Endpoints

### Using curl

```bash
# Custom YAML endpoint
curl -L "http://localhost:8080/config/wallet-gateway-api/dev"

# Standard JSON endpoint
curl "http://localhost:8080/config/wallet-gateway-api-application/dev"

# Health check
curl "http://localhost:8080/config/actuator/health"
```

### Using IntelliJ HTTP Client

Create a file `requests.http`:

```http
### Get config as YAML
GET http://localhost:8080/config/wallet-gateway-api/dev

### Get config as JSON
GET http://localhost:8080/config/wallet-gateway-api-application/dev

### Health check
GET http://localhost:8080/config/actuator/health
```

Click the green play icon next to each request to execute.

## Docker Development

### Build Image

```bash
./scripts/docker-build.sh config-server latest
```

### Run Container

```bash
export GIT_USERNAME=your-username
export GIT_TOKEN=ghp_your-token
./scripts/docker-run.sh config-server latest
```

### Debug Docker Build

```bash
# Build with verbose output
docker build --progress=plain -t config-server:debug .

# Run with shell access
docker run -it --rm config-server:debug /bin/bash
```

## Troubleshooting

### Maven Dependencies Not Resolving

1. **Maven → Reload All Maven Projects**
2. If still failing: **File → Invalidate Caches → Invalidate and Restart**

### Port 8080 Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change port in application.yml
server:
  port: 8081
```

### Git Repository Clone Fails

- Verify Git credentials (GIT_USERNAME, GIT_TOKEN)
- Check repository URL in `application.yml`
- Check network/firewall settings
- Review logs: look for `org.eclipse.jgit` errors

### Tests Failing

```bash
# Clean and rebuild
./mvnw clean test

# Run specific test with verbose output
./mvnw test -Dtest=PropertyTreeTest -X
```

### IntelliJ Not Recognizing Java 21

1. **File → Project Structure → SDKs**
2. Add Java 21 SDK if not present
3. **File → Project Structure → Project**
4. Select Java 21 as SDK

## Code Style

The project follows standard Java conventions:
- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters
- **Imports**: Organize imports (Ctrl+Alt+O on Windows/Linux, Cmd+Opt+O on Mac)

IntelliJ will automatically format code:
- **Code → Reformat Code** (Ctrl+Alt+L / Cmd+Opt+L)

## Useful Maven Commands

```bash
# Compile only
./mvnw compile

# Run specific test
./mvnw test -Dtest=ConfigServerIT

# Package without running tests
./mvnw package -DskipTests

# Clean build artifacts
./mvnw clean

# Display dependency tree
./mvnw dependency:tree

# Check for dependency updates
./mvnw versions:display-dependency-updates
```

## Git Workflow

```bash
# Create feature branch
git checkout -b feature/my-feature

# Make changes and commit
git add .
git commit -m "Add new feature"

# Push to remote
git push origin feature/my-feature

# Create pull request (use GitHub web interface)
```

## Getting Help

- **Spring Cloud Config Docs**: https://docs.spring.io/spring-cloud-config/docs/current/reference/html/
- **Spring Boot Docs**: https://docs.spring.io/spring-boot/docs/current/reference/html/
- **Project README**: [README.md](README.md)
- **Architecture Guide**: [CLAUDE.md](CLAUDE.md)
