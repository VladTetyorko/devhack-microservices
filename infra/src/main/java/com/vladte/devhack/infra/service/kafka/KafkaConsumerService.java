package com.vladte.devhack.infra.service.kafka;

import com.vladte.devhack.infra.model.KafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Base service for consuming messages from Kafka topics.
 * This class provides common functionality for all modules to consume Kafka messages.
 * <p>
 * Note: This is an abstract base class. Each module should extend this class
 * and implement the specific message handling logic.
 */
@Service
public abstract class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    /**
     * Process a message received from Kafka.
     * This method should be implemented by each module to handle messages according to their specific needs.
     *
     * @param message The message received from Kafka
     * @param source  The source of the message (main, domain, or ai)
     */
    protected void processMessage(KafkaMessage message, String source) {
        logger.info("Processing message from {}: {}", source, message);
        // This method should be overridden by subclasses to implement specific processing logic
    }

    /**
     * Log a received message.
     *
     * @param message The message received from Kafka
     * @param source  The source of the message
     */
    protected void logReceivedMessage(KafkaMessage message, String source) {
        logger.info("Received message from {}: {}", source, message);
    }
}