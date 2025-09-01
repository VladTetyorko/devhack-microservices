package com.vladte.devhack.common.controller;

import com.vladte.devhack.domain.entities.BasicEntity;
import com.vladte.devhack.domain.model.dto.BaseDTO;
import com.vladte.devhack.domain.model.mapper.EntityDTOMapper;
import com.vladte.devhack.domain.service.CrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Base REST controller for CRUD operations.
 * This class provides common REST endpoints for all entities.
 *
 * @param <E>  the entity type, must extend BasicEntity
 * @param <D>  the DTO type, must implement BaseDTO
 * @param <ID> the entity ID type
 * @param <S>  the service type, must extend BaseService
 * @param <M>  the mapper type, must implement EntityDTOMapper
 */
@Validated
@Slf4j
public abstract class BaseRestController<E extends BasicEntity, D extends BaseDTO, ID, S extends CrudService<E, ID>, M extends EntityDTOMapper<E, D>> {

    protected final S relatedEntityService;
    protected final M relatedEntityMapper;

    /**
     * Constructor with service and mapper injection.
     *
     * @param service the service
     * @param mapper  the mapper
     */
    protected BaseRestController(S service, M mapper) {
        this.relatedEntityService = service;
        this.relatedEntityMapper = mapper;
    }

    /**
     * Get all entities.
     *
     * @return a list of all entities as DTOs
     */
    @GetMapping
    @Operation(summary = "Get all entities", description = "Returns a list of all entities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<D>> getAll() {
        log.debug("REST request to get all entities");
        List<E> entities = relatedEntityService.findAll();
        return ResponseEntity.ok(relatedEntityMapper.toDTOList(entities));
    }

    /**
     * Get all entities with pagination.
     *
     * @param pageable pagination information
     * @return a page of entities as DTOs
     */
    @GetMapping("/page")
    @Operation(summary = "Get all entities with pagination", description = "Returns a page of entities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved page",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Page<D>> getAllPaged(
            @Parameter(description = "Pagination information (page, size, sort)")
            Pageable pageable) {
        log.debug("REST request to get a page of entities");
        Page<E> page = relatedEntityService.findAll(pageable);
        Page<D> dtoPage = page.map(relatedEntityMapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Get an entity by ID.
     *
     * @param id the ID of the entity to retrieve
     * @return the entity as a DTO
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get an entity by ID", description = "Returns a single entity by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved entity",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Entity not found",
                    content = @Content)
    })
    public ResponseEntity<D> getById(
            @Parameter(description = "ID of the entity to be retrieved")
            @PathVariable ID id) {
        log.debug("REST request to get entity with ID: {}", id);
        Optional<E> entity = relatedEntityService.findById(id);
        return entity.map(e -> ResponseEntity.ok(relatedEntityMapper.toDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new entity.
     *
     * @param dto the entity to create as a DTO
     * @return the created entity as a DTO
     */
    @PostMapping
    @Operation(summary = "Create a new entity", description = "Creates a new entity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Entity created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content)
    })
    public ResponseEntity<D> create(
            @Parameter(description = "Entity to be created")
            @Valid @RequestBody D dto) {
        log.debug("REST request to create entity: {}", dto);
        E entity = relatedEntityMapper.toEntity(dto);
        E savedEntity = relatedEntityService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(relatedEntityMapper.toDTO(savedEntity));
    }

    /**
     * Update an existing entity.
     *
     * @param id  the ID of the entity to update
     * @param dto the updated entity as a DTO
     * @return the updated entity as a DTO
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing entity", description = "Updates an existing entity by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity updated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Entity not found",
                    content = @Content)
    })
    public ResponseEntity<D> update(
            @Parameter(description = "ID of the entity to be updated")
            @PathVariable ID id,
            @Parameter(description = "Updated entity")
            @Valid @RequestBody D dto) {
        log.debug("REST request to update entity with ID: {}", id);
        Optional<E> existingEntity = relatedEntityService.findById(id);
        if (existingEntity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        E entity = existingEntity.get();
        relatedEntityMapper.updateEntityFromDTO(entity, dto);
        E savedEntity = relatedEntityService.save(entity);
        return ResponseEntity.ok(relatedEntityMapper.toDTO(savedEntity));
    }

    /**
     * Delete an entity by ID.
     *
     * @param id the ID of the entity to delete
     * @return no content if successful
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an entity", description = "Deletes an entity by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Entity deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found",
                    content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the entity to be deleted")
            @PathVariable ID id) {
        log.debug("REST request to delete entity with ID: {}", id);
        Optional<E> existingEntity = relatedEntityService.findById(id);
        if (existingEntity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        relatedEntityService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}