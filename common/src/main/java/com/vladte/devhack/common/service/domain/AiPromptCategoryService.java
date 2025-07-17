package com.vladte.devhack.common.service.domain;

import com.vladte.devhack.entities.AiPromptCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiPromptCategoryService {
    List<AiPromptCategory> findAll();

    Optional<AiPromptCategory> findById(UUID id);

    AiPromptCategory save(AiPromptCategory category);

    void deleteById(UUID id);
}
