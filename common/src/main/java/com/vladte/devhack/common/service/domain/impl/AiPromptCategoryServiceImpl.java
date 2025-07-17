package com.vladte.devhack.common.service.domain.impl;

import com.vladte.devhack.common.repository.AiPromptCategoryRepository;
import com.vladte.devhack.common.service.domain.AiPromptCategoryService;
import com.vladte.devhack.entities.AiPromptCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiPromptCategoryServiceImpl implements AiPromptCategoryService {
    private final AiPromptCategoryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<AiPromptCategory> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AiPromptCategory> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public AiPromptCategory save(AiPromptCategory category) {
        return repository.save(category);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
