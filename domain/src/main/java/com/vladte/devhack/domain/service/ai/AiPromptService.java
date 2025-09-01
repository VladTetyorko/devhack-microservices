package com.vladte.devhack.domain.service.ai;

import com.vladte.devhack.domain.entities.global.ai.AiPrompt;
import com.vladte.devhack.domain.entities.global.ai.AiPromptCategory;
import com.vladte.devhack.domain.service.CrudService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiPromptService extends CrudService<AiPrompt, UUID> {

    Optional<AiPrompt> findByKey(String key);

    List<AiPrompt> findByCategoryId(UUID categoryId);

    List<AiPrompt> findEnabled();

    Optional<AiPrompt> findLatestByCategory(AiPromptCategory category);

}
