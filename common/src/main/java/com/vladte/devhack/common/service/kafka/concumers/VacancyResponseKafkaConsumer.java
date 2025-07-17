package com.vladte.devhack.common.service.kafka.concumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.service.kafka.util.PendingRequestManager;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.response.VacancyParseResultArguments;
import com.vladte.devhack.infra.model.payload.response.VacancyParseResponsePayload;
import com.vladte.devhack.infra.topics.Topics;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VacancyResponseKafkaConsumer
        extends AbstractMainConsumer<VacancyParseResultArguments, VacancyParseResponsePayload> {


    public VacancyResponseKafkaConsumer(ObjectMapper objectMapper,
                                        @Qualifier("vacancyPendingRequestManager") PendingRequestManager<VacancyParseResultArguments> pendingRequestManager) {
        super(objectMapper, VacancyParseResponsePayload.class, pendingRequestManager);
    }

    @Transactional
    @KafkaListener(topics = Topics.VACANCY_PARSING_RESULT, groupId = "${spring.kafka.consumer.group-id}")
    public void listen(KafkaMessage<VacancyParseResponsePayload> message) {
        processMessage(message);
    }


    @Override
    protected boolean isExpectedResponse(KafkaMessage<VacancyParseResponsePayload> message) {
        return MessageTypes.VACANCY_PARSING_RESULT.getValue().equals(message.getType());
    }

}
