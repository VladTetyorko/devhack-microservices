package com.vladte.devhack.infra.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.infra.model.KafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic base class for Kafka consumers that processes raw messages.
 */
public abstract class AbstractConsumer<PayloadOfMessage> {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ObjectMapper objectMapper;
    private final Class<PayloadOfMessage> payloadClass;

    protected AbstractConsumer(ObjectMapper objectMapper, Class<PayloadOfMessage> payloadClass) {
        this.objectMapper = objectMapper;
        this.payloadClass = payloadClass;
    }


    protected abstract void listen(KafkaMessage<PayloadOfMessage> rawMessage);


    /**
     * Entry point to consume and convert raw message.
     */
    protected void processMessage(KafkaMessage<?> rawMessage) {
        log.debug("Received raw Kafka message: {}", rawMessage);
        try {
            KafkaMessage<PayloadOfMessage> typedMessage = convertMessage(rawMessage);
            log.debug("Converted to typed message: {}", typedMessage);
            handleMessage(typedMessage);
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", rawMessage.getId(), e);
            handleError(rawMessage, e);
        }
    }

    /**
     * Converts untyped message into a typed one.
     */
    protected KafkaMessage<PayloadOfMessage> convertMessage(KafkaMessage<?> rawMessage) {
        PayloadOfMessage typedPayload = objectMapper.convertValue(rawMessage.getPayload(), payloadClass);
        return KafkaMessage.<PayloadOfMessage>builder()
                .id(rawMessage.getId())
                .source(rawMessage.getSource())
                .destination(rawMessage.getDestination())
                .type(rawMessage.getType())
                .timestamp(rawMessage.getTimestamp())
                .payload(typedPayload)
                .build();
    }

    /**
     * Method to be implemented by subclasses for handling typed message.
     */
    protected abstract void handleMessage(KafkaMessage<PayloadOfMessage> message);

    /**
     * Optional error handler.
     */
    protected void handleError(KafkaMessage<?> message, Exception error) {
        log.error("Failed to process message {}: {}", message.getId(), error.getMessage(), error);
    }
}
