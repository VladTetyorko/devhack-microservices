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
public class KafkaMessage<T> implements Serializable {
    private String id;
    private String source;
    private String destination;
    private String type;
    private T payload;
    private LocalDateTime timestamp;

}