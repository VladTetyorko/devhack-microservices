# AI Module Setup Guide

## Overview

The DevHack application uses a real AI integration through Kafka messaging to generate interview questions. The system
consists of two main components:

1. **Common Module** - Handles web requests and sends AI generation requests via Kafka
2. **AI Module** - Consumes Kafka messages and processes them using real AI services (OpenAI/GPT-J)

## Current Status

✅ **Angular Components** - Fully implemented with real API calls  
✅ **REST Controller** - Connected to real AI orchestration service  
✅ **Kafka Integration** - Properly configured for AI message processing  
✅ **AI Module Implementation** - Complete with OpenAI and GPT-J services  
❌ **AI Module Running** - Currently not started (needs manual startup)

## Starting the AI Module

To enable real AI question generation, you need to start the AI module:

### Method 1: Using Gradle (Recommended for Development)

```bash
cd /home/vladte/IdeaProjects/DevHack
./gradlew :ai:bootRun --args='--spring.profiles.active=local'
```

### Method 2: Using Background Process

```bash
cd /home/vladte/IdeaProjects/DevHack
nohup ./gradlew :ai:bootRun --args='--spring.profiles.active=local' > ai-module.log 2>&1 &
```

### Method 3: Using Docker (Production)

```bash
cd /home/vladte/IdeaProjects/DevHack
docker-compose up ai
```

## Configuration Requirements

### 1. Kafka Configuration

Ensure Kafka is running and accessible:

- Zookeeper: `localhost:2181`
- Kafka Broker: `localhost:9092`

### 2. AI Service Configuration

The AI module requires API keys for AI services:

**OpenAI Configuration:**

```yaml
openai:
  api-key: ${OPENAI_API_KEY:your-openai-api-key}
  base-url: https://api.openai.com/v1
  model: gpt-3.5-turbo
```

**GPT-J Configuration (Alternative):**

```yaml
gptj:
  base-url: ${GPTJ_BASE_URL:http://localhost:8080}
  enabled: ${GPTJ_ENABLED:false}
```

### 3. Environment Variables

Set the following environment variables:

```bash
export OPENAI_API_KEY="your-openai-api-key-here"
export SPRING_PROFILES_ACTIVE="local"
```

## Verification

### 1. Check if AI Module is Running

```bash
ps aux | grep "ai.*bootRun" | grep -v grep
```

### 2. Check Kafka Topics

```bash
kafka-topics --bootstrap-server localhost:9092 --list | grep question
```

### 3. Monitor AI Module Logs

```bash
tail -f ai-module.log
```

## How It Works

1. **User Request** → Angular component calls REST API
2. **REST API** → QuestionGenerationOrchestrationService sends Kafka message
3. **Kafka Message** → AI module QuestionGenerateConsumer receives request
4. **AI Processing** → OpenAiService generates questions using real AI
5. **Response** → AI module sends results back via Kafka
6. **Database** → Questions are saved and displayed to user

## Troubleshooting

### AI Module Not Starting

- Check Java version (requires Java 21)
- Verify Kafka is running
- Check for port conflicts
- Review application logs

### No Questions Generated

- Verify AI module is running
- Check Kafka connectivity
- Verify OpenAI API key is valid
- Monitor Kafka message flow

### Timeout Issues

- AI processing can take 30-60 seconds
- Check network connectivity to OpenAI
- Monitor system resources

## Current Implementation Status

The question generation system is **fully implemented** and ready for real AI processing. The Angular components
already:

- ✅ Call real REST APIs (`/api/questions/generate`)
- ✅ Handle asynchronous processing with proper loading states
- ✅ Display success/error messages
- ✅ Redirect users after successful generation
- ✅ Support multiple generation options (custom, auto, multi-tag)

**To enable real AI functionality, simply start the AI module as described above.**