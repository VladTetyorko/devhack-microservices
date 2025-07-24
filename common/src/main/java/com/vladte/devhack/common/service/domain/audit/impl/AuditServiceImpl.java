package com.vladte.devhack.common.service.domain.audit.impl;

import com.vladte.devhack.common.repository.audit.AuditRepository;
import com.vladte.devhack.common.service.domain.CrudService;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.entities.global.Audit;
import com.vladte.devhack.entities.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of the AuditService interface.
 */
@Service
public class AuditServiceImpl implements AuditService, CrudService<Audit, UUID> {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditRepository repository;

    /**
     * Constructor with repository injection.
     * Note: We don't inject AuditUtil here to avoid circular dependencies,
     * since AuditUtil depends on AuditService.
     *
     * @param repository the Audit repository
     */
    public AuditServiceImpl(AuditRepository repository) {
        this.repository = repository;
    }

    @Override
    public Audit save(Audit entity) {
        return repository.save(entity);
    }

    @Override
    public List<Audit> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<Audit> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<Audit> findById(UUID uuid) {
        return repository.findById(uuid);
    }

    @Override
    public void deleteById(UUID uuid) {
        repository.deleteById(uuid);
    }

    @Override
    public Audit auditCreate(String entityType, String entityId, User user, String details) {
        log.debug("Creating audit record for CREATE operation on entity: {}, ID: {}, user: {}",
                entityType, entityId, user != null ? user.getProfile().getName() : "null");
        return createAudit(Audit.OperationType.CREATE, entityType, entityId, user, details);
    }

    @Override
    public Audit auditRead(String entityType, String entityId, User user, String details) {
        log.debug("Creating audit record for READ operation on entity: {}, ID: {}, user: {}",
                entityType, entityId, user != null ? user.getProfile().getName() : "null");
        return createAudit(Audit.OperationType.READ, entityType, entityId, user, details);
    }

    @Override
    public Audit auditUpdate(String entityType, String entityId, User user, String details) {
        log.debug("Creating audit record for UPDATE operation on entity: {}, ID: {}, user: {}",
                entityType, entityId, user != null ? user.getProfile().getName() : "null");
        return createAudit(Audit.OperationType.UPDATE, entityType, entityId, user, details);
    }

    @Override
    public Audit auditDelete(String entityType, String entityId, User user, String details) {
        log.debug("Creating audit record for DELETE operation on entity: {}, ID: {}, user: {}",
                entityType, entityId, user != null ? user.getProfile().getName() : "null");
        return createAudit(Audit.OperationType.DELETE, entityType, entityId, user, details);
    }

    /**
     * Helper method to create and save an audit record.
     *
     * @param operationType the type of operation
     * @param entityType    the type of entity
     * @param entityId      the ID of the entity
     * @param user          the user who performed the operation
     * @param details       additional details about the operation
     * @return the created audit record
     */
    private Audit createAudit(Audit.OperationType operationType, String entityType, String entityId, User user, String details) {
        log.debug("Building audit record: operation={}, entity={}, ID={}",
                operationType, entityType, entityId);

        Audit audit = Audit.builder()
                .operationType(operationType)
                .entityType(entityType)
                .entityId(entityId)
                .user(user)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();

        log.debug("Saving audit record to repository");
        Audit savedAudit = repository.save(audit);
        log.debug("Audit record saved with ID: {}", savedAudit.getId());

        return savedAudit;
    }

    public static void main(String[] args) {
        UUID currentId = UUID.randomUUID();
        Map<UUID, Integer> countMap = new HashMap<>();
        countMap.compute(currentId, (k, v) -> v == null ? 1 : v + 1);

    }

}
