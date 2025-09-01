package com.vladte.devhack.domain.model.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * Base interface for all DTOs in the system.
 * Provides common properties that all DTOs should have.
 */
public interface BaseDTO extends Serializable {

    /**
     * Get the ID of the entity.
     *
     * @return the ID
     */
    UUID getId();

    /**
     * Set the ID of the entity.
     *
     * @param id the ID to set
     */
    void setId(UUID id);
}