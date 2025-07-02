# DevHack Code Structure Documentation

This document provides an overview of the code structure in the DevHack application, explaining the different logical
blocks and their responsibilities.

## Table of Contents

1. [Overview](#overview)
2. [Domain Layer](#domain-layer)
3. [AI Components](#ai-components)
4. [Data Access Layer](#data-access-layer)
5. [Service Layer](#service-layer)
6. [Presentation Layer](#presentation-layer)
7. [Utility Components](#utility-components)
8. [Configuration](#configuration)

## Overview

The DevHack application is structured following a layered architecture pattern with clear separation of concerns. The
main logical blocks are:

- **Domain Layer**: Contains the core business entities and logic
- **AI Components**: Handles AI-related functionality like question generation and answer evaluation
- **Data Access Layer**: Manages data persistence and retrieval
- **Service Layer**: Implements business logic and orchestrates operations
- **Presentation Layer**: Handles user interface and HTTP requests/responses
- **Utility Components**: Provides common functionality used across the application
- **Configuration**: Contains application configuration settings

## Domain Layer

The domain layer represents the core business concepts and rules of the application. It is located in the
`com.vladte.devhack.model` package.

### Key Components

- **Basic Entities**: Core domain objects like `User`, `VacancyResponse`, `InterviewQuestion`, `Answer`, etc.
- **Enums**: Domain-specific enumerations like `InterviewStage`

### Example

```java
// Example of a domain entity
@Entity
public class VacancyResponse extends BasicEntity {
    private String companyName;
    private String position;
    private String technologies;
    private String pros;
    private String cons;
    private String notes;
    private String salary;
    private String location;
    private InterviewStage interviewStage;
    
    @ManyToOne
    private User user;
    
    @ManyToMany
    private Set<Tag> tags;
    
    // Getters and setters
}
```

## AI Components

The AI components handle integration with AI services and AI-related functionality. They are located in the
`com.vladte.devhack.service.api` package.

### Key Components

- **OpenAiService**: Handles communication with the OpenAI API
- **AbstractAiService**: Base class for AI services with common functionality
- **QuestionGenerationService**: Generates interview questions using AI
- **AiPromptConstraints**: Defines constraints for AI prompts

### Example

```java
// Example of an AI service
@Service
public class OpenAiServiceImpl implements OpenAiService {
    @Override
    public Map<String, Object> checkAnswerWithFeedback(String questionText, String answerText) {
        // Implementation that calls OpenAI API to evaluate an answer
        // Returns a map with score and feedback
    }
    
    @Override
    public List<InterviewQuestion> generateQuestionsForTechnology(String technology, int count) {
        // Implementation that generates interview questions for a specific technology
    }
}
```

## Data Access Layer

The data access layer handles data persistence and retrieval. It is located in the `com.vladte.devhack.repository`
package.

### Key Components

- **JPA Repositories**: Interfaces extending Spring Data JPA repositories for CRUD operations
- **Custom Query Methods**: Methods with custom queries for specific data access needs

### Example

```java
// Example of a repository interface
@Repository
public interface VacancyResponseRepository extends JpaRepository<VacancyResponse, UUID> {
    List<VacancyResponse> findByUser(User user);
    
    Page<VacancyResponse> findByUser(User user, Pageable pageable);
    
    @Query("SELECT v FROM VacancyResponse v WHERE " +
           "(:query IS NULL OR LOWER(v.companyName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.position) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.technologies) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:stage IS NULL OR v.interviewStage = :stage)")
    Page<VacancyResponse> searchVacancyResponses(
            @Param("query") String query,
            @Param("stage") InterviewStage stage,
            Pageable pageable);
}
```

## Service Layer

The service layer implements business logic and orchestrates operations. It is divided into three main packages:

### Domain Services (`com.vladte.devhack.service.domain`)

Handle core business logic related to domain entities.

#### Key Components

- **BaseService**: Base interface for all domain services
- **Entity-specific services**: Services for each domain entity (e.g., `UserService`, `VacancyResponseService`)

### View Services (`com.vladte.devhack.service.view`)

Prepare data for presentation in the UI.

#### Key Components

- **BaseViewService**: Base interface for all view services
- **Entity-specific view services**: Services for preparing views (e.g., `VacancyResponseViewService`)

### API Services (`com.vladte.devhack.service.api`)

Handle integration with external APIs and AI functionality.

#### Key Components

- **OpenAiService**: Service for OpenAI integration
- **QuestionGenerationService**: Service for generating interview questions

### Example

```java
// Example of a domain service implementation
@Service
public class VacancyResponseServiceImpl extends UserOwnedServiceImpl<VacancyResponse, UUID, VacancyResponseRepository> 
        implements VacancyResponseService {
    
    @Override
    public Page<VacancyResponse> getVacancyResponsesByUser(User user, Pageable pageable) {
        return repository.findByUser(user, pageable);
    }
    
    @Override
    public Page<VacancyResponse> searchVacancyResponses(String query, InterviewStage stage, Pageable pageable) {
        return repository.searchVacancyResponses(query, stage, pageable);
    }
}
```

## Presentation Layer

The presentation layer handles user interface and HTTP requests/responses. It consists of controllers and DTOs.

### Controllers (`com.vladte.devhack.controller`)

Handle HTTP requests and responses.

#### Key Components

- **BaseController**: Base class for all controllers
- **BaseCrudController**: Base class for CRUD operations
- **UserEntityController**: Base class for user-owned entities
- **Entity-specific controllers**: Controllers for each domain entity (e.g., `VacancyResponseController`)

### DTOs (`com.vladte.devhack.dto`)

Data Transfer Objects for transferring data between the service layer and the presentation layer.

#### Key Components

- **BaseDTO**: Base interface for all DTOs
- **Entity-specific DTOs**: DTOs for each domain entity (e.g., `VacancyResponseDTO`)

### Mappers (`com.vladte.devhack.mapper`)

Convert between domain entities and DTOs.

#### Key Components

- **EntityDTOMapper**: Base interface for all mappers
- **Entity-specific mappers**: Mappers for each domain entity (e.g., `VacancyResponseMapper`)

### Example

```java
// Example of a controller
@Controller
@RequestMapping("/vacancies")
public class VacancyResponseController extends UserEntityController<VacancyResponse, UUID, VacancyResponseService> {
    
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        // Implementation that delegates to the view service
        vacancyResponseViewService.prepareCurrentUserVacancyResponsesModel(page, size, model);
        vacancyResponseViewService.setCurrentUserVacancyResponsesPageTitle(model);
        return "vacancies/list";
    }
    
    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String stage,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        // Implementation that delegates to the view service
        vacancyResponseViewService.prepareSearchResultsModel(query, stage, page, size, model);
        vacancyResponseViewService.setSearchResultsPageTitle(model);
        return "vacancies/list";
    }
}
```

## Utility Components

The utility components provide common functionality used across the application. They are located in the
`com.vladte.devhack.util` package.

### Key Components

- **Date utilities**: Utilities for working with dates
- **String utilities**: Utilities for string manipulation
- **Security utilities**: Utilities for security-related operations

## Configuration

The configuration components contain application configuration settings. They are located in the
`com.vladte.devhack.config` package.

### Key Components

- **Security configuration**: Configuration for Spring Security
- **Web configuration**: Configuration for web-related settings
- **API configuration**: Configuration for API integrations

### Example

```java
// Example of a configuration class
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/", "/home", "/register", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }
}
```