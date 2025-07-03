package com.vladte.devhack.common.service.domain.impl;

import com.vladte.devhack.common.service.domain.AuditService;
import com.vladte.devhack.common.service.domain.BaseService;
import com.vladte.devhack.entities.BasicEntity;
import com.vladte.devhack.entities.User;
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
public abstract class BaseServiceImpl<T extends BasicEntity, ID, R extends JpaRepository<T, ID>> implements BaseService<T, ID> {

    private static final Logger logger = LoggerFactory.getLogger(BaseServiceImpl.class);

    protected final R repository;
    protected final AuditService auditService;

    /**
     * Constructor with repository and auditService injection.
     *
     * @param repository   the JPA repository
     * @param auditService the audit service
     */
    protected BaseServiceImpl(R repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    /**
     * Constructor with repository injection.
     * This constructor is provided for services that don't need audit functionality.
     *
     * @param repository the JPA repository
     */
    protected BaseServiceImpl(R repository) {
        this.repository = repository;
        this.auditService = null;
    }

    @Override
    public T save(T entity) {
        logger.debug("Saving entity of type {}", entity.getClass().getSimpleName());
        if (entity.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        T savedEntity = repository.save(entity);
        logger.debug("Entity saved with ID: {}", savedEntity.getId());
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
        logger.debug("Saving entity of type {} with audit, isNew: {}", entity.getClass().getSimpleName(), isNew);

        T savedEntity = repository.save(entity);
        logger.debug("Entity saved with ID: {}", savedEntity.getId());

        if (auditService != null) {
            String entityType = entity.getClass().getSimpleName();
            String entityId = savedEntity.getId().toString();

            if (isNew) {
                logger.debug("Creating audit record for new entity: {}, ID: {}", entityType, entityId);
                auditService.auditCreate(entityType, entityId, user, details);
            } else {
                logger.debug("Creating audit record for updated entity: {}, ID: {}", entityType, entityId);
                auditService.auditUpdate(entityType, entityId, user, details);
            }
        } else {
            logger.debug("Audit service is null, skipping audit record creation");
        }

        return savedEntity;
    }

    @Override
    public List<T> findAll() {
        logger.debug("Finding all entities");
        List<T> entities = repository.findAll();
        logger.debug("Found {} entities", entities.size());
        return entities;
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        logger.debug("Finding all entities with pagination: page {}, size {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<T> page = repository.findAll(pageable);
        logger.debug("Found {} entities (page {} of {})",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());
        return page;
    }

    @Override
    public Optional<T> findById(ID id) {
        logger.debug("Finding entity by ID: {}", id);
        Optional<T> entity = repository.findById(id);
        if (entity.isPresent()) {
            logger.debug("Found entity of type {}", entity.get().getClass().getSimpleName());
        } else {
            logger.debug("No entity found with ID: {}", id);
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
        logger.debug("Finding entity by ID: {} with audit", id);
        Optional<T> result = repository.findById(id);

        if (result.isPresent()) {
            logger.debug("Found entity of type {}", result.get().getClass().getSimpleName());

            if (auditService != null) {
                String entityType = result.get().getClass().getSimpleName();
                logger.debug("Creating audit record for read operation on entity: {}, ID: {}", entityType, id);
                auditService.auditRead(entityType, id.toString(), user, details);
            } else {
                logger.debug("Audit service is null, skipping audit record creation");
            }
        } else {
            logger.debug("No entity found with ID: {}", id);
        }

        return result;
    }

    @Override
    public void deleteById(ID id) {
        logger.debug("Deleting entity with ID: {}", id);
        repository.deleteById(id);
        logger.debug("Entity deleted with ID: {}", id);
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
        logger.debug("Deleting entity of type {} with ID: {} with audit", entityClass.getSimpleName(), id);
        repository.deleteById(id);
        logger.debug("Entity deleted with ID: {}", id);

        if (auditService != null) {
            String entityType = entityClass.getSimpleName();
            logger.debug("Creating audit record for delete operation on entity: {}, ID: {}", entityType, id);
            auditService.auditDelete(entityType, id.toString(), user, details);
        } else {
            logger.debug("Audit service is null, skipping audit record creation");
        }
    }
}
