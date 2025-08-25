package com.vladte.devhack.infra.ai.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonPathUtilsTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void resolvePath_supportsJsonPointerAndDotPaths() throws Exception {
        JsonNode node = om.readTree("{" +
                "\"a\": {\"b\":[{\"c\": 123}]}," +
                "\"root\": {\"inner\": true}" +
                "}");

        assertEquals(123, JsonPathUtils.resolvePath(node, "/a/b/0/c").asInt());
        assertEquals(123, JsonPathUtils.resolvePath(node, "a.b[0].c").asInt());
        assertTrue(JsonPathUtils.resolvePath(node, "/x/y").isMissingNode());
    }

    @Test
    void deepSearchByAnyKey_findsNestedInArrayAndObjects() throws Exception {
        JsonNode node = om.readTree("{" +
                "\"wrapper\": {\"items\":[{\"name\":\"foo\"},{\"target\":42}]}," +
                "\"other\":{}} ");
        JsonNode found = JsonPathUtils.deepSearchByAnyKey(node, Set.of("target"));
        assertEquals(42, found.asInt());
    }

    @Test
    void buildPathCandidates_variantsAndTail() {
        var list = JsonPathUtils.buildPathCandidates("Entity.field.sub", true);
        assertTrue(list.contains("entity.field.sub"));
        assertTrue(list.contains("Entity.field.sub"));
        assertTrue(list.contains("field.sub"));
    }
}
