package com.vladte.devhack.domain.service;

import com.vladte.devhack.domain.entities.BasicEntity;
import com.vladte.devhack.domain.entities.user.User;
import com.vladte.devhack.domain.service.audit.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Base implementation of the BaseService interface.
 * Provides default implementations for common CRUD operations.
 *
 * @param <T>  the entity type, must extend BasicEntity
 * @param <ID> the entity ID type
 * @param <R>  the repository type
 */
public abstract class AuditableCrudService<T extends BasicEntity, ID, R extends JpaRepository<T, ID>> implements CrudService<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(AuditableCrudService.class);

    protected final R repository;
    protected final AuditService auditService;

    /**
     * Constructor with repository and auditService injection.
     *
     * @param repository   the JPA repository
     * @param auditService the audit service
     */
    protected AuditableCrudService(R repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }


    @Override
    public T save(T entity) {
        log.debug("Saving entity of type {}", entity.getClass().getSimpleName());
        if (entity.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        T savedEntity = repository.save(entity);
        log.debug("Entity saved with ID: {}", savedEntity.getId());
        return savedEntity;
    }

    /**
     * Save an entity and create an audit record.
     *
     * @param entity  the entity to save
     * @param user    the user performing the operation
     * @param details additional details about the operation
     * @return the saved entity
     */
    public T save(T entity, User user, String details) {
        boolean isNew = entity.getId() == null;
        log.debug("Saving entity of type {} with audit, isNew: {}", entity.getClass().getSimpleName(), isNew);

        T savedEntity = repository.save(entity);
        log.debug("Entity saved with ID: {}", savedEntity.getId());

        if (auditService != null) {
            String entityType = entity.getClass().getSimpleName();
            String entityId = savedEntity.getId().toString();

            if (isNew) {
                log.debug("Creating audit record for new entity: {}, ID: {}", entityType, entityId);
                auditService.auditCreate(entityType, entityId, user, details);
            } else {
                log.debug("Creating audit record for updated entity: {}, ID: {}", entityType, entityId);
                auditService.auditUpdate(entityType, entityId, user, details);
            }
        } else {
            log.debug("Audit service is null, skipping audit record creation");
        }

        return savedEntity;
    }

    @Override
    public List<T> findAll() {
        log.debug("Finding all entities");
        List<T> entities = repository.findAll();
        log.debug("Found {} entities", entities.size());
        return entities;
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        log.debug("Finding all entities with pagination: page {}, size {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<T> page = repository.findAll(pageable);
        log.debug("Found {} entities (page {} of {})",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());
        return page;
    }

    @Override
    public Optional<T> findById(ID id) {
        log.debug("Finding entity by ID: {}", id);
        Optional<T> entity = repository.findById(id);
        if (entity.isPresent()) {
            log.debug("Found entity of type {}", entity.get().getClass().getSimpleName());
        } else {
            log.debug("No entity found with ID: {}", id);
        }
        return entity;
    }

    /**
     * Find an entity by ID and create an audit record.
     *
     * @param id      the ID of the entity to find
     * @param user    the user performing the operation
     * @param details additional details about the operation
     * @return an Optional containing the entity, or empty if not found
     */
    public Optional<T> findById(ID id, User user, String details) {
        log.debug("Finding entity by ID: {} with audit", id);
        Optional<T> result = repository.findById(id);

        if (result.isPresent()) {
            log.debug("Found entity of type {}", result.get().getClass().getSimpleName());

            if (auditService != null) {
                String entityType = result.get().getClass().getSimpleName();
                log.debug("Creating audit record for read operation on entity: {}, ID: {}", entityType, id);
                auditService.auditRead(entityType, id.toString(), user, details);
            } else {
                log.debug("Audit service is null, skipping audit record creation");
            }
        } else {
            log.debug("No entity found with ID: {}", id);
        }

        return result;
    }

    @Override
    public void deleteById(ID id) {
        log.debug("Deleting entity with ID: {}", id);
        repository.deleteById(id);
        log.debug("Entity deleted with ID: {}", id);
    }

    /**
     * Delete an entity by ID and create an audit record.
     *
     * @param id          the ID of the entity to delete
     * @param entityClass the class of the entity being deleted
     * @param user        the user performing the operation
     * @param details     additional details about the operation
     */
    public void deleteById(ID id, Class<?> entityClass, User user, String details) {
        log.debug("Deleting entity of type {} with ID: {} with audit", entityClass.getSimpleName(), id);
        repository.deleteById(id);
        log.debug("Entity deleted with ID: {}", id);

        if (auditService != null) {
            String entityType = entityClass.getSimpleName();
            log.debug("Creating audit record for delete operation on entity: {}, ID: {}", entityType, id);
            auditService.auditDelete(entityType, id.toString(), user, details);
        } else {
            log.debug("Audit service is null, skipping audit record creation");
        }
    }

}
