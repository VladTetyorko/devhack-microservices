package com.vladte.devhack.common.service.kafka.concumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.service.kafka.util.PendingRequestManager;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.response.QuestionGenerateResponseArguments;
import com.vladte.devhack.infra.model.payload.response.QuestionGenerateResponsePayload;
import com.vladte.devhack.infra.topics.Topics;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Service for consuming question generation responses from the AI module via Kafka.
 */
@Service
public class QuestionKafkaConsumer
        extends AbstractMainConsumer<QuestionGenerateResponseArguments, QuestionGenerateResponsePayload> {

    public QuestionKafkaConsumer(ObjectMapper objectMapper,
                                 @Qualifier("questionGeneratePendingRequestManager") PendingRequestManager<QuestionGenerateResponseArguments> pendingRequestManager) {
        super(objectMapper, QuestionGenerateResponsePayload.class, pendingRequestManager);
    }

    /**
     * Consumes messages from the AI module.
     *
     * @param message The message received from the AI module
     */
    @Override
    @KafkaListener(topics = Topics.QUESTION_GENERATE_RESULT, groupId = "${spring.kafka.consumer.group-id}")
    public void listen(KafkaMessage<QuestionGenerateResponsePayload> message) {
        processMessage(message);
    }

    @Override
    protected boolean isExpectedResponse(KafkaMessage<QuestionGenerateResponsePayload> message) {
        return MessageTypes.QUESTION_GENERATE_RESULT.getValue().equals(message.getType());
    }
}
