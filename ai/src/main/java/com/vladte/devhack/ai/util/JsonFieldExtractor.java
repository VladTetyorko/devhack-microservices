package com.vladte.devhack.ai.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class JsonFieldExtractor {

    private static final Set<String> FIELDS_NOT_INCLUDE = new HashSet<>(Set.of("responses", "deadline", "createdAt", "updatedAt"));

    @SneakyThrows
    public static String parse(Class clazz) {
        Map<String, String> fields = new LinkedHashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (FIELDS_NOT_INCLUDE.contains(field.getName()))
                continue;
            fields.put(field.getName(), field.getType().getSimpleName());
        }

        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(fields);
    }
}
