package com.vladte.devhack.domain.service;

import com.vladte.devhack.domain.entities.BasicEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Base service interface for common CRUD operations.
 *
 * @param <T>  the entity type, must extend BasicEntity
 * @param <ID> the entity ID type
 */
public interface CrudService<T extends BasicEntity, ID> {
    /**
     * Save an entity.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);

    /**
     * Find all entities.
     *
     * @return a list of all entities
     */
    List<T> findAll();

    /**
     * Find all entities with pagination.
     *
     * @param pageable pagination information
     * @return a page of entities
     */
    Page<T> findAll(Pageable pageable);

    /**
     * Find an entity by ID.
     *
     * @param id the ID of the entity to find
     * @return an Optional containing the entity, or empty if not found
     */
    Optional<T> findById(ID id);

    /**
     * Delete an entity by ID.
     *
     * @param id the ID of the entity to delete
     */
    void deleteById(ID id);
}
