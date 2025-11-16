package com.example.configserver.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class to convert dot-notation property keys into nested Map structure.
 * For example: "server.port" -> {"server": {"port": value}}
 */
public class PropertyTree {

    private PropertyTree() {
        // Utility class, prevent instantiation
    }

    /**
     * Puts a property with dot-notation key into a nested Map structure.
     *
     * @param tree  the root map to build the tree in
     * @param key   the property key in dot notation (e.g., "a.b.c")
     * @param value the value to set
     */
    @SuppressWarnings("unchecked")
    public static void put(Map<String, Object> tree, String key, Object value) {
        if (key == null || key.isEmpty()) {
            return;
        }

        String[] parts = key.split("\\.");
        Map<String, Object> current = tree;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            Object existing = current.get(part);

            if (existing instanceof Map) {
                current = (Map<String, Object>) existing;
            } else {
                // Create a new nested map
                Map<String, Object> newMap = new LinkedHashMap<>();
                current.put(part, newMap);
                current = newMap;
            }
        }

        // Set the final value
        String lastPart = parts[parts.length - 1];
        current.put(lastPart, value);
    }

    /**
     * Converts a flat Properties-style map to a nested Map structure.
     *
     * @param flatMap map with dot-notation keys
     * @return nested map structure
     */
    public static Map<String, Object> toNestedMap(Map<String, Object> flatMap) {
        Map<String, Object> result = new LinkedHashMap<>();
        flatMap.forEach((key, value) -> put(result, key, value));
        return result;
    }
}
