package com.vladte.devhack.domain.repository.ai;

import com.vladte.devhack.domain.entities.global.ai.AiPromptUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiPromptUsageLogRepository extends JpaRepository<AiPromptUsageLog, UUID> {
    List<AiPromptUsageLog> findByUserId(UUID userId);

    List<AiPromptUsageLog> findByPromptId(UUID promptId);
}
