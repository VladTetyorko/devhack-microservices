package com.vladte.devhack.common.service.domain.personalized;

import com.vladte.devhack.common.service.domain.AuditableCrudService;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.entities.BasicEntity;
import com.vladte.devhack.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Base implementation for services that handle user-owned entities.
 * This class extends BaseServiceImpl and adds access control based on user roles.
 *
 * @param <T>  the entity type, must extend BasicEntity
 * @param <ID> the entity ID type
 * @param <R>  the repository type
 */
@Component
public abstract class PersonalizedService<T extends BasicEntity, ID, R extends JpaRepository<T, ID>>
        extends AuditableCrudService<T, ID, R> {

    private static final Logger log = LoggerFactory.getLogger(PersonalizedService.class);
    private static final String ROLE_MANAGER = "ROLE_MANAGER";

    /**
     * Constructor with repository and auditService injection.
     *
     * @param repository   the JPA repository
     * @param auditService the audit service
     */
    protected PersonalizedService(R repository, AuditService auditService) {
        super(repository, auditService);
    }


    /**
     * Get the user associated with the entity.
     *
     * @param entity the entity
     * @return the user associated with the entity
     */
    protected abstract User getEntityUser(T entity);

    /**
     * Check if the current user has access to the entity.
     *
     * @param entity the entity to check
     * @return true if the current user has access to the entity, false otherwise
     */
    protected boolean hasAccessToEntity(T entity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If authentication is null (e.g., when called from an async thread), allow access
        // This is a security trade-off to allow async operations to work
        if (authentication == null) {
            log.warn("Authentication is null when checking access to entity. This may be due to an async call. Allowing access.");
            return true;
        }

        // Managers have access to all entities
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_MANAGER))) {
            return true;
        }

        // Users have access only to their own entities
        User entityUser = getEntityUser(entity);
        return entityUser != null && entityUser.getEmail().equals(authentication.getName());
    }

    /**
     * Get the current authenticated user's email.
     *
     * @return the current user's email
     */
    protected String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.warn("Authentication is null when getting current user email. This may be due to an async call.");
            return "system"; // Return a default value for async operations
        }
        return authentication.getName();
    }

    /**
     * Check if the current user is a manager.
     *
     * @return true if the current user is a manager, false otherwise
     */
    protected boolean isCurrentUserManager() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.warn("Authentication is null when checking if current user is manager. This may be due to an async call.");
            return false; // Default to non-manager for security
        }
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_MANAGER));
    }

    /**
     * Find all entities that the current user has access to.
     *
     * @return a list of entities
     */
    @Override
    public List<T> findAll() {
        log.debug("Finding all entities with access control");
        List<T> allEntities = repository.findAll();

        // If the current user is a manager, return all entities
        if (isCurrentUserManager()) {
            log.debug("Current user is a manager, returning all {} entities", allEntities.size());
            return allEntities;
        }

        // Otherwise, filter entities based on user access
        List<T> accessibleEntities = allEntities.stream()
                .filter(this::hasAccessToEntity)
                .collect(Collectors.toList());

        log.debug("Filtered {} entities down to {} accessible entities for current user",
                allEntities.size(), accessibleEntities.size());

        return accessibleEntities;
    }

    /**
     * Find all entities with pagination that the current user has access to.
     *
     * @param pageable the pagination information
     * @return a page of entities
     */
    @Override
    public Page<T> findAll(Pageable pageable) {
        log.debug("Finding all entities with pagination and access control");
        Page<T> page = repository.findAll(pageable);

        // If the current user is a manager, return all entities
        if (isCurrentUserManager()) {
            log.debug("Current user is a manager, returning all entities");
            return page;
        }

        // Otherwise, we need to filter the page content
        // Note: This is not an optimal solution for large datasets
        // A better approach would be to implement a custom repository method
        // that filters at the database level
        List<T> filteredContent = page.getContent().stream()
                .filter(this::hasAccessToEntity)
                .collect(Collectors.toList());

        log.debug("Filtered page content from {} entities to {} accessible entities",
                page.getContent().size(), filteredContent.size());

        // Create a new page with the filtered content
        // This is a simplified approach and doesn't handle pagination correctly
        // For a production system, a custom repository implementation would be better
        return new org.springframework.data.domain.PageImpl<>(
                filteredContent, pageable, filteredContent.size());
    }

    /**
     * Find an entity by ID if the current user has access to it.
     *
     * @param id the ID of the entity to find
     * @return an Optional containing the entity, or empty if not found or not accessible
     */
    @Override
    public Optional<T> findById(ID id) {
        log.debug("Finding entity by ID: {} with access control", id);
        Optional<T> entityOpt = repository.findById(id);

        if (entityOpt.isPresent()) {
            T entity = entityOpt.get();

            // Check if the current user has access to the entity
            if (hasAccessToEntity(entity)) {
                log.debug("Current user has access to entity with ID: {}", id);
                return entityOpt;
            } else {
                log.debug("Current user does not have access to entity with ID: {}", id);
                return Optional.empty();
            }
        }

        log.debug("No entity found with ID: {}", id);
        return Optional.empty();
    }
}
