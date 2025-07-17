package com.vladte.devhack.ai.service.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.ai.service.api.OpenAiService;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.payload.request.QuestionGenerateRequestPayload;
import com.vladte.devhack.infra.model.payload.response.QuestionGenerateResponsePayload;
import com.vladte.devhack.infra.service.kafka.AbstractKafkaResponder;
import com.vladte.devhack.infra.topics.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Service for consuming question generation request messages.
 */
@Service
public class QuestionGenerateConsumer extends AbstractAiConsumer<QuestionGenerateRequestPayload, QuestionGenerateResponsePayload> {

    private static final Logger log = LoggerFactory.getLogger(QuestionGenerateConsumer.class);
    private final OpenAiService openAiService;

    public QuestionGenerateConsumer(@Qualifier("QuestionKafkaProvider") AbstractKafkaResponder<QuestionGenerateResponsePayload> responder,
                                    @Qualifier("gptJService") OpenAiService openAiService, ObjectMapper objectMapper) {
        super(responder, objectMapper, QuestionGenerateRequestPayload.class);
        this.openAiService = openAiService;
    }

    @KafkaListener(
            topics = Topics.QUESTION_GENERATE_REQUEST,
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "2"
    )
    public void listen(KafkaMessage<QuestionGenerateRequestPayload> message) {
        processMessage(message);
    }

    @Override
    protected QuestionGenerateResponsePayload handleRequest(KafkaMessage<QuestionGenerateRequestPayload> message) {
        QuestionGenerateRequestPayload payload = message.getPayload();
        if (payload == null || payload.getArguments() == null || payload.getArguments().necessaryArgumentsAreEmpty()) {
            log.error("Invalid payload received: null or empty arguments");
            return QuestionGenerateResponsePayload.error("Invalid payload format");
        }

        try {
            String result = openAiService.generateQuestionsForTagAsync(payload).join();
            return QuestionGenerateResponsePayload.fromGeneratedText(result);
        } catch (Exception e) {
            log.error("Error generating questions for message: {}", message.getId(), e);
            return QuestionGenerateResponsePayload.error("Internal error: " + e.getMessage());
        }
    }

    @Override
    protected QuestionGenerateResponsePayload createErrorResponse(String message) {
        return null;
    }
}
