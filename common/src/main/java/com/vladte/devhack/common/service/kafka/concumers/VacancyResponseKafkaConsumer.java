package com.vladte.devhack.common.service.kafka.concumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.service.domain.VacancyResponseService;
import com.vladte.devhack.common.service.kafka.producers.VacancyResponseKafkaProvider;
import com.vladte.devhack.entities.User;
import com.vladte.devhack.entities.VacancyResponse;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.topics.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VacancyResponseKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(VacancyResponseKafkaConsumer.class);
    private final ObjectMapper objectMapper;
    private final VacancyResponseService vacancyResponseService;
    private final VacancyResponseKafkaProvider vacancyResponseKafkaProvider;

    @Autowired
    public VacancyResponseKafkaConsumer(VacancyResponseService vacancyResponseService, VacancyResponseKafkaProvider vacancyResponseKafkaProvider) {
        this.vacancyResponseService = vacancyResponseService;
        this.vacancyResponseKafkaProvider = vacancyResponseKafkaProvider;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = Topics.VACANCY_PARSING_RESULT, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAnswerFeedbackResult(KafkaMessage message) {
        logger.info("Received parsed vacancy response {}", message);
        logger.debug("Processing vacancy parsing result from message ID: {}, type: {}", message.getId(), message.getType());

        try {
            VacancyResponse vacancyResponse = objectMapper.readValue(
                    message.getPayload(),
                    VacancyResponse.class
            );
            Map<String, Object> mapOfPendingRequest = vacancyResponseKafkaProvider.getPendingRequest(message.getId());
            User user = (User) mapOfPendingRequest.get("user");
            vacancyResponse.setUser(user);
            vacancyResponseService.save(vacancyResponse);
            logger.info("Successfully processed and saved vacancy response with ID: {}", vacancyResponse.getId());
        } catch (Exception e) {
            logger.error("Error processing vacancy response message: {}", message.getId(), e);
        }
    }
}
