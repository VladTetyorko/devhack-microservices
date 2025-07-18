package com.vladte.devhack.infra.service.kafka;

import com.vladte.devhack.infra.model.KafkaMessage;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public abstract class KafkaMessageSender<T> {

    protected final KafkaTemplate<String, KafkaMessage<T>> kafkaTemplate;

    protected KafkaMessageSender(KafkaTemplate<String, KafkaMessage<T>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    protected abstract String getTopic();

    protected abstract String getMessageType();

    protected abstract String getSource();

    protected abstract String getDestination();

    public CompletableFuture<SendResult<String, KafkaMessage<T>>> buildAndSend(String messageId, T payload) {
        KafkaMessage<T> message = KafkaMessage.<T>builder()
                .id(messageId)
                .source(getSource())
                .destination(getDestination())
                .type(getMessageType())
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        return sendToKafka(getTopic(), message);
    }


    private CompletableFuture<SendResult<String, KafkaMessage<T>>> sendToKafka(String topic, KafkaMessage<T> message) {
        return kafkaTemplate.send(topic, message.getId(), message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        LoggerFactory.getLogger(getClass()).debug("Sent message: {}", message.getId());
                    } else {
                        LoggerFactory.getLogger(getClass()).error("Failed to send message: {}", ex.getMessage());
                    }
                });
    }
}
