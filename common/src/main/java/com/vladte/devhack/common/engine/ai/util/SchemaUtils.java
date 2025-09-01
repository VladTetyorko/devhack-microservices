package com.vladte.devhack.common.engine.ai.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SchemaUtils {
    public static final String FIELD_TYPE = "type";
    public static final String EXT_X_DEEP = "x-deepSearch";

    private SchemaUtils() {
    }

    public static String readType(JsonNode schema) {
        JsonNode t = schema.get(FIELD_TYPE);
        return t != null && t.isTextual() ? t.asText() : null;
    }

    public static boolean readDeepSearch(JsonNode propSchema, boolean globalDefault) {
        JsonNode n = propSchema.get(EXT_X_DEEP);
        return n != null && n.isBoolean() ? n.asBoolean() : globalDefault;
    }

    public static Map<String, Object> mergeSchemaDefaults(JsonNode props, Map<String, Object> defaults, Map<String, Object> values) {
        if (defaults == null || defaults.isEmpty()) {
            return new LinkedHashMap<>(values);
        }
        Map<String, Object> merged = new LinkedHashMap<>(values);
        if (props != null && props.isObject()) {
            Iterator<String> it = props.fieldNames();
            while (it.hasNext()) {
                String name = it.next();
                Object defVal = defaults.get(name);
                if (defVal != null && !merged.containsKey(name)) {
                    merged.put(name, defVal);
                }
            }
        }
        return merged;
    }

    public static Map<String, Object> mergeDefaults(Map<String, Object> defaults, Map<String, Object> values) {
        if (defaults == null || defaults.isEmpty()) return new LinkedHashMap<>(values);
        Map<String, Object> merged = new LinkedHashMap<>(defaults);
        merged.putAll(values);
        return merged;
    }
}
