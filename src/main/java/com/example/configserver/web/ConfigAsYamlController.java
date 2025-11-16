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
 * Maps the request path /{service}/{env} to the file naming convention: {service}-application-{env}.yaml
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

        // Map request to file naming convention: {service}-application-{env}.yaml
        String application = service + "-application";

        // Fetch configuration from repository
        Environment envObj;
        try {
            envObj = environmentRepository.findOne(application, env, label);
        } catch (Exception e) {
            log.error("Error fetching configuration for {}-{} (label={})", application, env, label, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(String.format("{\"error\": \"Failed to fetch configuration: %s\"}", e.getMessage()));
        }

        // Check if configuration exists
        if (envObj == null || envObj.getPropertySources() == null || envObj.getPropertySources().isEmpty()) {
            log.warn("Configuration not found for {}-{} (label={})", application, env, label);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(String.format("{\"error\": \"Configuration not found for %s-%s.yaml (label=%s)\"}",
                            application, env, label));
        }

        log.debug("Found {} property sources for {}-{}", envObj.getPropertySources().size(), application, env);

        // Merge all property sources into a flat map
        Map<String, Object> merged = new LinkedHashMap<>();
        for (PropertySource ps : envObj.getPropertySources()) {
            log.debug("Processing property source: {}", ps.getName());
            if (ps.getSource() instanceof Map<?, ?> map) {
                map.forEach((k, v) -> {
                    String key = String.valueOf(k);
                    Object value = v;
                    // Handle null values
                    if (value == null) {
                        value = "";
                    }
                    merged.put(key, value);
                });
            }
        }

        log.debug("Merged {} properties", merged.size());

        // Convert flat map to nested structure
        Map<String, Object> tree = new LinkedHashMap<>();
        merged.forEach((k, v) -> PropertyTree.put(tree, k, v));

        // Serialize to YAML
        String body = yaml.dump(tree);

        log.info("Successfully generated YAML configuration ({} bytes) for {}-{}", body.length(), application, env);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"application.yaml\"")
                .contentType(MediaType.parseMediaType("application/x-yaml"))
                .body(body);
    }
}
