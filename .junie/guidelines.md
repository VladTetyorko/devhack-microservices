# Project Guidelines

# DevHack Project Guidelines for Junie

## Project Overview

DevHack is a comprehensive AI-powered interview preparation platform built with a microservices architecture using Java
21 and Spring Boot 3.2.5. The application helps job seekers practice technical interviews through AI-generated
questions, automated feedback, and progress tracking.

## Architecture Overview

### Module Structure

```
DevHack/
├── ai/           - AI service integration module
├── common/       - Main application module (controllers, services, views)
├── entities/     - Domain entities and data models
├── parser/       - Job site parser (in progress)
├── infra/        - Infrastructure components (Kafka, messaging)
└── .junie/       - Junie configuration and guidelines
```

### Technology Stack

- **Backend**: Java 21, Spring Boot 3.2.5, Spring Security, Spring Data JPA
- **Database**: PostgreSQL with Liquibase migrations
- **Frontend**: Thymeleaf, Bootstrap 5, jQuery
- **Messaging**: Apache Kafka for event-driven architecture
- **AI Integration**: OpenAI API, LocalAI/GPT-J
- **Containerization**: Docker, Docker Compose
- **Build Tool**: Gradle

## Code Organization Patterns

### Service Layer Architecture

The project follows a layered service architecture:

1. **Domain Services** (`service/domain/`): Core business logic
2. **View Services** (`service/view/`): Presentation logic and UI data preparation
3. **Generation Services** (`service/generations/`): AI-related functionality
4. **Kafka Services** (`service/kafka/`): Message handling and event processing

### Controller Patterns

Controllers follow inheritance-based patterns:

- **BaseController**: Common functionality for all controllers
- **BaseCrudController**: Generic CRUD operations with pagination
- **BaseRestController**: REST API specific functionality

Example controller structure:

```java

@Controller
public class InterviewQuestionController extends BaseCrudController<
        InterviewQuestion,      // Entity type
        InterviewQuestionDTO,   // DTO type
        UUID,                   // ID type
        InterviewQuestionService,   // Service type
        InterviewQuestionMapper     // Mapper type
        > {
    // Controller-specific methods
}
```

### Entity Design

Entities follow a hierarchical structure:

- **BasicEntity**: Base entity with common fields (id, createdAt, updatedAt)
- **UserOwnedBasicEntity**: Extends BasicEntity with user ownership
- Domain-specific entities in `entities/global/`, `entities/user/`, `entities/personalized/`

### Service Implementation Patterns

Services use dependency injection and follow single responsibility principle:

```java

@Service
public class QuestionGenerationOrchestrationServiceImpl
        implements QuestionGenerationOrchestrationService {

    private final TagService tagService;
    private final QuestionGenerationService questionGenerationService;

    // Constructor injection
    public QuestionGenerationOrchestrationServiceImpl(
            TagService tagService,
            QuestionGenerationService questionGenerationService) {
        this.tagService = tagService;
        this.questionGenerationService = questionGenerationService;
    }

    // Asynchronous processing with CompletableFuture
    @Override
    public CompletableFuture<List<InterviewQuestion>> startQuestionGeneration(
            String tagName, int count, String difficulty) {
        return dispatchGeneration(tagName, count, difficulty);
    }
}
```

## Key Implementation Patterns

### 1. Asynchronous Processing

- Uses `CompletableFuture` for non-blocking operations
- Kafka messaging for inter-service communication
- Error handling with graceful degradation

### 2. Event-Driven Architecture

- Kafka topics for AI processing requests
- Producers and consumers for message handling
- Asynchronous AI question generation and answer evaluation

### 3. Security Implementation

- Spring Security with JWT authentication
- Method-level security annotations
- CSRF protection for web forms

### 4. Data Access Patterns

- Repository pattern with Spring Data JPA
- Custom query methods with `@Query` annotations
- Pagination and sorting support
- Liquibase for database migrations

### 5. Error Handling

- Global exception handlers
- Custom exception classes
- Graceful error responses with user-friendly messages

## AI Integration Architecture

### AI Service Structure

```
ai/
├── service/
│   ├── ai/           - AI provider implementations
│   ├── kafka/        - Message consumers/producers
│   └── orchestration/ - AI workflow orchestration
```

### AI Processing Flow

1. Request sent via Kafka from Common module
2. AI module processes using OpenAI API or LocalAI
3. Results sent back via Kafka
4. Common module saves results and notifies user

## Testing Patterns

### Test Structure

```
test/
├── service/     - Service layer tests
├── repository/  - Repository tests
├── mapper/      - Mapper tests
└── controller/  - Controller tests
```

### Testing Approaches

- Unit tests with Mockito for mocking dependencies
- Integration tests for repository layer
- Service tests with test slices
- Comprehensive test coverage for critical business logic

## Configuration Management

