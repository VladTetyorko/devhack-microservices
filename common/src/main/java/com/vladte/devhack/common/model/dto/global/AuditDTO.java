package com.vladte.devhack.common.model.dto.global;

import com.vladte.devhack.common.model.dto.BaseDTO;
import com.vladte.devhack.entities.global.Audit.OperationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Audit entity.
 * Implements BaseDTO for consistency with other DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditDTO implements BaseDTO {
    private UUID id;
    private OperationType operationType;
    private String entityType;
    private String entityId;
    private UUID userId;
    private String userName;
    private LocalDateTime timestamp;
    private String details;
    private LocalDateTime createdAt;
}