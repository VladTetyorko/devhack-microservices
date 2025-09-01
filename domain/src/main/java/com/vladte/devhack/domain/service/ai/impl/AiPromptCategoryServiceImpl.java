package com.vladte.devhack.domain.service.ai.impl;

import com.vladte.devhack.domain.entities.global.ai.AiPromptCategory;
import com.vladte.devhack.domain.repository.ai.AiPromptCategoryRepository;
import com.vladte.devhack.domain.service.ai.AiPromptCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<AiPromptCategory> findAll(Pageable pageable) {
        return repository.findAll(pageable);
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

    @Override
    public Optional<AiPromptCategory> findByCode(String code) {
        return repository.findByCode(code);
    }
}
