package com.vladte.devhack.common.service.domain.ai.impl;

import com.vladte.devhack.common.repository.AiPromptUsageLogRepository;
import com.vladte.devhack.common.service.domain.ai.AiPromptUsageLogService;
import com.vladte.devhack.entities.AiPromptUsageLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiPromptUsageLogServiceImpl implements AiPromptUsageLogService {
    private final AiPromptUsageLogRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<AiPromptUsageLog> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AiPromptUsageLog> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiPromptUsageLog> findByUserId(UUID userId) {
        return repository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiPromptUsageLog> findByPromptId(UUID promptId) {
        return repository.findByPromptId(promptId);
    }

    @Override
    @Transactional
    public AiPromptUsageLog save(AiPromptUsageLog log) {
        return repository.save(log);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
