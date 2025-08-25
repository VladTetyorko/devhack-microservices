# SJSON Schema, ArgsBinder, and Prompt Engine Guide

This guide explains how to build concise JSON Schemas ("SJSON"), how the ArgsBinder maps your heterogeneous inputs into schema-defined arguments, and how PromptEngine validates and renders prompts from your AiPrompt templates.

It contains:
- Quick start examples
- Schema reference with DevHack extensions (x-* fields)
- ArgsBinder resolution rules (x-path, x-aliases, key variants, deep search)
- PromptEngine usage patterns
- Best practices and troubleshooting tips


## 1. Quick Start

### 1.1 Define arguments schema (SJSON)

```json
{
  "type": "object",
  "properties": {
    "position": {
      "type": "string",
      "x-aliases": ["jobTitle", "title"],
      "default": "Java Developer"
    },
    "company": {
      "type": "string",
      "x-aliases": ["org", "employer"],
      "x-path": "vacancy.company.name"
    },
    "level": {
      "type": "string",
      "x-aliases": ["seniority"],
      "default": "mid"
    },
    "skills": {
      "type": "array"
    },
    "context": {
      "type": "object",
      "properties": {
        "candidateName": { "type": "string", "x-aliases": ["userName"] },
        "yearsOfExperience": { "type": "integer", "x-aliases": ["yoe"] }
      }
    }
  }
}
```

### 1.2 Prepare inputs (any mix is OK)

```java
record Vacancy(String title, Company company) {}
record Company(String name) {}

Map<String, Object> map = Map.of(
  "skills", List.of("Java", "Spring", "Kafka"),
  "yoe", 5,
  "userName", "Alex"
);

String json = "{\n  \"jobTitle\": \"Backend Engineer\",\n  \"vacancy\": { \"company\": { \"name\": \"Acme Corp\" } }\n}";

Vacancy pojo = new Vacancy("Platform Engineer", new Company("DevHack"));
```

### 1.3 Bind arguments with ArgsBinder

```java
ObjectMapper om = new ObjectMapper();
ArgsBinder binder = new ArgsBinder(om);

Map<String, Object> args = binder.bind(schema, /* defaults */ Map.of(), /* deepSearch */ true, map, json, pojo);
```

Result (effective argument map):
```json
{
  "position": "Backend Engineer",
  "company": "DevHack",
  "level": "mid",
  "skills": ["Java", "Spring", "Kafka"],
  "context": {
    "candidateName": "Alex",
    "yearsOfExperience": 5
  }
}
```

### 1.4 Render an AiPrompt with PromptEngine

```java
AiPrompt prompt = AiPrompt.builder()
  .key("interview-question")
  .model("gpt-4o-mini")
  .systemTemplate("You are a helpful interviewer for {{position}} at {{company}}.")
  .userTemplate("Generate {{level}} difficulty questions on {{#skills}}{{.}}, {{/skills}}and assess answers.")
  .argsSchema(schema)
  .defaults(Map.of("level", "junior")) // will be overridden by bound args
  .responseContract(Map.of("type","object","properties", Map.of("questions", Map.of("type","array"))))
  .build();

PromptEngine engine = new PromptEngine(om);
AiRenderedRequestPayload rendered = engine.render(prompt, args); // or: engine.render(prompt, map, json, pojo)
```


## 2. SJSON Schema Reference (with DevHack extensions)

SJSON is standard JSON Schema 2020-12 for objects, plus a few non-standard, optional x-* extensions that help bind values from heterogeneous inputs.

Top-level:
- type: "object"
- properties: key-value pairs of property schemas

Common fields per property:
- type: "string" | "integer" | "number" | "boolean" | "array" | "object"
- default: any JSON value; used if nothing is found in inputs and no explicit default override is provided

DevHack extensions (optional):
- x-aliases: array of alternative keys or paths for this property
- x-path: a preferred path (JSON Pointer like "/a/b/0/c" or dot/bracket path like "vacancy.company[0].name") to resolve the value
- x-aliasPath: boolean. Interpret aliases as paths too (besides treating them as simple keys)
- x-allowTailAlias: boolean. When alias is a dot path, also try resolving only the tail part (e.g., for "Entity.field.sub" also try "field.sub")
- x-deepSearch: boolean. If true (or when global deepSearch flag passed to ArgsBinder is true), do recursive search for any matching key in nested structures

Nested objects:
- Use type: "object" and an inner "properties" object to define nested shapes

