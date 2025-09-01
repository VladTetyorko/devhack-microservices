package com.vladte.devhack.common.engine.ai.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamingUtilsTest {

    @Test
    void toSnake_convertsCamelKebabAndSpaces() {
        assertEquals("hello_world", NamingUtils.toSnake("helloWorld"));
        assertEquals("hello_world", NamingUtils.toSnake("Hello-World"));
        assertEquals("hello_world", NamingUtils.toSnake("Hello World"));
    }

    @Test
    void toCamel_convertsSnakeAndKebab() {
        assertEquals("helloWorld", NamingUtils.toCamel("hello_world"));
        assertEquals("helloWorld", NamingUtils.toCamel("hello-world"));
        assertEquals("helloWorld", NamingUtils.toCamel("Hello World"));
    }

    @Test
    void generateKeyVariants_containsCommonForms() {
        Set<String> variants = NamingUtils.generateKeyVariants("HelloWorld");
        assertTrue(variants.contains("HelloWorld"));
        assertTrue(variants.contains("helloworld"));
        assertTrue(variants.contains("HELLOWORLD"));
        assertTrue(variants.contains("hello_world"));
        assertTrue(variants.contains("hello-world"));
        assertTrue(variants.contains("helloWorld"));
    }
}
