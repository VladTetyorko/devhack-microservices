package com.vladte.devhack.common.service.kafka.producers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.service.domain.ai.AiPromptCategoryService;
import com.vladte.devhack.common.service.domain.ai.AiPromptService;
import com.vladte.devhack.common.service.kafka.producers.QuestionKafkaProvider;
import com.vladte.devhack.entities.global.ai.AiPrompt;
import com.vladte.devhack.entities.global.ai.AiPromptCategory;
import com.vladte.devhack.infra.message.MessageDestinations;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.response.QuestionGenerateResponseArguments;
import com.vladte.devhack.infra.model.payload.request.AiRenderedRequestPayload;
import com.vladte.devhack.infra.service.kafka.PendingRequestManager;
import com.vladte.devhack.infra.service.kafka.producer.subscribe.KafkaRequestSubscriber;
import com.vladte.devhack.infra.topics.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for sending question generation requests to the AI module via Kafka.
 */
@Service
public class QuestionKafkaProviderImpl
        extends KafkaRequestSubscriber<QuestionGenerateResponseArguments>
        implements QuestionKafkaProvider {

    private static final Logger log = LoggerFactory.getLogger(QuestionKafkaProviderImpl.class);

    private final AiPromptService aiPromptService;
    private final AiPromptCategoryService aiPromptCategoryService;

    public QuestionKafkaProviderImpl(KafkaTemplate<String, KafkaMessage<AiRenderedRequestPayload>> kafkaTemplate,
                                     @Qualifier("questionGeneratePendingRequestManager") PendingRequestManager<QuestionGenerateResponseArguments> pendingRequestManager,
                                     ObjectMapper objectMapper,
                                     AiPromptService aiPromptService, AiPromptCategoryService aiPromptCategoryService) {
        super(kafkaTemplate, pendingRequestManager, objectMapper);
        this.aiPromptService = aiPromptService;
        this.aiPromptCategoryService = aiPromptCategoryService;
    }

    @Override
    protected String getTopic() {
        return Topics.QUESTION_GENERATE_REQUEST;
    }

    @Override
    protected String getMessageType() {
        return MessageTypes.QUESTION_GENERATE.getValue();
    }

    @Override
    protected String getSource() {
        return MessageDestinations.MAIN_APP;
    }

    @Override
    protected String getDestination() {
        return MessageDestinations.AI_APP;
    }

    /**
     * Sends a request to generate questions for a specific tag, using a pre-generated message ID.
     */
    public CompletableFuture<QuestionGenerateResponseArguments> subscribeToQuestionGeneration(
            String messageId, String tagName, int count, String difficulty) {
        log.info("Sending request to generate {} {} difficulty questions for tag: {} with ID: {}",
                count, difficulty, tagName, messageId);

        AiPromptCategory category = aiPromptCategoryService.findByCode(getTopic()).get();
        AiPrompt prompt = aiPromptService.findLatestByCategory(category).get();


        Map<String, Object> args = new HashMap<>();
        args.put("tag", tagName);
        args.put("count", count);
        args.put("difficulty", difficulty);

        AiRenderedRequestPayload payload = super.buildAiMessagePayloadFromSources(prompt, args);
        return subscribeToResponse(messageId, payload);
    }
}
