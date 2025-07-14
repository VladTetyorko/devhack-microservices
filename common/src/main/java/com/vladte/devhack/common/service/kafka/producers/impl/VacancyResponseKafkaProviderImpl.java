package com.vladte.devhack.common.service.kafka.producers.impl;

import com.vladte.devhack.common.service.kafka.KafkaProducerService;
import com.vladte.devhack.common.service.kafka.producers.VacancyResponseKafkaProvider;
import com.vladte.devhack.entities.User;
import com.vladte.devhack.infra.message.MessageDestinations;
import com.vladte.devhack.infra.message.MessageSources;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VacancyResponseKafkaProviderImpl implements VacancyResponseKafkaProvider {

    private static final Logger logger = LoggerFactory.getLogger(VacancyResponseKafkaProviderImpl.class);
    private final KafkaProducerService kafkaProducerService;
    private final Map<String, CompletableFuture<Map<String, Object>>> pendingRequests = new ConcurrentHashMap<>();


    public VacancyResponseKafkaProviderImpl(@Qualifier("MainKafkaProducerService") KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public CompletableFuture<SendResult<String, KafkaMessage>> parseVacancyResponse(
            String messageId, String vacancyText, User user) {
        logger.info("Sending request to check answer with AI with ID: {}", messageId);

        // Create the Kafka message with the specified ID
        KafkaMessage message = new KafkaMessage(
                messageId,
                MessageSources.MAIN_APP,
                MessageDestinations.AI_APP,
                MessageTypes.VACANCY_PARSING.getValue(),
                vacancyText,
                java.time.LocalDateTime.now()
        );

        // Store the message and user in the pending requests map
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("user", user);
        requestData.put("vacancyText", vacancyText);
        future.complete(requestData);
        pendingRequests.put(messageId, future);

        return kafkaProducerService.sendMessage(message);
    }

    @Override
    public Map<String, Object> getPendingRequest(String messageId) {
        CompletableFuture<Map<String, Object>> future = pendingRequests.get(messageId);
        if (future != null) {
            try {
                return future.getNow(null);
            } finally {
                pendingRequests.remove(messageId);
            }
        }
        return null;
    }
}
