package com.vladte.devhack.common.engine.ai.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * Value coercion and JSON->Java conversion helpers.
 */
public final class JsonNodeValueConverter {

    private JsonNodeValueConverter() {
    }

    public static Object toJava(ObjectMapper objectMapper, JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return null;
        if (node.isTextual()) return node.asText();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isNumber()) return node.numberValue();
        if (node.isArray())
            return objectMapper.convertValue(node, new TypeReference<List<Object>>() {
            });
        if (node.isObject()) return objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {
        });
        return node.toString();
    }

    public static Object coerce(ObjectMapper objectMapper, JsonNode propSchema, JsonNode valueNode) {
        String type = SchemaUtils.readType(propSchema);
        if (type == null) return toJava(objectMapper, valueNode);

        // Validate shape for primitive types
        if (("string".equals(type) || "integer".equals(type) || "number".equals(type) || "boolean".equals(type))
                && valueNode.isContainerNode()) {
            throw new IllegalArgumentException("Expected primitive type '" + type + "' but got container value: " + valueNode);
        }

        try {
            return switch (type) {
                case "string" -> objectMapper.convertValue(valueNode, String.class);
                case "integer" -> objectMapper.convertValue(valueNode, Long.class);   // safer than Integer
                case "number" -> objectMapper.convertValue(valueNode, java.math.BigDecimal.class);
                case "boolean" -> objectMapper.convertValue(valueNode, Boolean.class);
                case "array" -> objectMapper.convertValue(valueNode, new TypeReference<List<Object>>() {
                });
                case "object" -> objectMapper.convertValue(valueNode, new TypeReference<Map<String, Object>>() {
                });
                default -> toJava(objectMapper, valueNode);
            };
        } catch (IllegalArgumentException e) {
            return toJava(objectMapper, valueNode);
        }
    }
}