### Profile-Based Configuration

- **Default Profile**: Docker-based deployment
- **Local Profile**: Local development with Docker infrastructure
- Environment-specific properties files

### Key Configuration Areas

- Database connections and JPA settings
- Kafka broker configuration
- AI service endpoints and API keys
- Security and authentication settings

## Development Best Practices

### 1. Code Organization

- Clear separation of concerns between layers
- Single responsibility principle for services
- Dependency injection over static dependencies
- Interface-based programming

### 2. Naming Conventions

- Service interfaces: `XxxService`
- Service implementations: `XxxServiceImpl`
- Controllers: `XxxController` (UI) or `XxxRestController` (API)
- DTOs: `XxxDTO`
- Entities: Domain-specific names without suffixes

### 3. Error Handling

- Use `Optional<T>` for potentially null returns
- Custom exceptions for business logic errors
- Global exception handlers for consistent error responses
- Logging with appropriate levels (INFO, WARN, ERROR)

### 4. Asynchronous Processing

- Use `CompletableFuture` for async operations
- Proper exception handling in async chains
- Timeout configurations for external service calls

### 5. Database Design

- Proper entity relationships with cascade settings
- Audit fields in base entities
- Liquibase changesets for schema evolution
- Proper indexing for performance

## Common Patterns and Utilities

### 1. Base Classes

- `BaseController`: Common controller functionality
- `BaseCrudController`: Generic CRUD operations
- `BasicEntity`: Common entity fields
- `BaseService`: Common service patterns

### 2. Utility Classes

- Mappers for entity-DTO conversion
- Validation utilities
- String and collection utilities

### 3. View Services

- Separate services for UI data preparation
- Model attribute management
- Page title and navigation handling

## Kafka Integration Patterns

### Message Flow

1. **Question Generation**: Common → AI module
2. **Answer Evaluation**: Common → AI module
3. **Results Processing**: AI → Common module

### Topic Structure

- Dedicated topics for different message types
- Proper serialization/deserialization
- Error handling and retry mechanisms

## Security Considerations

### Authentication & Authorization

- JWT-based stateless authentication
- Role-based access control
- Method-level security where needed

### Data Protection

- Input validation and sanitization
- SQL injection prevention through JPA
- XSS protection in templates

## Performance Optimization

### Database Optimization

- Proper JPA fetch strategies
- Query optimization with indexes
- Connection pooling configuration

### Caching Strategies

- Application-level caching for frequently accessed data
- Database query result caching

### Asynchronous Processing

- Non-blocking AI operations
- Background processing for heavy operations

## Deployment and Infrastructure

### Docker Configuration

- Multi-stage builds for optimized images
- Docker Compose for local development
- Health checks for service monitoring

### Environment Management

- Profile-based configuration
- External configuration for sensitive data
- Proper logging configuration

## Key Files and Locations

### Configuration Files

- `application.yml` - Main application configuration
- `docker-compose.yml` - Container orchestration
- `build.gradle` - Build configuration

### Important Directories

- `common/src/main/java/com/vladte/devhack/common/` - Main application code
- `entities/src/main/java/com/vladte/devhack/entities/` - Domain entities
- `ai/src/main/java/com/vladte/devhack/ai/` - AI service implementation
- `infra/src/main/java/com/vladte/devhack/infra/` - Infrastructure components

## Development Workflow

### Local Development Setup

1. Start infrastructure services: `docker-compose up -d postgres kafka zookeeper`
2. Run application with local profile: `./gradlew :common:bootRun --args='--spring.profiles.active=local'`
3. Run AI service: `./gradlew :ai:bootRun --args='--spring.profiles.active=local'`

### Testing

- Run all tests: `./gradlew test`
- Run specific module tests: `./gradlew :common:test`

### Building

- Build all modules: `./gradlew build`
- Build specific module: `./gradlew :common:build`

## Common Issues and Solutions

### 1. Kafka Connection Issues

- Ensure Kafka is running and accessible
- Check profile-specific configuration
- Verify topic creation and permissions

### 2. Database Migration Issues

- Check Liquibase changelog files
- Ensure proper database permissions
- Verify migration order and dependencies

### 3. AI Service Integration

- Verify API keys and endpoints
- Check network connectivity
- Monitor rate limits and quotas

## Future Considerations

### Scalability

- Horizontal scaling of services
- Database sharding strategies
- Caching layer implementation

### Monitoring and Observability

- Application metrics collection
- Distributed tracing
- Health check endpoints

### Security Enhancements

- OAuth2 integration
- API rate limiting
- Enhanced audit logging

### Patterns and rules of coding

- Coder is following SOLID, DRY, KISS principals
- Coder follows OOP principals and build logic that will be scaled in the future
- Coder covers his code with tests
- Coder adds docks to his code
- User experience is in priority, if something can be better for user — it should be implemented
