package com.vladte.devhack.ai.service.kafka.producers.impl;

import com.vladte.devhack.ai.service.kafka.producers.QuestionKafkaProvider;
import com.vladte.devhack.infra.message.MessageDestinations;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.payload.response.QuestionGenerateResponsePayload;
import com.vladte.devhack.infra.service.kafka.AbstractKafkaResponder;
import com.vladte.devhack.infra.topics.Topics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Implementation of QuestionKafkaProvider for sending question generation results.
 */
@Service("QuestionKafkaProvider")
public class QuestionKafkaProviderImpl extends AbstractKafkaResponder<QuestionGenerateResponsePayload> implements QuestionKafkaProvider {


    public QuestionKafkaProviderImpl(KafkaTemplate<String, KafkaMessage<QuestionGenerateResponsePayload>> kafkaTemplate) {
        super(kafkaTemplate);
    }

    @Override
    protected String getTopic() {
        return Topics.QUESTION_GENERATE_RESULT;
    }

    @Override
    protected String getMessageType() {
        return MessageTypes.QUESTION_GENERATE_RESULT.getValue();
    }

    @Override
    protected String getSource() {
        return MessageDestinations.AI_APP;
    }

    @Override
    protected String getDestination() {
        return MessageDestinations.MAIN_APP;
    }

    /**
     * Sends a question generation result message.
     *
     * @param message The question generation result message to send
     */
    @Override
    public void sendQuestionGenerateResult(String messageId, QuestionGenerateResponsePayload payload) {
        super.sendResponse(messageId, payload);
    }
}
