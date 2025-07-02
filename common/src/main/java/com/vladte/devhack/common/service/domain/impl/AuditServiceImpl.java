package com.vladte.devhack.common.service.domain.impl;

import com.vladte.devhack.common.repository.AuditRepository;
import com.vladte.devhack.common.service.domain.AuditService;
import com.vladte.devhack.entities.Audit;
import com.vladte.devhack.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of the AuditService interface.
 */
@Service
public class AuditServiceImpl extends BaseServiceImpl<Audit, UUID, AuditRepository> implements AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    /**
     * Constructor with repository injection.
     * Note: We don't inject AuditUtil here to avoid circular dependencies,
     * since AuditUtil depends on AuditService.
     *
     * @param repository the Audit repository
     */
    public AuditServiceImpl(AuditRepository repository) {
        super(repository);
        // We don't need to audit the audit service operations
    }

    @Override
    public Audit auditCreate(String entityType, String entityId, User user, String details) {
        logger.debug("Creating audit record for CREATE operation on entity: {}, ID: {}, user: {}",
                entityType, entityId, user != null ? user.getName() : "null");
        return createAudit(Audit.OperationType.CREATE, entityType, entityId, user, details);
    }

    @Override
    public Audit auditRead(String entityType, String entityId, User user, String details) {
        logger.debug("Creating audit record for READ operation on entity: {}, ID: {}, user: {}",
                entityType, entityId, user != null ? user.getName() : "null");
        return createAudit(Audit.OperationType.READ, entityType, entityId, user, details);
    }

    @Override
    public Audit auditUpdate(String entityType, String entityId, User user, String details) {
        logger.debug("Creating audit record for UPDATE operation on entity: {}, ID: {}, user: {}",
                entityType, entityId, user != null ? user.getName() : "null");
        return createAudit(Audit.OperationType.UPDATE, entityType, entityId, user, details);
    }

    @Override
    public Audit auditDelete(String entityType, String entityId, User user, String details) {
        logger.debug("Creating audit record for DELETE operation on entity: {}, ID: {}, user: {}",
                entityType, entityId, user != null ? user.getName() : "null");
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
        logger.debug("Building audit record: operation={}, entity={}, ID={}",
                operationType, entityType, entityId);

        Audit audit = Audit.builder()
                .operationType(operationType)
                .entityType(entityType)
                .entityId(entityId)
                .user(user)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();

        logger.debug("Saving audit record to repository");
        Audit savedAudit = repository.save(audit);
        logger.debug("Audit record saved with ID: {}", savedAudit.getId());

        return savedAudit;
    }
}
