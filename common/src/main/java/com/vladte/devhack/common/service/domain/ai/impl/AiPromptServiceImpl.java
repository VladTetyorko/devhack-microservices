package com.vladte.devhack.common.service.domain.ai.impl;

import com.vladte.devhack.common.repository.AiPromptRepository;
import com.vladte.devhack.common.service.domain.ai.AiPromptService;
import com.vladte.devhack.entities.AiPrompt;
import lombok.RequiredArgsConstructor;
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
    @Transactional(readOnly = true)
    public Optional<AiPrompt> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AiPrompt> findByCode(String code) {
        return repository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiPrompt> findByCategoryId(UUID categoryId) {
        return repository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiPrompt> findActive() {
        return repository.findByActiveTrue();
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
