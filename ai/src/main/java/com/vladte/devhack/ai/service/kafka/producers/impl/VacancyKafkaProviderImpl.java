package com.vladte.devhack.ai.service.kafka.producers.impl;

import com.vladte.devhack.ai.service.kafka.producers.VacancyKafkaProvider;
import com.vladte.devhack.infra.message.MessageDestinations;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.payload.response.VacancyParseResponsePayload;
import com.vladte.devhack.infra.service.kafka.AbstractKafkaResponder;
import com.vladte.devhack.infra.topics.Topics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Implementation of VacancyKafkaProvider for sending vacancy parsing results.
 */
@Service("VacancyKafkaProvider")
public class VacancyKafkaProviderImpl extends AbstractKafkaResponder<VacancyParseResponsePayload> implements VacancyKafkaProvider {

    public VacancyKafkaProviderImpl(KafkaTemplate<String, KafkaMessage<VacancyParseResponsePayload>> kafkaTemplate) {
        super(kafkaTemplate);
    }

    @Override
    protected String getTopic() {
        return Topics.VACANCY_PARSING_RESULT;
    }

    @Override
    protected String getMessageType() {
        return MessageTypes.VACANCY_PARSING_RESULT.getValue();
    }

    @Override
    protected String getSource() {
        return MessageDestinations.AI_APP;
    }

    @Override
    protected String getDestination() {
        return MessageDestinations.MAIN_APP;
    }

    @Override
    public void sendParsedVacancyResult(String messageId, VacancyParseResponsePayload payload) {
        super.sendResponse(messageId, payload);
    }
}
