# Controller Testing Guide

This document provides guidelines for testing controllers in the DevHack project.

## Testing Approach

Controllers in this project follow a hierarchical structure:

- `BaseController`: Base class for all controllers
- `BaseCrudController`: Extends BaseController, adds CRUD operations
- `UserEntityController`: Extends BaseCrudController, adds user-specific operations
- Concrete controllers: Implement specific functionality

The testing approach should follow this hierarchy, with tests for each level of abstraction.

## Test Structure

Each controller test should:

1. Mock all dependencies (services, mappers, view services)
2. Test all public methods
3. Test both success and error cases
4. Verify that the correct view is returned
5. Verify that the model is populated correctly
6. Test fallback behavior when view services are not available

## Example Tests

### BaseController Tests

See `HomeControllerTest.java` for an example of testing a controller that extends BaseController.

Key points:

- Mock the BaseViewService
- Test the setPageTitle method
- Test fallback behavior when BaseViewService is null

### BaseCrudController Tests

See `BaseCrudControllerTest.java` for an example of testing a controller that extends BaseCrudController.

Key points:

- Create concrete test implementations of abstract classes
- Mock all dependencies (service, mapper, view services)
- Test list() and view() methods
- Test fallback behavior when BaseCrudViewService is null
- Test error handling (e.g., entity not found)

### AuthController Tests

See `AuthControllerTest.java` for an example of testing a controller with form handling.

Key points:

- Mock the UserService
- Test form submission with valid and invalid data
- Test validation error handling

## Testing Patterns

### Testing Abstract Controllers

When testing abstract controllers:

1. Create a concrete test implementation of the abstract class
2. Create test implementations of any required interfaces or abstract classes
3. Test the concrete implementation

### Testing View Names

Always verify that the controller returns the correct view name:

```java
assertEquals("expected/view", viewName);
```

### Testing Model Attributes

Verify that the model is populated correctly:

```java
verify(model).addAttribute(eq("attributeName"), any(AttributeType.class));
```

### Testing Error Handling

Test error cases to ensure proper error handling:

```java
ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
    controller.method(invalidId, model);
});
assertEquals("404 NOT_FOUND \"Entity not found\"", exception.getMessage());
```

## Next Steps for Controller Testing

The following controllers still need tests:

1. UserEntityController
2. UserOwnedCrudController
3. AnswerController
4. InterviewQuestionController
5. NoteController
6. TagController
7. UserController
8. VacancyResponseController

When implementing these tests, follow the patterns established in the existing tests.