package com.vladte.devhack.ai.service.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.ai.service.api.OpenAiService;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.payload.request.AiRenderedRequestPayload;
import com.vladte.devhack.infra.model.payload.response.VacancyParseResponsePayload;
import com.vladte.devhack.infra.service.kafka.producer.publish.KafkaResponsePublisher;
import com.vladte.devhack.infra.topics.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for consuming vacancy parsing request messages.
 */
@Service
public class VacancyParsingConsumer extends KafkaAiRequestConsumer<AiRenderedRequestPayload, VacancyParseResponsePayload> {

    private static final Logger log = LoggerFactory.getLogger(VacancyParsingConsumer.class);
    private final OpenAiService openAiService;

    public VacancyParsingConsumer(@Qualifier("VacancyKafkaProvider") KafkaResponsePublisher<VacancyParseResponsePayload> responsePublisher,
                                  OpenAiService aiService,
                                  ObjectMapper objectMapper) {
        super(responsePublisher, objectMapper, AiRenderedRequestPayload.class);
        this.openAiService = aiService;
    }

    @KafkaListener(
            topics = Topics.VACANCY_PARSING_REQUEST,
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "2"
    )
    public void listen(KafkaMessage<AiRenderedRequestPayload> message) {
        processMessage(message);
    }

    @Override
    protected VacancyParseResponsePayload performAiRequest(KafkaMessage<AiRenderedRequestPayload> message) {
        AiRenderedRequestPayload payload = message.getPayload();

        if (payload == null || payload.getArguments() == null) {
            log.error("Invalid vacancy parsing request payload: null");
            return VacancyParseResponsePayload.error("Invalid payload format for vacancy parsing");
        }

        try {
            log.debug("Extracting vacancy model for incoming payload");
            Map<String, Object> result = openAiService.extractVacancyModelFromDescription(payload).join();

            if (Boolean.TRUE.equals(result.get("success"))) {
                return VacancyParseResponsePayload.fromJson((String) result.get("data"));
            } else {
                return VacancyParseResponsePayload.error((String) result.get("message"));
            }
        } catch (Exception e) {
            log.error("Exception while extracting vacancy model", e);
            return VacancyParseResponsePayload.error("Error extracting vacancy: " + e.getMessage());
        }
    }

    @Override
    protected VacancyParseResponsePayload createErrorResponse(String message) {
        return null;
    }
}
