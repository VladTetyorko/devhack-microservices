package com.vladte.devhack.infra.config;

import com.vladte.devhack.infra.topics.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Centralized Kafka configuration for all modules.
 * This class defines all Kafka topics used for communication between modules.
 */
@Configuration
public class KafkaConfig {


    /**
     * Creates the topic for question generation requests.
     */
    @Bean
    public NewTopic questionGenerateRequestTopic() {
        return TopicBuilder.name(Topics.QUESTION_GENERATE_REQUEST)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Creates the topic for question generation results.
     */
    @Bean
    public NewTopic questionGenerateResultTopic() {
        return TopicBuilder.name(Topics.QUESTION_GENERATE_RESULT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Creates the topic for answer feedback requests.
     */
    @Bean
    public NewTopic answerFeedbackRequestTopic() {
        return TopicBuilder.name(Topics.ANSWER_FEEDBACK_REQUEST)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Creates the topic for answer feedback results.
     */
    @Bean
    public NewTopic answerFeedbackResultTopic() {
        return TopicBuilder.name(Topics.ANSWER_FEEDBACK_RESULT)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
