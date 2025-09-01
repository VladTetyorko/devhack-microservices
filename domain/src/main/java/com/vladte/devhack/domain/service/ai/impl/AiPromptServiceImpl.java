package com.vladte.devhack.domain.service.ai.impl;

import com.vladte.devhack.domain.entities.global.ai.AiPrompt;
import com.vladte.devhack.domain.entities.global.ai.AiPromptCategory;
import com.vladte.devhack.domain.repository.ai.AiPromptRepository;
import com.vladte.devhack.domain.service.ai.AiPromptService;
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
public class AiPromptServiceImpl implements AiPromptService {
    private final AiPromptRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<AiPrompt> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<AiPrompt> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AiPrompt> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AiPrompt> findByKey(String key) {
        return repository.findByKey(key);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiPrompt> findByCategoryId(UUID categoryId) {
        return repository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiPrompt> findEnabled() {
        return repository.findByEnabledTrue();
    }

    @Override
    public Optional<AiPrompt> findLatestByCategory(AiPromptCategory category) {
        return repository.findLatestByCategoryId(category.getId());
    }

    @Override
    @Transactional
    public AiPrompt save(AiPrompt prompt) {
        return repository.save(prompt);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
