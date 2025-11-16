package com.example.configserver.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PropertyTree utility.
 */
class PropertyTreeTest {

    @Test
    void testPut_simpleKey() {
        Map<String, Object> tree = new LinkedHashMap<>();
        PropertyTree.put(tree, "key", "value");

        assertThat(tree).containsEntry("key", "value");
    }

    @Test
    void testPut_nestedKey() {
        Map<String, Object> tree = new LinkedHashMap<>();
        PropertyTree.put(tree, "server.port", "8080");

        assertThat(tree).containsKey("server");
        @SuppressWarnings("unchecked")
        Map<String, Object> server = (Map<String, Object>) tree.get("server");
        assertThat(server).containsEntry("port", "8080");
    }

    @Test
    void testPut_deeplyNestedKey() {
        Map<String, Object> tree = new LinkedHashMap<>();
        PropertyTree.put(tree, "spring.cloud.config.server.git.uri", "https://github.com/example/repo");

        assertThat(tree).containsKey("spring");
        @SuppressWarnings("unchecked")
        Map<String, Object> spring = (Map<String, Object>) tree.get("spring");
        assertThat(spring).containsKey("cloud");

        @SuppressWarnings("unchecked")
        Map<String, Object> cloud = (Map<String, Object>) spring.get("cloud");
        assertThat(cloud).containsKey("config");

        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) cloud.get("config");
        assertThat(config).containsKey("server");

        @SuppressWarnings("unchecked")
        Map<String, Object> server = (Map<String, Object>) config.get("server");
        assertThat(server).containsKey("git");

        @SuppressWarnings("unchecked")
        Map<String, Object> git = (Map<String, Object>) server.get("git");
        assertThat(git).containsEntry("uri", "https://github.com/example/repo");
    }

    @Test
    void testPut_multipleKeysInSameNamespace() {
        Map<String, Object> tree = new LinkedHashMap<>();
        PropertyTree.put(tree, "server.port", "8080");
        PropertyTree.put(tree, "server.address", "localhost");

        assertThat(tree).containsKey("server");
        @SuppressWarnings("unchecked")
        Map<String, Object> server = (Map<String, Object>) tree.get("server");
        assertThat(server).containsEntry("port", "8080");
        assertThat(server).containsEntry("address", "localhost");
    }

    @Test
    void testToNestedMap() {
        Map<String, Object> flatMap = new LinkedHashMap<>();
        flatMap.put("server.port", "8080");
        flatMap.put("server.address", "localhost");
        flatMap.put("spring.application.name", "config-server");

        Map<String, Object> nested = PropertyTree.toNestedMap(flatMap);

        assertThat(nested).containsKey("server");
        assertThat(nested).containsKey("spring");

        @SuppressWarnings("unchecked")
        Map<String, Object> server = (Map<String, Object>) nested.get("server");
        assertThat(server).containsEntry("port", "8080");
        assertThat(server).containsEntry("address", "localhost");

        @SuppressWarnings("unchecked")
        Map<String, Object> spring = (Map<String, Object>) nested.get("spring");
        @SuppressWarnings("unchecked")
        Map<String, Object> application = (Map<String, Object>) spring.get("application");
        assertThat(application).containsEntry("name", "config-server");
    }

    @Test
    void testPut_nullOrEmptyKey() {
        Map<String, Object> tree = new LinkedHashMap<>();
        PropertyTree.put(tree, null, "value");
        PropertyTree.put(tree, "", "value");

        assertThat(tree).isEmpty();
    }
}
