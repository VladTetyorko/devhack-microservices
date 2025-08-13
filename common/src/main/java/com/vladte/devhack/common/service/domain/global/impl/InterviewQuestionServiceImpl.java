package com.vladte.devhack.common.service.domain.global.impl;

import com.vladte.devhack.common.repository.global.InterviewQuestionRepository;
import com.vladte.devhack.common.repository.global.specification.InterviewQuestionSpecification;
import com.vladte.devhack.common.service.domain.AuditableCrudService;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.common.service.domain.global.InterviewQuestionService;
import com.vladte.devhack.common.service.websocket.QuestionWebSocketService;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.global.Tag;
import com.vladte.devhack.entities.user.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the InterviewQuestionService interface.
 */
@Service
public class InterviewQuestionServiceImpl extends AuditableCrudService<InterviewQuestion, UUID, InterviewQuestionRepository> implements InterviewQuestionService {

    private final QuestionWebSocketService webSocketService;

    /**
     * Constructor with repository injection.
     *
     * @param repository the interview question repository
     * @param auditService the audit service
     * @param webSocketService the WebSocket service for broadcasting updates
     */
    public InterviewQuestionServiceImpl(InterviewQuestionRepository repository,
                                        AuditService auditService,
                                        QuestionWebSocketService webSocketService) {
        super(repository, auditService);
        this.webSocketService = webSocketService;
    }

    /**
     * Override save method to add WebSocket notification and cache eviction.
     */
    @Override
    @CacheEvict(value = {"questionCountByUser", "totalQuestionCount", "answeredQuestionsByUser"}, allEntries = true, cacheManager = "shortTermCacheManager")
    public InterviewQuestion save(InterviewQuestion entity) {
        boolean isNew = entity.getId() == null;
        InterviewQuestion savedEntity = super.save(entity);

        if (isNew) {
            webSocketService.broadcastQuestionCreated(savedEntity);
        } else {
            webSocketService.broadcastQuestionUpdated(savedEntity);
        }

        return savedEntity;
    }

    /**
     * Override audited save method to add WebSocket notification and cache eviction.
     */
    @Override
    @CacheEvict(value = {"questionCountByUser", "totalQuestionCount", "answeredQuestionsByUser"}, allEntries = true, cacheManager = "shortTermCacheManager")
    public InterviewQuestion save(InterviewQuestion entity, User user, String details) {
        boolean isNew = entity.getId() == null;
        InterviewQuestion savedEntity = super.save(entity, user, details);

        if (isNew) {
            webSocketService.broadcastQuestionCreated(savedEntity);
        } else {
            webSocketService.broadcastQuestionUpdated(savedEntity);
        }

        return savedEntity;
    }

    /**
     * Override deleteById method to add WebSocket notification and cache eviction.
     */
    @Override
    @CacheEvict(value = {"questionCountByUser", "totalQuestionCount", "answeredQuestionsByUser"}, allEntries = true, cacheManager = "shortTermCacheManager")
    public void deleteById(UUID id) {
        super.deleteById(id);
        webSocketService.broadcastQuestionDeleted(id);
    }

    /**
     * Override audited deleteById method to add WebSocket notification and cache eviction.
     */
    @Override
    @CacheEvict(value = {"questionCountByUser", "totalQuestionCount", "answeredQuestionsByUser"}, allEntries = true, cacheManager = "shortTermCacheManager")
    public void deleteById(UUID id, Class<?> entityClass, User user, String details) {
        super.deleteById(id, entityClass, user, details);
        webSocketService.broadcastQuestionDeleted(id);
    }

    @Override
    public List<InterviewQuestion> findQuestionsByTag(Tag tag) {
        return repository.findByTagsContaining(tag);
    }

    @Override
    public Page<InterviewQuestion> findQuestionsByTag(Tag tag, Pageable pageable) {
        return repository.findByTagsContaining(tag, pageable);
    }

    @Override
    public Page<InterviewQuestion> searchQuestions(String query, String difficulty, UUID tagId, Pageable pageable) {
        return repository.findAll(
                InterviewQuestionSpecification.searchQuestions(query, difficulty, tagId),
                pageable
        );
    }

    @Override
    @Cacheable(value = "questionCountByUser", key = "#user.id", cacheManager = "shortTermCacheManager")
    public int countQuestionsByUser(User user) {
        if (user == null) {
            return 0;
        }
        return repository.countInterviewQuestionsByUserId(user.getId());
    }

    @Override
    @Cacheable(value = "totalQuestionCount", key = "'all'", cacheManager = "mediumTermCacheManager")
    public int countAllQuestions() {
        return findAll().size();
    }

    @Override
    @Cacheable(value = "answeredQuestionsByUser", key = "#user.id", cacheManager = "shortTermCacheManager")
    public int findAnsweredQuestionsByUser(User user) {
        if (user == null) {
            return 0;
        }
        return repository.countQuestionsWithAnswerByUserId(user.getId());
    }


}
