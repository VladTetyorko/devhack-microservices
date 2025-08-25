package com.vladte.devhack.common.service.domain.ai;

import com.vladte.devhack.common.service.domain.CrudService;
import com.vladte.devhack.entities.global.ai.AiPromptCategory;

import java.util.Optional;
import java.util.UUID;

public interface AiPromptCategoryService extends CrudService<AiPromptCategory, UUID> {

    Optional<AiPromptCategory> findByCode(String code);

}
