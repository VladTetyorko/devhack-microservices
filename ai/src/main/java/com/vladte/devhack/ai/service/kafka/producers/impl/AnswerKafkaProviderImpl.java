package com.vladte.devhack.ai.service.kafka.producers.impl;

import com.vladte.devhack.ai.service.kafka.producers.KafkaResponseProvider;
import com.vladte.devhack.infra.message.MessageDestinations;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.payload.response.AnswerCheckResponsePayload;
import com.vladte.devhack.infra.service.kafka.producer.publish.KafkaResponsePublisher;
import com.vladte.devhack.infra.topics.Topics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Implementation of AnswerKafkaProvider for sending answer feedback results.
 */
@Service("AnswerKafkaProvider")
public class AnswerKafkaProviderImpl
        extends KafkaResponsePublisher<AnswerCheckResponsePayload>
        implements KafkaResponseProvider<AnswerCheckResponsePayload> {

    public AnswerKafkaProviderImpl(KafkaTemplate<String, KafkaMessage<AnswerCheckResponsePayload>> kafkaTemplate) {
        super(kafkaTemplate);
    }

    @Override
    protected String getTopic() {
        return Topics.ANSWER_FEEDBACK_RESULT;
    }

    @Override
    protected String getMessageType() {
        return MessageTypes.CHECK_ANSWER_RESULT.getValue();
    }

    @Override
    protected String getSource() {
        return MessageDestinations.AI_APP;
    }

    @Override
    protected String getDestination() {
        return MessageDestinations.MAIN_APP;
    }
}
