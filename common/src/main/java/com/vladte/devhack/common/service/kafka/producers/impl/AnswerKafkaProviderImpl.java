package com.vladte.devhack.common.service.kafka.producers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.service.kafka.KafkaRequestSubscriber;
import com.vladte.devhack.common.service.kafka.producers.AnswerKafkaProvider;
import com.vladte.devhack.domain.entities.global.InterviewQuestion;
import com.vladte.devhack.domain.entities.global.ai.AiPrompt;
import com.vladte.devhack.domain.entities.global.ai.AiPromptCategory;
import com.vladte.devhack.domain.entities.personalized.Answer;
import com.vladte.devhack.domain.service.ai.AiPromptCategoryService;
import com.vladte.devhack.domain.service.ai.AiPromptService;
import com.vladte.devhack.infra.message.MessageDestinations;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.response.AnswerCheckResponseArguments;
import com.vladte.devhack.infra.model.payload.request.AiRenderedRequestPayload;
import com.vladte.devhack.infra.service.kafka.PendingRequestManager;
import com.vladte.devhack.infra.topics.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for sending answer feedback requests to the AI module via Kafka.
 * Uses generic Kafka messages for scalable and type-safe communication.
 */
@Service
public class AnswerKafkaProviderImpl
        extends KafkaRequestSubscriber<AnswerCheckResponseArguments>
        implements AnswerKafkaProvider {

    private final AiPromptService aiPromptService;
    private final AiPromptCategoryService aiPromptCategoryService;

    private static final Logger log = LoggerFactory.getLogger(AnswerKafkaProviderImpl.class);

    public AnswerKafkaProviderImpl(KafkaTemplate<String, KafkaMessage<AiRenderedRequestPayload>> kafkaTemplate,
                                   @Qualifier("answerPendingRequestManager") PendingRequestManager<AnswerCheckResponseArguments> pendingRequestManager,
                                   ObjectMapper objectMapper,
                                   AiPromptService aiPromptService,
                                   AiPromptCategoryService aiPromptCategoryService) {
        super(kafkaTemplate, pendingRequestManager, objectMapper);
        this.aiPromptService = aiPromptService;
        this.aiPromptCategoryService = aiPromptCategoryService;
    }

    @Override
    protected String getTopic() {
        return Topics.ANSWER_FEEDBACK_REQUEST;
    }

    @Override
    protected String getMessageType() {
        return MessageTypes.CHECK_ANSWER_WITH_FEEDBACK.getValue();
    }

    @Override
    protected String getSource() {
        return MessageDestinations.MAIN_APP;
    }

    @Override
    protected String getDestination() {
        return MessageDestinations.AI_APP;
    }

    @Override
    public CompletableFuture<AnswerCheckResponseArguments> subscribeToAnswerCheatingCheck(
            String messageId, String questionText, String answerText) {

        log.info("Sending cheating check request [id={}]", messageId);
        AiPromptCategory category = aiPromptCategoryService.findByCode(getTopic()).get();
        AiPrompt prompt = aiPromptService.findLatestByCategory(category).get();

        AiRenderedRequestPayload payload = super.buildAiMessagePayloadFromSources(prompt, questionText, answerText);

        return subscribeToResponse(messageId, payload);
    }

    @Override
    public CompletableFuture<AnswerCheckResponseArguments> subscribeToAnswerFeedbackCheck(
            String messageId, InterviewQuestion question, Answer answer) {

        log.info("Sending feedback request [id={}]", messageId);
        AiPromptCategory category = aiPromptCategoryService.findByCode(getTopic()).get();
        AiPrompt prompt = aiPromptService.findLatestByCategory(category).get();

        AiRenderedRequestPayload payload = super.buildAiMessagePayloadFromSources(prompt, question, answer);

        return subscribeToResponse(messageId, payload);
    }

}
