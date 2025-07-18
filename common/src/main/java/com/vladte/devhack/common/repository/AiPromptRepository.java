package com.vladte.devhack.common.repository;

import com.vladte.devhack.entities.AiPrompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiPromptRepository extends JpaRepository<AiPrompt, UUID> {
    Optional<AiPrompt> findByCode(String code);

    List<AiPrompt> findByCategoryId(UUID categoryId);

    List<AiPrompt> findByActiveTrue();
}
