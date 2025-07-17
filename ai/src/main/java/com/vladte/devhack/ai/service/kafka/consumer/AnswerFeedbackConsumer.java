package com.vladte.devhack.ai.service.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.ai.service.api.OpenAiService;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.payload.request.AnswerCheckRequestPayload;
import com.vladte.devhack.infra.model.payload.response.AnswerCheckResponsePayload;
import com.vladte.devhack.infra.service.kafka.AbstractKafkaResponder;
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
public class AnswerFeedbackConsumer extends AbstractAiConsumer<AnswerCheckRequestPayload, AnswerCheckResponsePayload> {

    private static final Logger log = LoggerFactory.getLogger(AnswerFeedbackConsumer.class);
    private final OpenAiService openAiService;


    public AnswerFeedbackConsumer(@Qualifier("AnswerKafkaProvider") AbstractKafkaResponder<AnswerCheckResponsePayload> responder,
                                  @Qualifier("gptJService") OpenAiService openAiService,
                                  ObjectMapper objectMapper) {
        super(responder, objectMapper, AnswerCheckRequestPayload.class);
        this.openAiService = openAiService;
    }

    @KafkaListener(topics = Topics.ANSWER_FEEDBACK_REQUEST, groupId = "${spring.kafka.consumer.group-id}", concurrency = "2")
    protected void listen(KafkaMessage<AnswerCheckRequestPayload> message) {
        processMessage(message);
    }

    @Override
    protected AnswerCheckResponsePayload handleRequest(KafkaMessage<AnswerCheckRequestPayload> message) {
        AnswerCheckRequestPayload payload = message.getPayload();
        if (payload == null || payload.getArguments() == null || payload.getArguments().necessaryArgumentsAreEmpty()) {
            log.error("Invalid payload received: null arguments");
            return AnswerCheckResponsePayload.error("Invalid payload format");
        }

        try {
            if (MessageTypes.CHECK_ANSWER_FOR_CHEATING.getValue().equals(message.getType())) {
                log.debug("Handling CHECK_ANSWER_FOR_CHEATING message");
                Boolean isCheating = openAiService.checkAnswerForCheatingAsync(payload).join();
                return AnswerCheckResponsePayload.fromCheatingResult(isCheating);

            } else if (MessageTypes.CHECK_ANSWER_WITH_FEEDBACK.getValue().equals(message.getType())) {
                log.debug("Handling CHECK_ANSWER_WITH_FEEDBACK message");
                Map<String, Object> result = openAiService.checkAnswerWithFeedbackAsync(payload).join();
                return AnswerCheckResponsePayload.fromScoreAndFeedback(result);
            } else {
                log.error("Unknown message type: {}", message.getType());
                return AnswerCheckResponsePayload.error("Unknown message type: " + message.getType());
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", message.getId(), e);
            return AnswerCheckResponsePayload.error("Internal error: " + e.getMessage());
        }
    }

    @Override
    protected AnswerCheckResponsePayload createErrorResponse(String message) {
        return null;
    }
}
