package com.vladte.devhack.common.service.kafka.producers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.service.kafka.KafkaRequestSubscriber;
import com.vladte.devhack.common.service.kafka.producers.VacancyResponseKafkaProvider;
import com.vladte.devhack.common.util.JsonFieldExtractor;
import com.vladte.devhack.domain.entities.global.Vacancy;
import com.vladte.devhack.domain.entities.global.ai.AiPrompt;
import com.vladte.devhack.domain.entities.global.ai.AiPromptCategory;
import com.vladte.devhack.domain.service.ai.AiPromptCategoryService;
import com.vladte.devhack.domain.service.ai.AiPromptService;
import com.vladte.devhack.infra.message.MessageDestinations;
import com.vladte.devhack.infra.message.MessageTypes;
import com.vladte.devhack.infra.model.KafkaMessage;
import com.vladte.devhack.infra.model.arguments.response.VacancyParseResultArguments;
import com.vladte.devhack.infra.model.payload.request.AiRenderedRequestPayload;
import com.vladte.devhack.infra.service.kafka.PendingRequestManager;
import com.vladte.devhack.infra.topics.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class VacancyResponseKafkaProviderImpl
        extends KafkaRequestSubscriber<VacancyParseResultArguments>
        implements VacancyResponseKafkaProvider {

    private static final Logger log = LoggerFactory.getLogger(VacancyResponseKafkaProviderImpl.class);

    private final AiPromptService aiPromptService;
    private final AiPromptCategoryService aiPromptCategoryService;

    public VacancyResponseKafkaProviderImpl(KafkaTemplate<String, KafkaMessage<AiRenderedRequestPayload>> kafkaTemplate,
                                            @Qualifier("vacancyPendingRequestManager") PendingRequestManager<VacancyParseResultArguments> pendingRequestManager,
                                            ObjectMapper objectMapper,
                                            AiPromptService aiPromptService, AiPromptCategoryService aiPromptCategoryService) {
        super(kafkaTemplate, pendingRequestManager, objectMapper);
        this.aiPromptService = aiPromptService;
        this.aiPromptCategoryService = aiPromptCategoryService;
    }

    @Override
    protected String getTopic() {
        return Topics.VACANCY_PARSING_REQUEST;
    }

    @Override
    protected String getMessageType() {
        return MessageTypes.VACANCY_PARSING.getValue();
    }

    @Override
    protected String getSource() {
        return MessageDestinations.MAIN_APP;
    }

    @Override
    protected String getDestination() {
        return MessageDestinations.AI_APP;
    }

    @Override
    public CompletableFuture<VacancyParseResultArguments> parseVacancyResponse(
            String messageId, String vacancyText) {
        log.info("Sending vacancy parsing request with ID: {}", messageId);


        AiPromptCategory category = aiPromptCategoryService.findByCode(getTopic()).get();
        AiPrompt prompt = aiPromptService.findLatestByCategory(category).get();

        String fields = JsonFieldExtractor.parse(Vacancy.class);
        Map<String, Object> args = new HashMap<>();
        args.put("fields", fields);
        args.put("vacancyText", vacancyText);

        AiRenderedRequestPayload payload = super.buildAiMessagePayloadFromSources(prompt, args);
        return subscribeToResponse(messageId, payload);
    }
}
