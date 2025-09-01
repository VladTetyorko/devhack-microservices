package com.vladte.devhack.domain.repository.ai;

import com.vladte.devhack.domain.entities.global.ai.AiPromptCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiPromptCategoryRepository extends JpaRepository<AiPromptCategory, UUID> {
    Optional<AiPromptCategory> findByCode(String code);
}
