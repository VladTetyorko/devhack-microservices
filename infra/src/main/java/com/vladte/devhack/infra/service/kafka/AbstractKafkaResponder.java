package com.vladte.devhack.infra.service.kafka;

import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.payload.AiResponsePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;


public abstract class AbstractKafkaResponder<T extends AiResponsePayload<?>> {

    private final KafkaTemplate<String, KafkaMessage<T>> kafkaTemplate;
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected AbstractKafkaResponder(KafkaTemplate<String, KafkaMessage<T>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    protected abstract String getTopic();

    protected abstract String getMessageType();

    protected abstract String getSource();

    protected abstract String getDestination();

    public void sendResponse(String messageId, T payload) {
        KafkaMessage<T> message = KafkaMessage.<T>builder()
                .id(messageId)
                .source(getSource())
                .destination(getDestination())
                .type(getMessageType())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Sending Kafka message to topic '{}': {}", getTopic(), message);

        kafkaTemplate.send(getTopic(), messageId, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send message", ex);
                    } else {
                        log.info("Successfully sent message with ID {}", messageId);
                    }
                });
    }
}





