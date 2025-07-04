package com.vladte.devhack.ai.service.kafka;

import com.vladte.devhack.infra.model.KafkaMessage;

public interface KafkaProducerService {
    void sendQuestionGenerateResult(KafkaMessage message);

    void sendAnswerFeedbackResult(KafkaMessage message);

    void sendParsedVacancyResult(KafkaMessage message);
}