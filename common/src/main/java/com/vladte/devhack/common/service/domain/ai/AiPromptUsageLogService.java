package com.vladte.devhack.common.service.domain.ai;

import com.vladte.devhack.entities.AiPromptUsageLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiPromptUsageLogService {
    List<AiPromptUsageLog> findAll();

    Optional<AiPromptUsageLog> findById(UUID id);

    List<AiPromptUsageLog> findByUserId(UUID userId);

    List<AiPromptUsageLog> findByPromptId(UUID promptId);

    AiPromptUsageLog save(AiPromptUsageLog log);

    void deleteById(UUID id);
}
