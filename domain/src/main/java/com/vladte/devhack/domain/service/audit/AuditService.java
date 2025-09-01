package com.vladte.devhack.domain.service.audit;

import com.vladte.devhack.domain.entities.global.Audit;
import com.vladte.devhack.domain.entities.user.User;
import com.vladte.devhack.domain.service.CrudService;

import java.util.UUID;

/**
 * Service interface for audit operations.
 * This interface follows the Single Responsibility Principle by focusing only on audit-related operations.
 */
public interface AuditService extends CrudService<Audit, UUID> {

    /**
     * Audit a create operation.
     *
     * @param entityType the type of entity that was created
     * @param entityId   the ID of the entity that was created
     * @param user       the user who performed the operation
     * @param details    additional details about the operation
     * @return the created audit record
     */
    Audit auditCreate(String entityType, String entityId, User user, String details);

    /**
     * Audit an update operation.
     *
     * @param entityType the type of entity that was updated
     * @param entityId   the ID of the entity that was updated
     * @param user       the user who performed the operation
     * @param details    additional details about the operation
     * @return the created audit record
     */
    Audit auditUpdate(String entityType, String entityId, User user, String details);

    /**
     * Audit a read operation.
     *
     * @param entityType the type of entity that was read
     * @param entityId   the ID of the entity that was read
     * @param user       the user who performed the operation
     * @param details    additional details about the operation
     * @return the created audit record
     */
    Audit auditRead(String entityType, String entityId, User user, String details);

    /**
     * Audit a delete operation.
     *
     * @param entityType the type of entity that was deleted
     * @param entityId   the ID of the entity that was deleted
     * @param user       the user who performed the operation
     * @param details    additional details about the operation
     * @return the created audit record
     */
    Audit auditDelete(String entityType, String entityId, User user, String details);
}
