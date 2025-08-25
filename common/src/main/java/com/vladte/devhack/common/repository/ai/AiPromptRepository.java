package com.vladte.devhack.common.repository.ai;

import com.vladte.devhack.entities.global.ai.AiPrompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiPromptRepository extends JpaRepository<AiPrompt, UUID> {
    Optional<AiPrompt> findByKey(String key);

    List<AiPrompt> findByCategoryId(UUID categoryId);

    List<AiPrompt> findByEnabledTrue();

    @Query(value = "SELECT * FROM ai_prompts p WHERE p.category_id = ?1 AND p.enabled = TRUE ORDER BY p.created_at DESC, p.version DESC", nativeQuery = true)
    Optional<AiPrompt> findLatestByCategoryId(UUID id);
}
