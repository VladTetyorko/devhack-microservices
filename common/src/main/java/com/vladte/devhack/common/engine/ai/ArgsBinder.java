package com.vladte.devhack.common.engine.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.engine.ai.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class ArgsBinder {

    private static final Logger log = LoggerFactory.getLogger(ArgsBinder.class);

    private static final String FIELD_PROPERTIES = "properties";
    private static final String FIELD_DEFAULT = "default";
    private static final String EXT_X_PATH = "x-path";
    private static final String EXT_X_ALIASES = "x-aliases";
    private static final String EXT_X_ALIAS_PATH = "x-aliasPath";
    private static final String EXT_X_ALLOW_TAIL = "x-allowTailAlias";

    private final ObjectMapper objectMapper;

    public ArgsBinder(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        log.debug("ArgsBinder initialized");
    }

    public Map<String, Object> bind(Object argsSchema, Map<String, Object> defaults, Object... sources) {
        return bind(argsSchema, defaults, false, sources);
    }

    public Map<String, Object> bind(Object argsSchema,
                                    Map<String, Object> defaults,
                                    boolean deepSearch,
                                    Object... sources) {
        long started = System.nanoTime();
        JsonNode schema = objectMapper.valueToTree(argsSchema == null ? Map.of() : argsSchema);
        JsonNode props = schema.path(FIELD_PROPERTIES);

        log.info("Binding arguments (deepSearch={}, sources={}, defaultsKeys={})",
                deepSearch, sources != null ? sources.length : 0, defaults != null ? defaults.keySet() : Set.of());

        if (props.isMissingNode() || !props.isObject()) {
            log.debug("No schema 'properties' found, returning merged defaults only");
            return SchemaUtils.mergeDefaults(defaults, Map.of());
        }

        List<JsonNode> sourceNodes = SourceNormalizationUtils.normalizeSources(objectMapper, sources);
        log.debug("Normalized {} source node(s) for binding", sourceNodes.size());

        Map<String, Object> out = new LinkedHashMap<>();
        bindObjectProperties(out, props, defaults, deepSearch, sourceNodes);

        Map<String, Object> merged = SchemaUtils.mergeSchemaDefaults(props, defaults, out);
        merged.values().removeIf(Objects::isNull);

        long elapsedMs = (System.nanoTime() - started) / 1_000_000;
        log.info("Binding complete: {} key(s), elapsed={}ms, keys={}", merged.size(), elapsedMs, merged.keySet());
        return merged;
    }

    private void bindObjectProperties(Map<String, Object> out,
                                      JsonNode propertiesSchema,
                                      Map<String, Object> defaults,
                                      boolean deepSearch,
                                      List<JsonNode> sources) {
        Iterator<String> names = propertiesSchema.fieldNames();
        while (names.hasNext()) {
            String propName = names.next();
            JsonNode propSchema = propertiesSchema.get(propName);
            String type = SchemaUtils.readType(propSchema);
            boolean effectiveDeep = SchemaUtils.readDeepSearch(propSchema, deepSearch);

            log.debug("Resolve property='{}' (type={}, deepSearch={})", propName, type, effectiveDeep);

            if ("object".equals(type) && propSchema.has(FIELD_PROPERTIES)) {
                Map<String, Object> nested = new LinkedHashMap<>();

                if (propSchema.has(FIELD_DEFAULT) && propSchema.get(FIELD_DEFAULT).isObject()) {
                    nested.putAll(objectMapper.convertValue(
                            propSchema.get(FIELD_DEFAULT),
                            new TypeReference<Map<String, Object>>() {
                            }
                    ));
                    log.debug("Applied object default for '{}': keys={}", propName, nested.keySet());
                }

                bindObjectProperties(nested, propSchema.get(FIELD_PROPERTIES), defaults, effectiveDeep, sources);

                if (!nested.isEmpty()) {
                    out.put(propName, nested);
                    log.debug("Bound nested object for '{}': keys={}", propName, nested.keySet());
                } else if (propSchema.has(FIELD_DEFAULT)) {
                    Object def = JsonNodeValueConverter.coerce(objectMapper, propSchema, propSchema.get(FIELD_DEFAULT));
                    out.put(propName, def);
                    log.debug("Used schema default for empty nested '{}'", propName);
                }
                continue;
            }

            Optional<JsonNode> located = resolvePropertyValueNode(propSchema, propName, sources, effectiveDeep);
            Object value = chooseEffectiveValue(propName, propSchema, located, defaults);
            if (value != null) {
                out.put(propName, value);
                log.debug("Property '{}' resolved via {}", propName, resolutionSource(propSchema, located, defaults, propName));
            } else {
                log.debug("Property '{}' unresolved; no value/default", propName);
            }
        }
    }

    private Optional<JsonNode> resolvePropertyValueNode(JsonNode propSchema,
                                                        String propName,
                                                        List<JsonNode> sources,
                                                        boolean deepSearch) {
        Optional<JsonNode> byPath = resolveViaExplicitPath(propSchema, sources);
        if (byPath.isPresent()) return byPath;

        Optional<JsonNode> byAliases = resolveViaAliases(propSchema, propName, sources, deepSearch);
        if (byAliases.isPresent()) return byAliases;

        return resolveViaKeyVariants(propName, sources, deepSearch);
    }

    private Optional<JsonNode> resolveViaExplicitPath(JsonNode propSchema, List<JsonNode> sources) {
        JsonNode xPath = propSchema.get(EXT_X_PATH);
        if (xPath == null || !xPath.isTextual()) return Optional.empty();

        String path = xPath.asText();
        for (JsonNode src : sources) {
            JsonNode node = JsonPathUtils.resolvePath(src, path);
            if (!JsonPathUtils.isMissingOrNull(node)) {
                log.debug("Matched by x-path='{}'", path);
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    private Optional<JsonNode> resolveViaAliases(JsonNode propSchema,
                                                 String propName,
                                                 List<JsonNode> sources,
                                                 boolean deepSearch) {
        Set<String> keys = collectKeyCandidates(propSchema, propName);

        for (String alias : keys) {
            boolean aliasPathFlag = propSchema.has(EXT_X_ALIAS_PATH) && propSchema.get(EXT_X_ALIAS_PATH).asBoolean(false);
            boolean looksLikePath = alias != null && !alias.isBlank()
                    && (alias.startsWith("/") || alias.contains(".") || alias.contains("["));
            if (looksLikePath || aliasPathFlag) {
                boolean extAllowTail = propSchema.has(EXT_X_ALLOW_TAIL) && propSchema.get(EXT_X_ALLOW_TAIL).asBoolean(false);
                boolean allowTail = aliasPathFlag || extAllowTail;
                List<String> candidates = JsonPathUtils.buildPathCandidates(alias, allowTail);
                for (JsonNode source : sources) {
                    for (String candidate : candidates) {
                        JsonNode valueNode = JsonPathUtils.resolvePath(source, candidate);
                        if (!JsonPathUtils.isMissingOrNull(valueNode)) {
                            log.debug("Matched by alias-path='{}' (candidate='{}')", alias, candidate);
                            return Optional.of(valueNode);
                        }
                    }
                }
            }

            Set<String> variants = NamingUtils.generateKeyVariants(alias);
            for (JsonNode source : sources) {
                for (String key : variants) {
                    JsonNode valueNode = source.get(key);
                    if (!JsonPathUtils.isMissingOrNull(valueNode)) {
                        log.debug("Matched by alias key='{}'", key);
                        return Optional.of(valueNode);
                    }
                }
                if (deepSearch) {
                    JsonNode foundValue = JsonPathUtils.deepSearchByAnyKey(source, variants);
                    if (!JsonPathUtils.isMissingOrNull(foundValue)) {
                        log.debug("Matched by deep-search alias variants={}", variants);
                        return Optional.of(foundValue);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<JsonNode> resolveViaKeyVariants(String propName,
                                                     List<JsonNode> sources,
                                                     boolean deepSearch) {
        Set<String> variants = NamingUtils.generateKeyVariants(propName);
        for (JsonNode source : sources) {
            for (String key : variants) {
                JsonNode valueNode = source.get(key);
                if (!JsonPathUtils.isMissingOrNull(valueNode)) {
                    log.debug("Matched by key variant='{}'", key);
                    return Optional.of(valueNode);
                }
            }
            if (deepSearch) {
                JsonNode foundValue = JsonPathUtils.deepSearchByAnyKey(source, variants);
                if (!JsonPathUtils.isMissingOrNull(foundValue)) {
                    log.debug("Matched by deep-search key variants={}", variants);
                    return Optional.of(foundValue);
                }
            }
        }
        return Optional.empty();
    }

    private Object chooseEffectiveValue(String propName,
                                        JsonNode propSchema,
                                        Optional<JsonNode> candidate,
                                        Map<String, Object> defaults) {
        if (candidate.isPresent() && !JsonPathUtils.isMissingOrNull(candidate.get())) {
            return JsonNodeValueConverter.coerce(objectMapper, propSchema, candidate.get());
        }
        if (defaults != null && defaults.containsKey(propName)) {
            return defaults.get(propName);
        }
        if (propSchema.has(FIELD_DEFAULT)) {
            return JsonNodeValueConverter.coerce(objectMapper, propSchema, propSchema.get(FIELD_DEFAULT));
        }
        return null;
    }

    private Set<String> collectKeyCandidates(JsonNode propSchema, String propName) {
        Set<String> keys = new LinkedHashSet<>();
        keys.add(propName);
        JsonNode aliases = propSchema.get(EXT_X_ALIASES);
        if (aliases != null && aliases.isArray()) {
            for (JsonNode a : aliases) {
                if (a.isTextual()) keys.add(a.asText());
            }
        }
        return keys;
    }

    private String resolutionSource(JsonNode propSchema,
                                    Optional<JsonNode> candidate,
                                    Map<String, Object> defaults,
                                    String propName) {
        if (candidate.isPresent()) return "value source";
        if (defaults != null && defaults.containsKey(propName)) return "defaults";
        if (propSchema.has(FIELD_DEFAULT)) return "schema.default";
        return "none";
    }
}
