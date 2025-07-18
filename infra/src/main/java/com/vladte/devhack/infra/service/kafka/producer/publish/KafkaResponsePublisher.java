package com.vladte.devhack.infra.service.kafka.producer.publish;

import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.payload.ResponsePayload;
import com.vladte.devhack.infra.service.kafka.KafkaMessageSender;
import org.springframework.kafka.core.KafkaTemplate;


public abstract class KafkaResponsePublisher<T extends ResponsePayload<?>> extends KafkaMessageSender<T> {

    protected KafkaResponsePublisher(KafkaTemplate<String, KafkaMessage<T>> kafkaTemplate) {
        super(kafkaTemplate);
    }
}
