package com.vladte.devhack.domain.service.ai;

import com.vladte.devhack.domain.entities.global.ai.AiPromptCategory;
import com.vladte.devhack.domain.service.CrudService;

import java.util.Optional;
import java.util.UUID;

public interface AiPromptCategoryService extends CrudService<AiPromptCategory, UUID> {

    Optional<AiPromptCategory> findByCode(String code);

}
