package com.vladte.devhack.infra.ai.util;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

import java.util.*;
import java.util.regex.Pattern;

/**
 * JSON path resolution, key searching, and node nullability checks.
 */
public final class JsonPathUtils {

    private static final Pattern BRACKET_INDEX = Pattern.compile("\\[(\\d+)]");

    private JsonPathUtils() {
    }

    public static boolean isMissingOrNull(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull();
    }

    public static JsonNode resolvePath(JsonNode source, String path) {
        if (source == null || path == null || path.isBlank()) return MissingNode.getInstance();

        try {
            if (path.startsWith("/")) { // JSON Pointer
                JsonPointer ptr = JsonPointer.compile(path);
                JsonNode node = source.at(ptr);
                return node == null ? MissingNode.getInstance() : node;
            }

            // Dot path with optional bracket indexes: a.b[0].c
            JsonNode currentNode = source;
            for (String pathPart : path.split("\\.")) {
                if (isMissingOrNull(currentNode)) return MissingNode.getInstance();

                var matcher = BRACKET_INDEX.matcher(pathPart);
                if (matcher.find()) {
                    String field = pathPart.substring(0, matcher.start());
                    if (!field.isEmpty()) currentNode = currentNode.path(field);
                    matcher.reset();

                    while (matcher.find()) {
                        int idx = Integer.parseInt(matcher.group(1));
                        currentNode = currentNode.path(idx);
                    }
                } else {
                    currentNode = currentNode.path(pathPart);
                }
            }
            return currentNode == null ? MissingNode.getInstance() : currentNode;
        } catch (Exception e) {
            return MissingNode.getInstance();
        }
    }

    public static List<String> buildPathCandidates(String aliasPath, boolean includeTail) {
        if (aliasPath == null || aliasPath.isBlank()) {
            return List.of();
        }
        // JSON Pointer paths are not variant-expanded; return as-is
        if (aliasPath.startsWith("/")) {
            return List.of(aliasPath);
        }
        // For dot paths, expand only the first segment (entity name) into variants
        String[] parts = aliasPath.split("\\.");
        if (parts.length == 0) {
            return List.of(aliasPath);
        }
        String root = parts[0];
        String tail = parts.length > 1 ? String.join(".", Arrays.copyOfRange(parts, 1, parts.length)) : "";
        Set<String> rootVariants = NamingUtils.generateKeyVariants(root);
        List<String> candidates = new ArrayList<>(rootVariants.size());
        for (String rv : rootVariants) {
            candidates.add(tail.isEmpty() ? rv : rv + "." + tail);
        }
        // Also include the original aliasPath as a fallback
        if (!candidates.contains(aliasPath)) {
            candidates.add(aliasPath);
        }
        // Tail-only candidate increases ambiguity; include only if explicitly enabled
        if (includeTail && !tail.isEmpty()) {
            candidates.add(tail);
        }
        return candidates;
    }

    public static JsonNode deepSearchByAnyKey(JsonNode node, Set<String> keys) {
        if (isMissingOrNull(node)) return MissingNode.getInstance();

        if (node.isObject()) {
            Iterator<String> fields = node.fieldNames();
            while (fields.hasNext()) {
                String name = fields.next();
                if (keys.contains(name)) return node.get(name);

                JsonNode nextNode = node.get(name);
                JsonNode found = deepSearchByAnyKey(nextNode, keys);
                if (!isMissingOrNull(found)) return found;
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                JsonNode found = deepSearchByAnyKey(item, keys);
                if (!isMissingOrNull(found)) return found;
            }
        }
        return MissingNode.getInstance();
    }
}
