package com.example.configserver.web;

import com.example.configserver.util.PropertyTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom controller that returns merged configuration as a YAML file named application.yaml.
 * Maps the request path /{service}/{env} to the directory structure: {service}/application-{env}.yaml
 * 
 * Repository structure:
 *   {service}/
 *     |- application-{env}.yaml
 *     |- application-{env}.yaml
 */
@RestController
@RequestMapping
public class ConfigAsYamlController {

    private static final Logger log = LoggerFactory.getLogger(ConfigAsYamlController.class);

    private final EnvironmentRepository environmentRepository;
    private final Yaml yaml;

    public ConfigAsYamlController(EnvironmentRepository environmentRepository) {
        this.environmentRepository = environmentRepository;

        // Configure YAML dumper options for better output formatting
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        this.yaml = new Yaml(options);
    }

    /**
     * Custom endpoint that returns merged configuration as application.yaml file.
     *
     * @param service the service name (e.g., "wallet-gateway-api")
     * @param env     the environment (e.g., "dev", "prod")
     * @param label   the git branch/tag/commit (default: "main")
     * @return YAML content as application.yaml file
     */
    @GetMapping(value = "/{service}/{env}", produces = "application/x-yaml")
    public ResponseEntity<String> configAsYaml(
            @PathVariable String service,
            @PathVariable String env,
            @RequestParam(value = "label", required = false, defaultValue = "main") String label) {

        log.info("Fetching config for service={}, env={}, label={}", service, env, label);

        // Map request to directory structure: {service}/application-{env}.yaml
        // Spring Cloud Config resolves files in the following order:
        // 1. {application}-{profile}.yaml (in root or search-paths)
        // 2. {application}/{profile}.yaml (directory structure)
        // 
        // Since our files are in {service}/application-{env}.yaml format,
        // we need to use:
        // - application name = "{service}" (the service directory)
        // - profile = "application-{env}" (the file name without extension)
        // This will make Spring Cloud Config look for: {service}/application-{env}.yaml
        String applicationName = service;
        String profile = "application-" + env;
        Environment envObj;
        try {
            envObj = environmentRepository.findOne(applicationName, profile, label);
        } catch (Exception e) {
            log.error("Error fetching configuration for {}/application-{} (label={})", service, env, label, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(String.format("{\"error\": \"Failed to fetch configuration: %s\"}", e.getMessage()));
        }

        // Check if configuration exists
        if (envObj == null || envObj.getPropertySources() == null || envObj.getPropertySources().isEmpty()) {
            log.warn("Configuration not found for {}/application-{} (label={})", service, env, label);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(String.format("{\"error\": \"Configuration not found for %s/application-%s.yaml (label=%s)\"}",
                            service, env, label));
        }

        log.debug("Found {} property sources for {}/application-{}", envObj.getPropertySources().size(), service, env);

        // Get the first property source (which contains the actual YAML file content)
        PropertySource firstSource = envObj.getPropertySources().get(0);
        log.debug("Using property source: {}", firstSource.getName());
        
        if (!(firstSource.getSource() instanceof Map<?, ?>)) {
            log.error("Property source is not a Map: {}", firstSource.getSource().getClass());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"Invalid property source format\"}");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> sourceMap = (Map<String, Object>) firstSource.getSource();
        log.debug("Source map contains {} properties", sourceMap.size());

        // Convert flat map to nested structure
        Map<String, Object> tree = new LinkedHashMap<>();
        sourceMap.forEach((k, v) -> {
            // Handle null values
            Object value = v == null ? "" : v;
            PropertyTree.put(tree, k, value);
        });

        // Serialize to YAML
        String body = yaml.dump(tree);

        log.info("Successfully generated YAML configuration ({} bytes) for {}/application-{}", body.length(), service, env);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"application.yaml\"")
                .contentType(MediaType.parseMediaType("application/x-yaml"))
                .body(body);
    }
}
