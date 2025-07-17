package com.vladte.devhack.common.service.kafka.producers.impl;

import com.vladte.devhack.common.service.kafka.producers.AbstractMainKafkaProviderService;
import com.vladte.devhack.common.service.kafka.producers.VacancyResponseKafkaProvider;
import com.vladte.devhack.common.service.kafka.util.PendingRequestManager;
import com.vladte.devhack.common.util.JsonFieldExtractor;
import com.vladte.devhack.entities.Vacancy;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.arguments.request.VacancyParseFromTestRequestArguments;
import com.vladte.devhack.infra.model.arguments.response.VacancyParseResultArguments;
import com.vladte.devhack.infra.model.payload.request.VacancyParseRequestPayload;
import com.vladte.devhack.infra.service.kafka.KafkaProducerService;
import com.vladte.devhack.infra.topics.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class VacancyResponseKafkaProviderImpl
        extends AbstractMainKafkaProviderService<VacancyParseRequestPayload, VacancyParseResultArguments>
        implements VacancyResponseKafkaProvider {

    private static final Logger log = LoggerFactory.getLogger(VacancyResponseKafkaProviderImpl.class);

    private final static String VACANCY_TEXT_DESCRIPTION_PROMPT =
            """
                    You are a strict JSON generator.
                    
                    Input: A vacancy description.
                    
                    Your task:
                    - Extract data from the vacancy description.
                    - Return only a plain JSON object with these fields:
                    %s
                    
                    Rules:
                    - Output strictly valid JSON. No comments, explanations, or extra text.
                    - If a field is missing, output an empty string for that field.
                    - Include only the specified fields. Do not add any other fields or metadata.
                    - The output must be valid JSON and start with { and end with }.
                    - DONT ADD ANY EXPLANATIONS, ANY ADDITIONAL INFORMATION
                    - OUTPUT STARTS WITH { AND ENDS WITH }
                    - OUTPUT CONTAIN ONLY JSON OBJECT
                    - status should be "APPLIED" if not added another information, in uppercase as mentioned
                    - Straightly follow this rules, DONT add anything extra, dont break any rule and structure
                    
                    Vacancy Description:
                    %s
                    """;

    public VacancyResponseKafkaProviderImpl(@Qualifier("MainKafkaProducerService") KafkaProducerService<VacancyParseRequestPayload> kafkaProducerService, PendingRequestManager<VacancyParseResultArguments> pendingRequestManager) {
        super(kafkaProducerService, pendingRequestManager);
    }

    @Override
    public CompletableFuture<VacancyParseResultArguments> parseVacancyResponse(
            String messageId, String vacancyText) {
        log.info("Sending request to check answer with AI with ID: {}", messageId);

        VacancyParseFromTestRequestArguments arguments = new VacancyParseFromTestRequestArguments(
                JsonFieldExtractor.parse(Vacancy.class), vacancyText);

        VacancyParseRequestPayload payload = VacancyParseRequestPayload.builder()
                .prompt(String.format(VACANCY_TEXT_DESCRIPTION_PROMPT, JsonFieldExtractor.parse(Vacancy.class), vacancyText))
                .arguments(arguments)
                .language("en")
                .build();

        return sendRequest(messageId, payload);
    }


    @Override
    protected String getTopic() {
        return Topics.VACANCY_PARSING_REQUEST;
    }

    @Override
    protected String getMessageType() {
        return MessageTypes.VACANCY_PARSING.getValue();
    }

}
