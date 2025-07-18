package com.vladte.devhack.common.repository;

import com.vladte.devhack.entities.AiPromptCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AiPromptCategoryRepository extends JpaRepository<AiPromptCategory, UUID> {
}
