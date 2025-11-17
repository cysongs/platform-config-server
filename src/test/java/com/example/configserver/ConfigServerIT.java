package com.example.configserver;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Config Server.
 *
 * These tests require actual GitHub access and are disabled by default.
 * To run these tests locally:
 * 1. Set environment variables: GIT_USERNAME and GIT_TOKEN
 * 2. Remove @Disabled annotation or run with -Dtest=ConfigServerIT
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigServerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Disabled("Requires GitHub access token - enable manually for local testing")
    void testCustomEndpoint_success() {
        // Test custom endpoint: GET /config/{service}/{env}
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/config/wallet-gateway-api/dev",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString()).contains("application/x-yaml");
        assertThat(response.getBody()).isNotNull();

        // Verify it's valid YAML content
        String body = response.getBody();
        assertThat(body).isNotEmpty();

        // Check for common YAML structure (this depends on your actual config file)
        // Adjust these assertions based on your actual configuration
        assertThat(body).contains(":");
    }

    @Test
    @Disabled("Requires GitHub access token - enable manually for local testing")
    void testCustomEndpoint_notFound() {
        // Test with non-existent service
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/config/non-existent-service/dev",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("error");
        assertThat(response.getBody()).contains("not found");
    }

    @Test
    @Disabled("Requires GitHub access token - enable manually for local testing")
    void testCustomEndpoint_withLabel() {
        // Test with custom label (branch/tag)
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/config/wallet-gateway-api/dev?label=main",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @Disabled("Requires GitHub access token - enable manually for local testing")
    void testStandardEndpoint_json() {
        // Test standard Spring Cloud Config endpoint: /{name}/{profile}
        // New structure: {service}/application maps to {service}/application-{env}.yaml
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/config/wallet-gateway-api/application/dev",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("\"name\"");
        assertThat(response.getBody()).contains("\"profiles\"");
        assertThat(response.getBody()).contains("\"propertySources\"");
    }

    @Test
    @Disabled("Requires GitHub access token - enable manually for local testing")
    void testStandardEndpoint_yaml() {
        // Test standard YAML endpoint: /{name}-{profile}.yml
        // New structure: {service}/application-{env}.yml
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/config/wallet-gateway-api/application-dev.yml",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains(":");
    }

    @Test
    void testActuatorHealth() {
        // Test actuator health endpoint (should work without GitHub access)
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/config/actuator/health",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("status");
    }
}
