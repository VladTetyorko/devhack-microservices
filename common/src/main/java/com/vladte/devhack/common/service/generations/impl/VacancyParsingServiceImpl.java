package com.vladte.devhack.common.service.generations.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladte.devhack.common.service.generations.VacancyParsingService;
import com.vladte.devhack.common.service.kafka.producers.VacancyResponseKafkaProvider;
import com.vladte.devhack.domain.entities.global.Vacancy;
import com.vladte.devhack.domain.entities.personalized.VacancyResponse;
import com.vladte.devhack.domain.entities.user.User;
import com.vladte.devhack.domain.service.global.VacancyService;
import com.vladte.devhack.domain.service.personalized.VacancyResponseService;
import com.vladte.devhack.infra.model.arguments.response.VacancyParseResultArguments;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class VacancyParsingServiceImpl implements VacancyParsingService {

    private final VacancyService vacancyService;
    private final VacancyResponseService vacancyResponseService;
    private final ObjectMapper objectMapper;
    private final VacancyResponseKafkaProvider vacancyResponseKafkaProvider;

    public VacancyParsingServiceImpl(VacancyService vacancyService, VacancyResponseService vacancyResponseService, ObjectMapper objectMapper, VacancyResponseKafkaProvider vacancyResponseKafkaProvider) {
        this.vacancyService = vacancyService;
        this.vacancyResponseService = vacancyResponseService;
        this.objectMapper = objectMapper;
        this.vacancyResponseKafkaProvider = vacancyResponseKafkaProvider;
    }

    @Override
    @Async
    public void parseVacancyText(String vacancyText, User user) {

        String messageId = UUID.randomUUID().toString();

        CompletableFuture<VacancyParseResultArguments> future = vacancyResponseKafkaProvider.parseVacancyResponse(messageId, vacancyText);

        VacancyParseResultArguments result = future.join();

        try {
            Vacancy vacancy = objectMapper.readValue(
                    result.getVacancyJson(),
                    Vacancy.class
            );
            Vacancy savedVacancy = vacancyService.save(vacancy);
            VacancyResponse vacancyResponse = vacancyResponseService.saveNewResponseForUserAndVacancy(user, savedVacancy);

            log.info("Successfully processed and saved vacancy response with ID: {}", vacancyResponse.getId());
        } catch (Exception e) {
            log.error("Error processing vacancy response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process vacancy response", e);
        }
    }
}
