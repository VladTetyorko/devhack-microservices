package com.vladte.devhack.common.engine.ai.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class SourceNormalizationUtils {

    private SourceNormalizationUtils() {
    }

    public static List<JsonNode> normalizeSources(ObjectMapper objectMapper, Object... sources) {
        if (sources == null || sources.length == 0) return List.of();
        List<JsonNode> nodes = new ArrayList<>(sources.length * 2);
        for (Object src : sources) {
            if (src == null) continue;

            JsonNode node;
            if (src instanceof JsonNode jn) {
                node = jn;
            } else if (src instanceof CharSequence cs) {
                String s = cs.toString();
                try {
                    node = objectMapper.readTree(s);
                } catch (Exception e) {
                    node = objectMapper.valueToTree(s);
                }
            } else {
                if (notHibernateInitialized(src)) {
                    Object id = tryGetHibernateIdentifier(src);
                    node = id != null ? objectMapper.valueToTree(id) : MissingNode.getInstance();
                } else {
                    node = safeValueToTree(objectMapper, src);
                }
            }

            nodes.add(node);

            if (!(src instanceof JsonNode) && !(src instanceof CharSequence)) {
                String simpleName = src.getClass().getSimpleName();
                if (!simpleName.isBlank()) {
                    String camelRoot = NamingUtils.toCamel(NamingUtils.toSnake(simpleName));
                    ObjectNode wrapper = objectMapper.createObjectNode();
                    wrapper.set(camelRoot, node);
                    nodes.add(wrapper);
                }
            }
        }
        return nodes;
    }

    public static JsonNode safeValueToTree(ObjectMapper objectMapper, Object src) {
        try {
            return objectMapper.valueToTree(src);
        } catch (IllegalArgumentException ex) {
            return reflectToTree(objectMapper, src);
        }
    }

    public static JsonNode reflectToTree(ObjectMapper objectMapper, Object src) {
        if (src == null) return MissingNode.getInstance();
        try {
            ObjectNode obj = objectMapper.createObjectNode();
            Class<?> cls = src.getClass();
            while (cls != null && cls != Object.class) {
                Field[] fields = cls.getDeclaredFields();
                for (Field f : fields) {
                    int mod = f.getModifiers();
                    if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) continue;

                    if (!isJpaAttributeLoaded(src, f.getName())) continue;

                    try {
                        f.setAccessible(true);
                        Object val = f.get(src);
                        if (val == src) continue;

                        JsonNode child;
                        if (val == null) {
                            child = objectMapper.nullNode();
                        } else if (notHibernateInitialized(val)) {
                            Object id = tryGetHibernateIdentifier(val);
                            child = id != null ? objectMapper.valueToTree(id) : MissingNode.getInstance();
                        } else if (val instanceof CharSequence || val instanceof Number || val instanceof Boolean) {
                            child = objectMapper.valueToTree(val);
                        } else {
                            child = safeValueToTree(objectMapper, val);
                        }
                        obj.set(f.getName(), child);
                    } catch (Throwable ignored) {
                    }
                }
                cls = cls.getSuperclass();
            }
            return obj;
        } catch (Throwable t) {
            return objectMapper.valueToTree(String.valueOf(src));
        }
    }

    private static boolean notHibernateInitialized(Object value) {
        if (value == null) return false;
        try {
            Class<?> hib = Class.forName("org.hibernate.Hibernate");
            Method m = hib.getMethod("isInitialized", Object.class);
            Object r = m.invoke(null, value);
            return !Boolean.TRUE.equals(r);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Object tryGetHibernateIdentifier(Object proxy) {
        if (proxy == null) return null;
        try {
            Class<?> proxyIface = Class.forName("org.hibernate.proxy.HibernateProxy");
            if (!proxyIface.isInstance(proxy)) return null;
            Method getLI = proxyIface.getMethod("getHibernateLazyInitializer");
            Object li = getLI.invoke(proxy);
            Method getId = li.getClass().getMethod("getIdentifier");
            return getId.invoke(li);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isJpaAttributeLoaded(Object owner, String attribute) {
        if (owner == null || attribute == null) return true;
        try {
            Class<?> persistence = Class.forName("jakarta.persistence.Persistence");
            Method getPU = persistence.getMethod("getPersistenceUtil");
            Object pu = getPU.invoke(null);
            Method isLoaded = pu.getClass().getMethod("isLoaded", Object.class, String.class);
            Object r = isLoaded.invoke(pu, owner, attribute);
            return Boolean.TRUE.equals(r);
        } catch (Throwable ignored) {
            return true;
        }
    }
}
