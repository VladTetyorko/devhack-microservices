# DevHack - Interview Preparation Platform

DevHack is a comprehensive interview preparation platform designed to help job seekers practice for technical interviews. The application uses AI to generate interview questions, provide feedback on answers, and track progress across different technical topics.

## Project Overview

DevHack helps users prepare for technical interviews by:

- Generating AI-powered interview questions based on specific technical topics/tags
- Providing AI feedback on user answers
- Tracking job applications and interview stages
- Organizing questions and answers by technical topics
- Monitoring progress and performance across different areas

## Architecture

The application follows a modular architecture with clear separation of concerns:

```
DevHack
├── ai           - AI service integration module
├── common       - Main application module with controllers, services, and views
├── entities     - Domain entities and data models
├── parser       - Parser for job sites (🔧 in progress)
└── infra        - Infrastructure components (Kafka, etc.)
```

### Architecture Diagram

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Browser   │────▶│   Common    │────▶│  Database   │
│   Client    │◀────│   Module    │◀────│ (PostgreSQL)│
└─────────────┘     └──────┬──────┘     └─────────────┘
                           │
                           │ Kafka
                           ▼
                    ┌─────────────┐     ┌─────────────┐
                    │     AI      │────▶│  External   │
                    │   Module    │◀────│  AI APIs    │
                    └─────────────┘     └─────────────┘
                          │
                          |
                          ▼
                    ┌─────────────┐
                    │   Parser    │
                    │  (in prog)  │
                    └─────────────┘
```

### Data Flow

1. Users interact with the application through the web interface
2. The Common module handles requests and manages business logic
3. For AI-related tasks (question generation, answer feedback), requests are sent to the AI module via Kafka
4. The AI module processes requests using either local AI models or external AI APIs
5. Results are sent back to the Common module via Kafka
6. The Common module updates the database and presents results to the user

## Module Descriptions

### Entities Module

Contains all domain entities used across the application:

- `User` - User accounts and authentication
- `InterviewQuestion` - Interview questions with metadata
- `Answer` - User answers to questions with AI feedback
- `Tag` - Technical topics for categorizing questions
- `VacancyResponse` - Job applications and interview tracking
- `Note` - User notes on questions, answers, or job applications
- `Audit` - Audit records for tracking operations

### Infra Module

Provides infrastructure components used by other modules:

- Kafka configuration and topics
- Message models for inter-module communication
- Base services for Kafka producers and consumers

### AI Module

Handles AI-related functionality:

- Integration with AI APIs (OpenAI, GPT-J)
- Question generation based on technical topics
- Answer evaluation and feedback
- Asynchronous processing via Kafka

### Common Module

The main application module containing:

- Web controllers for handling HTTP requests
- Domain services for business logic
- View services for presentation logic
- Repositories for data access
- DTOs for data transfer
- Kafka providers/consumers for communication with the AI module

## Technologies Used

- **Backend**: Java 21, Spring Boot 3.5
- **Database**: PostgreSQL, Spring Data JPA, Liquibase
- **Frontend**: Thymeleaf, Bootstrap, jQuery
- **Messaging**: Kafka
- **AI Integration**: OpenAI API, Local GPT-J
- **Security**: Spring Security
- **Build Tool**: Gradle
- **Containerization**: Docker, Docker Compose

## Local Development Setup

This project supports both containerized and local development environments.

### Running with Docker Compose

#### Production-like Environment

To run the application in a production-like environment:

```bash
docker-compose up -d
```

This will start all services with the default configuration, which uses service names for inter-service communication.

#### Local Development Environment

For local development, use the local configuration:

```bash
docker-compose -f docker-compose.yml up -d
```

This will start all services with the local configuration, which uses `localhost` for inter-service communication.

### Running Services Individually

If you want to run some services in Docker and others locally, follow these steps:

1. Start the infrastructure services:

```bash
docker-compose up -d postgres zookeeper kafka gptj
```

2. Run the application services locally with the `local` profile:

```bash
# For the common module
./gradlew :common:bootRun --args='--spring.profiles.active=local'

# For the AI module
./gradlew :ai:bootRun --args='--spring.profiles.active=local'
```

### Configuration Profiles

- **Default Profile**: Uses service names for inter-service communication (e.g., `kafka:9092`, `postgres:5432`). This is suitable for running everything in Docker.
- **Local Profile**: Uses `localhost` with appropriate ports for inter-service communication (e.g., `localhost:9092`, `localhost:5434`). This is suitable for running application services locally while infrastructure services run in Docker.

### Available Services

- **Common Module**: The main application service
  - Default URL: http://localhost:8081
- **AI Module**: The AI processing service
  - Default URL: http://localhost:8083
- **PostgreSQL**: Database service
  - Default URL: jdbc:postgresql://localhost:5434/interview_prep
- **Kafka**: Message broker service
  - Default URL: localhost:9092
- **GPT-J**: Local AI service
  - Default URL: http://localhost:8086

## Building the Project

To build all modules:

```bash
./gradlew build
```

To build a specific module:

```bash
./gradlew :common:build
./gradlew :ai:build
```

## Usage Examples

- See also: SJSON Schema, ArgsBinder and PromptEngine
  guide: [SJSON_SCHEMA_AND_PROMPT_ENGINE_GUIDE.md](./SJSON_SCHEMA_AND_PROMPT_ENGINE_GUIDE.md)

### Generating Interview Questions

1. Navigate to the Tags section
2. Select a technical topic (e.g., "Java", "Spring", "Algorithms")
3. Click "Generate Questions"
4. Specify the difficulty level and number of questions
5. The AI will generate relevant interview questions

### Practicing Questions

1. Navigate to the Questions section
2. Select a question to practice
3. Write your answer in the provided text area
4. Submit your answer for AI feedback
5. Review the AI's evaluation and suggestions

### Tracking Job Applications

1. Navigate to the Vacancies section
2. Add a new job application with company, position, and status
3. Track the interview stages and outcomes
4. Add notes and prepare for specific company interviews

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
