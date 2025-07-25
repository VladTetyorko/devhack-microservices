package com.vladte.devhack.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Base DTO for entities that are owned by a user.
 * Extends BaseTimestampedDTO and adds user-related fields.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class UserOwnedDTO extends BaseTimestampedDTO {

    @Schema(description = "ID of the user who owns this entity", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID userId;

    @Schema(description = "Name of the user who owns this entity", accessMode = Schema.AccessMode.READ_ONLY)
    private String userName;
}