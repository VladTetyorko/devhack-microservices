package com.vladte.devhack.infra.ai.util;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Naming related helpers: case conversions and key variant generation.
 */
public final class NamingUtils {

    private NamingUtils() {
    }

    public static String toSnake(String value) {
        if (value == null || value.isEmpty()) return value;
        String snake = value
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replace('-', '_')
                .replace(' ', '_');
        return snake.toLowerCase(Locale.ROOT);
    }

    public static String toCamel(String value) {
        if (value == null || value.isEmpty()) return value;
        String[] parts = value.split("[-_\\s]+");
        if (parts.length == 0) return value;
        StringBuilder builder = new StringBuilder(parts[0].toLowerCase(Locale.ROOT));
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            builder.append(Character.toUpperCase(parts[i].charAt(0)))
                    .append(parts[i].substring(1).toLowerCase(Locale.ROOT));
        }
        return builder.toString();
    }

    public static Set<String> generateKeyVariants(String key) {
        Set<String> variants = new LinkedHashSet<>();
        if (key == null) return variants;

        String normalizedKey = key.trim();
        variants.add(normalizedKey);
        variants.add(normalizedKey.toLowerCase());
        variants.add(normalizedKey.toUpperCase());

        String snake = toSnake(normalizedKey);
        String kebab = snake.replace('_', '-');
        String camel = toCamel(snake);

        variants.add(snake);
        variants.add(kebab);
        variants.add(camel);
        variants.add(normalizedKey.replaceAll("[^A-Za-z0-9]", "").toLowerCase()); // loose variant
        return variants;
    }
}
