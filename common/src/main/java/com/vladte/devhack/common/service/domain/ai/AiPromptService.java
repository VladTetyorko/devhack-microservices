package com.vladte.devhack.common.service.domain.ai;

import com.vladte.devhack.common.service.domain.CrudService;
import com.vladte.devhack.entities.global.ai.AiPrompt;
import com.vladte.devhack.entities.global.ai.AiPromptCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiPromptService extends CrudService<AiPrompt, UUID> {

    Optional<AiPrompt> findByKey(String key);

    List<AiPrompt> findByCategoryId(UUID categoryId);

    List<AiPrompt> findEnabled();

    Optional<AiPrompt> findLatestByCategory(AiPromptCategory category);

}
