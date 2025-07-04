package com.vladte.devhack.infra.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Generic message class for Kafka communication between modules.
 * This class is used by all modules for sending and receiving messages via Kafka.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaMessage implements Serializable {
    private String id;
    private String source;
    private String destination;
    private String type;
    private String payload;
    private LocalDateTime timestamp;

    /**
     * Factory method to create a new KafkaMessage with a random UUID and the current timestamp.
     *
     * @param source      the source module of the message
     * @param destination the destination module of the message
     * @param type        the type of the message
     * @param payload     the actual content of the message
     * @return a new KafkaMessage instance
     */
    public static KafkaMessage create(String source, String destination, String type, String payload) {
        return new KafkaMessage(
                java.util.UUID.randomUUID().toString(),
                source,
                destination,
                type,
                payload,
                LocalDateTime.now()
        );
    }
}