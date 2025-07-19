package com.vladte.devhack.common.service.domain.ai;

import com.vladte.devhack.entities.AiPrompt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiPromptService {
    List<AiPrompt> findAll();

    Optional<AiPrompt> findById(UUID id);

    Optional<AiPrompt> findByCode(String code);

    List<AiPrompt> findByCategoryId(UUID categoryId);

    List<AiPrompt> findActive();

    AiPrompt save(AiPrompt prompt);

    void deleteById(UUID id);
}