Example with extensions:
```json
{
  "type": "object",
  "properties": {
    "company": { "type": "string", "x-path": "vacancy.company.name" },
    "position": { "type": "string", "x-aliases": ["jobTitle", "title"], "x-aliasPath": true },
    "tags": { "type": "array", "x-aliases": ["skills", "stack"] },
    "context": {
      "type": "object",
      "properties": {
        "candidateName": { "type": "string", "x-aliases": ["userName", "profile.name"] },
        "yearsOfExperience": { "type": "integer", "x-aliases": ["yoe"], "x-deepSearch": true }
      }
    }
  }
}
```


## 3. ArgsBinder Resolution Rules

ArgsBinder consolidates values from multiple sources into a map conforming to your schema.

Supported sources:
- JsonNode
- JSON String
- Map / POJO (converted to JsonNode; POJOs have a wrapper added under a camelCase root of the class name to make root-path resolution easier)

Resolution order per property:
1) x-path: If present, this path is tried first on every source
2) x-aliases: For each alias
   - If alias looks like a path (starts with "/" or contains "." or "[") or x-aliasPath = true, build path candidates and try them
   - Otherwise, try key variants on current object level; if deepSearch enabled, recursively search nested objects/arrays for a matching key
3) Key variants of the property name itself, with the same deepSearch behavior

Key variants include:
- original, lower, UPPER
- snake_case, kebab-case, camelCase
- a loose alphanumeric-lowered form

Path formats supported:
- JSON Pointer: /a/b/0/c
- Dot/bracket: entity.field[0].subField

Defaults:
- If a value is not found, the binder falls back to explicit defaults (the Map passed into bind), and then to the schema's default
- Final output filters nulls for cleanliness

Deep search:
- Globally enabled via bind(argsSchema, defaults, true, sources...) or property-level x-deepSearch: true
- Recursively walks nested objects/arrays to find any matching key from the variants set

Examples:
```java
Map<String, Object> defaults = Map.of("level", "mid");
Map<String, Object> bound = binder.bind(schema, defaults, true, map, json, pojo);
```


## 4. Prompt Engine Usage

PromptEngine responsibilities:
- Merge defaults + provided args
- Validate args against the provided argsSchema (using networknt JSON Schema 2020-12)
- Render system and user templates via Mustache
- Build a chat-style message list and include the responseContract for downstream AI service usage

Basic usage:
```java
PromptEngine engine = new PromptEngine(new ObjectMapper());

// Option A: Provide already-bound args
AiRenderedRequestPayload rendered = engine.render(prompt, args);

// Option B: Let engine bind from heterogeneous sources using ArgsBinder internally
AiRenderedRequestPayload rendered2 = engine.render(prompt, map, json, pojo);
```

AiRenderedRequestPayload fields (key ones):
- promptId, promptKey, model
- parameters (from prompt)
- messages (system/user)
- input (the rendered user content)
- responseContract (echo of your prompt.getResponseContract())
- version

Validation errors:
- If arguments fail schema validation, an IllegalArgumentException is thrown with validation messages


## 5. Best Practices

- Prefer simple, stable keys in your inputs and map them via x-aliases for resilience
- Use x-path when you know the precise nested location
- Use x-aliasPath and x-allowTailAlias to be flexible with entity-root naming differences
- Enable deepSearch sparingly; it’s powerful but can be slower on large inputs
- Always provide a responseContract describing expected AI output shape
- Keep templates small and composable; leverage Mustache sections/iteration


## 6. Troubleshooting

- "Missing value for property": Check your aliases/paths and whether deepSearch is needed
- "Validation failed": Ensure args conform to types declared in schema (string/integer/number/boolean/array/object)
- POJO not binding: Verify it’s reachable via key variants or use explicit x-path
- Path not found: Confirm JSON Pointer vs dot/bracket format and array indices


## 7. Additional References

Source code:
- ArgsBinder: infra/src/main/java/com/vladte/devhack/infra/ai/ArgsBinder.java
- Utilities: infra/src/main/java/com/vladte/devhack/infra/ai/util/*
- PromptEngine: infra/src/main/java/com/vladte/devhack/infra/ai/PromptEngine.java

Tests:
- ArgsBinder and PromptEngine tests under infra/src/test/java/com/vladte/devhack/infra/ai
- Utility tests under infra/src/test/java/com/vladte/devhack/infra/ai/util

