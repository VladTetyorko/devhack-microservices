package com.vladte.devhack.infra.service.kafka;

import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.payload.AiRequestPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for sending messages to Kafka topics.
 */
@Service("MainKafkaProducerService")
@Slf4j
public class KafkaProducerService<T extends AiRequestPayload<?>> {

    private final KafkaTemplate<String, KafkaMessage<T>> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, KafkaMessage<T>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends a message to a Kafka topic.
     *
     * @param message The message to send
     * @return A CompletableFuture that will be completed when the send operation completes
     */
    public CompletableFuture<SendResult<String, KafkaMessage<T>>> sendMessage(String topic, KafkaMessage<T> message) {
        log.debug("Sending message: {}", message);

        return kafkaTemplate.send(topic, message.getId(), message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Message sent successfully: {}", message.getId());
                    } else {
                        log.error("Failed to send message: {}", ex.getMessage());
                    }
                });
    }
}