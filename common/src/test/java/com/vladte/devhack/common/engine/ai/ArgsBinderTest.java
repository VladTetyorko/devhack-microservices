package com.vladte.devhack.common.engine.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ArgsBinderTest {

    private ObjectMapper om;
    private ArgsBinder binder;

    @BeforeMethod
    public void setUp() {
        this.om = new ObjectMapper();
        this.binder = new ArgsBinder(om);
    }

    private Map<String, Object> schema(Object... kv) {
        // helper to build a simple JSON schema with properties
        Map<String, Object> props = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            props.put((String) kv[i], kv[i + 1]);
        }
        Map<String, Object> root = new HashMap<>();
        root.put("type", "object");
        root.put("properties", props);
        return root;
    }

    @Test
    public void testDirectKeyAndTypeCoercion() {
        Map<String, Object> nameSchema = Map.of("type", "string");
        Map<String, Object> ageSchema = Map.of("type", "integer");
        Map<String, Object> s = schema("name", nameSchema, "age", ageSchema);

        Map<String, Object> src = Map.of("name", 123, "age", "42");
        Map<String, Object> result = binder.bind(s, Map.of(), src);

        assertEquals(result.get("name"), "123", "coerce to string");
        assertTrue(result.get("age") instanceof Number, "age should be numeric type");
        assertEquals(((Number) result.get("age")).longValue(), 42L, "coerce to integer");
    }

    @Test
    public void testAliasesAndVariants() {
        Map<String, Object> qSchema = new HashMap<>();
        qSchema.put("type", "string");
        qSchema.put("x-aliases", List.of("questionText", "q_text"));
        Map<String, Object> s = schema("question", qSchema);

        Map<String, Object> src = Map.of("qText", "What is DI? "); // camel variant of q_text
        Map<String, Object> result = binder.bind(s, Map.of(), src);

        assertEquals(result.get("question"), "What is DI? ");
    }

    @Test
    public void testExplicitXPathJsonPointer() {
        Map<String, Object> aSchema = new HashMap<>();
        aSchema.put("type", "string");
        aSchema.put("x-path", "/payload/user/name");
        Map<String, Object> s = schema("author", aSchema);

        String json = "{\"payload\":{\"user\":{\"name\":\"Ada\"}}}";
        Map<String, Object> result = binder.bind(s, Map.of(), json);

        assertEquals(result.get("author"), "Ada");
    }

    @Test
    public void testExplicitXPathDotPathWithArrays() throws Exception {
        Map<String, Object> tagSchema = new HashMap<>();
        tagSchema.put("type", "string");
        tagSchema.put("x-path", "items[0].name");
        Map<String, Object> s = schema("tag", tagSchema);

        JsonNode src = om.readTree("{\"items\":[{\"name\":\"Java\"},{\"name\":\"Spring\"}]} ");
        Map<String, Object> result = binder.bind(s, Map.of(), src);

        assertEquals(result.get("tag"), "Java");
    }

    @Test
    public void testDeepSearchByKey() {
        Map<String, Object> diffSchema = new HashMap<>();
        diffSchema.put("type", "string");
        diffSchema.put("x-deepSearch", true);
        Map<String, Object> s = schema("difficulty", diffSchema);

        String nested = "{\"outer\":{\"inner\":{\"difficulty\":\"HARD\"}}}";
        Map<String, Object> result = binder.bind(s, Map.of(), nested);

        assertEquals(result.get("difficulty"), "HARD");
    }

    @Test
    public void testDefaultsFromPromptAndSchema() {
        Map<String, Object> countSchema = Map.of("type", "integer", "default", 5);
        Map<String, Object> s = schema("count", countSchema);

        Map<String, Object> promptDefaults = Map.of("count", 10);
        Map<String, Object> result = binder.bind(s, promptDefaults /* override schema default */);

        assertEquals(result.get("count"), 10);
    }

    @Test
    public void testObjectAndArrayCoercion() {
        Map<String, Object> objSchema = Map.of("type", "object");
        Map<String, Object> arrSchema = Map.of("type", "array");
        Map<String, Object> s = schema("meta", objSchema, "tags", arrSchema);

        String json = "{\"meta\":{\"a\":1},\"tags\":[\"one\",\"two\"]}";
        Map<String, Object> result = binder.bind(s, Map.of(), json);

        assertTrue(result.get("meta") instanceof Map);
        assertTrue(result.get("tags") instanceof java.util.List);
    }

    @Test
    public void testDottedAliasPathResolvesObjectNameNestedField() {
        // Schema expects two strings, and allows aliases pointing to nested object paths
        Map<String, Object> qTextSchema = new HashMap<>();
        qTextSchema.put("type", "string");
        qTextSchema.put("x-aliases", List.of("question.text"));

        Map<String, Object> aTextSchema = new HashMap<>();
        aTextSchema.put("type", "string");
        aTextSchema.put("x-aliases", List.of("answer.text"));

        Map<String, Object> s = schema("qText", qTextSchema, "aText", aTextSchema);

        // Source simulates user providing nested objects with same field name 'text'
        Map<String, Object> src = Map.of(
                "question", Map.of("text", "What is DI?"),
                "answer", Map.of("text", "Dependency Injection")
        );

        Map<String, Object> result = binder.bind(s, Map.of(), src);

        assertEquals(result.get("qText"), "What is DI?");
        assertEquals(result.get("aText"), "Dependency Injection");
    }

    public static class Question {
        public String text;

        public Question(String t) {
            this.text = t;
        }
    }

    public static class Answer {
        public String text;

        public Answer(String t) {
            this.text = t;
        }
    }

    @Test
    public void testDottedAliasPathResolvesPojoClassNameWrapper() {
        Map<String, Object> qTextSchema = new HashMap<>();
        qTextSchema.put("type", "string");
        qTextSchema.put("x-aliases", List.of("question.text"));
        Map<String, Object> aTextSchema = new HashMap<>();
        aTextSchema.put("type", "string");
        aTextSchema.put("x-aliases", List.of("answer.text"));
        Map<String, Object> s = schema("qText", qTextSchema, "aText", aTextSchema);

        Question q = new Question("What is DI?");
        Answer a = new Answer("Dependency Injection");
        // Normalize will add wrappers {"question": q-node}, {"answer": a-node}
        Map<String, Object> result = binder.bind(s, Map.of(), q, a);

        assertEquals(result.get("qText"), "What is DI?");
        assertEquals(result.get("aText"), "Dependency Injection");
    }

    public static class InterviewQuestion {
        public String text;

        public InterviewQuestion(String t) {
            this.text = t;
        }
    }

    @Test
    public void testClassNameRootPathForPojo() {
        Map<String, Object> questionSchema = new HashMap<>();
        questionSchema.put("type", "string");
        // Use camelCase class-name-based alias path
        questionSchema.put("x-aliases", List.of("interviewQuestion.text"));
        Map<String, Object> s = schema("question", questionSchema);

        InterviewQuestion iq = new InterviewQuestion("Explain ACID properties in databases.");
        Map<String, Object> result = binder.bind(s, Map.of(), iq);

        assertEquals(result.get("question"), "Explain ACID properties in databases.");
    }

    @Test
    public void testSnakeCaseClassNameRootPathForPojo() {
        Map<String, Object> questionSchema = new HashMap<>();
        questionSchema.put("type", "string");
        // Use snake_case variant of class-name-based alias path; buildPathCandidates expands root variants
        questionSchema.put("x-aliases", List.of("interview_question.text"));
        Map<String, Object> s = schema("question", questionSchema);

        InterviewQuestion iq = new InterviewQuestion("What is Dependency Injection?");
        Map<String, Object> result = binder.bind(s, Map.of(), iq);

        assertEquals(result.get("question"), "What is Dependency Injection?");
    }
}
