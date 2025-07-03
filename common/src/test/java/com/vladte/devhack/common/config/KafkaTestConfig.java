package com.vladte.devhack.common.config;

import com.vladte.devhack.infra.topics.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Test configuration for Kafka components.
 * This configuration sets up an embedded Kafka broker for testing Kafka producers and consumers.
 */
@TestConfiguration
public class KafkaTestConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Creates a producer factory for testing Kafka producers.
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Creates a Kafka template for testing Kafka producers.
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Creates a consumer factory for testing Kafka consumers.
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.vladte.devhack.infra.model");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Creates a Kafka listener container factory for testing Kafka consumers.
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    /**
     * Creates the topic for question generation requests.
     */
    @Bean
    public NewTopic questionGenerateRequestTopic() {
        return TopicBuilder.name(Topics.QUESTION_GENERATE_REQUEST)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /**
     * Creates the topic for question generation results.
     */
    @Bean
    public NewTopic questionGenerateResultTopic() {
        return TopicBuilder.name(Topics.QUESTION_GENERATE_RESULT)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /**
     * Creates the topic for answer feedback requests.
     */
    @Bean
    public NewTopic answerFeedbackRequestTopic() {
        return TopicBuilder.name(Topics.ANSWER_FEEDBACK_REQUEST)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /**
     * Creates the topic for answer feedback results.
     */
    @Bean
    public NewTopic answerFeedbackResultTopic() {
        return TopicBuilder.name(Topics.ANSWER_FEEDBACK_RESULT)
                .partitions(1)
                .replicas(1)
                .build();
    }
}