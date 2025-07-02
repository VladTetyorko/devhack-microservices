package com.vladte.devhack.common.mapper;

import com.vladte.devhack.common.dto.BaseDTO;
import com.vladte.devhack.entities.BasicEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Interface for mapping between entities and DTOs.
 * This follows the Single Responsibility Principle by separating mapping logic from business logic.
 *
 * @param <E> the entity type, must extend BasicEntity
 * @param <D> the DTO type, must implement BaseDTO
 */
public interface EntityDTOMapper<E extends BasicEntity, D extends BaseDTO> {

    /**
     * Convert an entity to a DTO.
     *
     * @param entity the entity to convert
     * @return the resulting DTO
     */
    D toDTO(E entity);

    /**
     * Convert a DTO to an entity.
     *
     * @param dto the DTO to convert
     * @return the resulting entity
     */
    E toEntity(D dto);

    /**
     * Convert a list of entities to a list of DTOs.
     *
     * @param entities the entities to convert
     * @return the resulting list of DTOs
     */
    default List<D> toDTOList(List<E> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert a list of DTOs to a list of entities.
     *
     * @param dtos the DTOs to convert
     * @return the resulting list of entities
     */
    default List<E> toEntityList(List<D> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update an entity with data from a DTO.
     *
     * @param entity the entity to update
     * @param dto    the DTO containing the new data
     * @return the updated entity
     */
    E updateEntityFromDTO(E entity, D dto);
}