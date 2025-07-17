package com.vladte.devhack.common.service.kafka.concumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.service.kafka.util.PendingRequestManager;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.response.AnswerCheckResponseArguments;
import com.vladte.devhack.infra.model.payload.response.AnswerCheckResponsePayload;
import com.vladte.devhack.infra.topics.Topics;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Service for consuming answer check responses from the AI module via Kafka.
 */
@Service
public class AnswerKafkaConsumer
        extends AbstractMainConsumer<AnswerCheckResponseArguments, AnswerCheckResponsePayload> {

    public AnswerKafkaConsumer(ObjectMapper objectMapper,
                               @Qualifier("answerPendingRequestManager") PendingRequestManager<AnswerCheckResponseArguments> pendingRequestManager) {
        super(objectMapper, AnswerCheckResponsePayload.class, pendingRequestManager);
    }

    @Override
    @KafkaListener(topics = Topics.ANSWER_FEEDBACK_RESULT, groupId = "${spring.kafka.consumer.group-id}")
    public void listen(KafkaMessage<AnswerCheckResponsePayload> message) {
        processMessage(message);
    }

    @Override
    protected boolean isExpectedResponse(KafkaMessage<AnswerCheckResponsePayload> message) {
        return MessageTypes.CHECK_ANSWER_RESULT.getValue().equals(message.getType());
    }

}
