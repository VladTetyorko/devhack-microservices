package com.vladte.devhack.common.service.kafka.producers;

import com.vladte.devhack.entities.User;
import com.vladte.devhack.infra.model.KafkaMessage;
import org.springframework.kafka.support.SendResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface VacancyResponseKafkaProvider {

    CompletableFuture<SendResult<String, KafkaMessage>> parseVacancyResponse(
            String messageId, String vacancyText, User user);

    Map<String, Object> getPendingRequest(String messageId);
}
