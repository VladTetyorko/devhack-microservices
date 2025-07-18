package com.vladte.devhack.common.service.kafka.util;

import com.vladte.devhack.infra.model.arguments.response.AnswerCheckResponseArguments;
import com.vladte.devhack.infra.model.arguments.response.QuestionGenerateResponseArguments;
import com.vladte.devhack.infra.model.arguments.response.VacancyParseResultArguments;
import com.vladte.devhack.infra.service.kafka.PendingRequestManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaPendingManagerConfig {

    @Bean
    public PendingRequestManager<AnswerCheckResponseArguments> answerPendingRequestManager() {
        return new PendingRequestManager<>();
    }

    @Bean
    public PendingRequestManager<VacancyParseResultArguments> vacancyPendingRequestManager() {
        return new PendingRequestManager<>();
    }

    @Bean
    public PendingRequestManager<QuestionGenerateResponseArguments> questionGeneratePendingRequestManager() {
        return new PendingRequestManager<>();
    }
}
