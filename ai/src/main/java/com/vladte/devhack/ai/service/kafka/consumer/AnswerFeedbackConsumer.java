package com.vladte.devhack.ai.service.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.ai.service.api.OpenAiService;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.payload.request.AnswerCheckRequestPayload;
import com.vladte.devhack.infra.model.payload.response.AnswerCheckResponsePayload;
import com.vladte.devhack.infra.service.kafka.producer.publish.KafkaResponsePublisher;
import com.vladte.devhack.infra.topics.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for consuming answer feedback request messages.
 */
@Service
public class AnswerFeedbackConsumer extends KafkaAiRequestConsumer<AnswerCheckRequestPayload, AnswerCheckResponsePayload> {

    private static final Logger log = LoggerFactory.getLogger(AnswerFeedbackConsumer.class);
    private final OpenAiService openAiService;

    public AnswerFeedbackConsumer(@Qualifier("AnswerKafkaProvider") KafkaResponsePublisher<AnswerCheckResponsePayload> responsePublisher,
                                  OpenAiService aiService,
                                  ObjectMapper objectMapper) {
        super(responsePublisher, objectMapper, AnswerCheckRequestPayload.class);
        this.openAiService = aiService;
    }

    @KafkaListener(topics = Topics.ANSWER_FEEDBACK_REQUEST, groupId = "${spring.kafka.consumer.group-id}", concurrency = "2")
    protected void listen(KafkaMessage<AnswerCheckRequestPayload> message) {
        processMessage(message);
    }

    @Override
    protected AnswerCheckResponsePayload performAiRequest(KafkaMessage<AnswerCheckRequestPayload> message) {
        AnswerCheckRequestPayload payload = message.getPayload();

        if (!isValidPayload(payload)) {
            log.error("Invalid payload received: null arguments");
            return AnswerCheckResponsePayload.error("Invalid payload format");
        }

        try {
            return switch (MessageTypes.fromValue(message.getType())) {
                case MessageTypes.CHECK_ANSWER_FOR_CHEATING -> handleCheatingCheck(payload);
                case MessageTypes.CHECK_ANSWER_WITH_FEEDBACK -> handleAnswerFeedback(payload);
                default -> {
                    log.error("Unknown message type: {}", message.getType());
                    yield AnswerCheckResponsePayload.error("Unknown message type: " + message.getType());
                }
            };
        } catch (Exception e) {
            log.error("Error processing message: {}", message.getId(), e);
            return AnswerCheckResponsePayload.error("Internal error: " + e.getMessage());
        }
    }

    private boolean isValidPayload(AnswerCheckRequestPayload payload) {
        return payload != null &&
                payload.getArguments() != null &&
                !payload.getArguments().necessaryArgumentsAreEmpty();
    }

    private AnswerCheckResponsePayload handleCheatingCheck(AnswerCheckRequestPayload payload) {
        log.debug("Handling CHECK_ANSWER_FOR_CHEATING message");
        Boolean isCheating = openAiService.checkAnswerForCheatingAsync(payload).join();
        return AnswerCheckResponsePayload.fromCheatingResult(isCheating);
    }

    private AnswerCheckResponsePayload handleAnswerFeedback(AnswerCheckRequestPayload payload) {
        log.debug("Handling CHECK_ANSWER_WITH_FEEDBACK message");
        Map<String, Object> result = openAiService.checkAnswerWithFeedbackAsync(payload).join();
        return AnswerCheckResponsePayload.fromScoreAndFeedback(result);
    }


    @Override
    protected AnswerCheckResponsePayload createErrorResponse(String message) {
        return null;
    }
}
