# DevHack - Build and Run Guide

## Quick Start

### 1. Start Infrastructure

```bash
# Start required services (PostgreSQL, Kafka, Zookeeper)
docker-compose up -d postgres kafka zookeeper
```

### 2. Set Environment Variables (for AI module)

```bash
export OPENAI_API_KEY= key
export ai.service.provider="openai"
```

### 3. Build Project

```bash
# Build all modules
./gradlew build

# Or build specific modules
./gradlew :common:build :ai:build
```

### 4. Run Applications

**Terminal 1 - Common Module (Web UI - Port 8081):**

```bash
./gradlew :common:bootRun --args='--spring.profiles.active=local'
```

**Terminal 2 - AI Module (API - Port 8083):**

```bash
./gradlew :ai:bootRun --args='--spring.profiles.active=local'
```

## Access Points

- Common Module: http://localhost:8081
- AI Module: http://localhost:8083
- Health Checks: /actuator/health

## Module Details

- **Common**: `com.vladte.devhack.common.DevHackApplication`
- **AI**: `com.vladte.devhack.ai.AiApplication`

## Infrastructure Requirements

- PostgreSQL: localhost:5434 (database: interview_prep)
- Kafka: localhost:29092
- Optional LocalAI: localhost:8086

## System Requirements

The Docker services have the following memory limits configured:

- **PostgreSQL**: 512MB limit, 256MB reservation
- **Zookeeper**: 256MB limit, 128MB reservation
- **Kafka**: 1GB limit, 512MB reservation
- **LocalAI/GPT-J**: 4GB limit, 2GB reservation (only with "local" profile)
- **MinIO**: 512MB limit, 256MB reservation (only with "local" profile)

**Total Memory Requirements:**

- Core services (postgres, zookeeper, kafka): ~1.8GB
- With optional services (gptj, minio): ~6.3GB

Ensure your system has sufficient RAM available before starting all services.

## Alternative: Run with JAR files

```bash
./gradlew :common:bootJar :ai:bootJar
java -jar common/build/libs/common-0.0.1-SNAPSHOT.jar --spring.profiles.active=local &
java -jar ai/build/libs/ai-0.0.1-SNAPSHOT.jar --spring.profiles.active=local &
```