package com.vladte.devhack.common.controller.global.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.model.dto.global.AuditDTO;
import com.vladte.devhack.common.model.mapper.global.AuditMapper;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.entities.global.Audit;
import com.vladte.devhack.entities.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing Audit entities.
 * Provides RESTful API endpoints for CRUD operations on audit records.
 */
@RestController
@RequestMapping("/api/audits")
@Tag(name = "Audit", description = "Audit management API")
@Slf4j
public class AuditRestController extends BaseRestController<Audit, AuditDTO, UUID, AuditService, AuditMapper> {

    /**
     * Constructor with service and mapper injection.
     *
     * @param auditService the audit service
     * @param auditMapper  the audit mapper
     */
    public AuditRestController(AuditService auditService, AuditMapper auditMapper) {
        super(auditService, auditMapper);
    }

    /**
     * Get all audit records for a specific entity type.
     *
     * @param entityType the type of entity to filter by
     * @param pageable   pagination information
     * @return a page of audit records
     */
    @GetMapping("/by-entity-type/{entityType}")
    @Operation(summary = "Get all audit records for a specific entity type",
            description = "Returns a page of audit records for the specified entity type")
    public ResponseEntity<Page<AuditDTO>> getAuditsByEntityType(
            @Parameter(description = "Type of the entity")
            @PathVariable String entityType,
            Pageable pageable) {
        log.debug("REST request to get all audit records for entity type: {}", entityType);

        // Note: This would require adding a method to AuditService to filter by entity type
        // For now, we'll return all audits with pagination
        Page<Audit> page = service.findAll(pageable);
        Page<AuditDTO> dtoPage = page.map(mapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Get all audit records for a specific entity.
     *
     * @param entityType the type of entity
     * @param entityId   the ID of the entity
     * @param pageable   pagination information
     * @return a page of audit records
     */
    @GetMapping("/by-entity/{entityType}/{entityId}")
    @Operation(summary = "Get all audit records for a specific entity",
            description = "Returns a page of audit records for the specified entity")
    public ResponseEntity<Page<AuditDTO>> getAuditsByEntity(
            @Parameter(description = "Type of the entity")
            @PathVariable String entityType,
            @Parameter(description = "ID of the entity")
            @PathVariable String entityId,
            Pageable pageable) {
        log.debug("REST request to get all audit records for entity: {} with ID: {}", entityType, entityId);

        // Note: This would require adding a method to AuditService to filter by entity type and ID
        // For now, we'll return all audits with pagination
        Page<Audit> page = service.findAll(pageable);
        Page<AuditDTO> dtoPage = page.map(mapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Create an audit record for a create operation.
     *
     * @param entityType the type of entity that was created
     * @param entityId   the ID of the entity that was created
     * @param details    additional details about the operation
     * @param user       the authenticated user
     * @return the created audit record
     */
    @PostMapping("/audit-create")
    @Operation(summary = "Create an audit record for a create operation",
            description = "Creates an audit record for a create operation")
    public ResponseEntity<AuditDTO> auditCreate(
            @Parameter(description = "Type of the entity")
            @RequestParam String entityType,
            @Parameter(description = "ID of the entity")
            @RequestParam String entityId,
            @Parameter(description = "Additional details")
            @RequestParam(required = false) String details,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.debug("REST request to create audit record for create operation: {} {}", entityType, entityId);

        Audit audit = service.auditCreate(entityType, entityId, user, details);
        return ResponseEntity.ok(mapper.toDTO(audit));
    }

    /**
     * Create an audit record for an update operation.
     *
     * @param entityType the type of entity that was updated
     * @param entityId   the ID of the entity that was updated
     * @param details    additional details about the operation
     * @param user       the authenticated user
     * @return the created audit record
     */
    @PostMapping("/audit-update")
    @Operation(summary = "Create an audit record for an update operation",
            description = "Creates an audit record for an update operation")
    public ResponseEntity<AuditDTO> auditUpdate(
            @Parameter(description = "Type of the entity")
            @RequestParam String entityType,
            @Parameter(description = "ID of the entity")
            @RequestParam String entityId,
            @Parameter(description = "Additional details")
            @RequestParam(required = false) String details,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.debug("REST request to create audit record for update operation: {} {}", entityType, entityId);

        Audit audit = service.auditUpdate(entityType, entityId, user, details);
        return ResponseEntity.ok(mapper.toDTO(audit));
    }

    /**
     * Create an audit record for a delete operation.
     *
     * @param entityType the type of entity that was deleted
     * @param entityId   the ID of the entity that was deleted
     * @param details    additional details about the operation
     * @param user       the authenticated user
     * @return the created audit record
     */
    @PostMapping("/audit-delete")
    @Operation(summary = "Create an audit record for a delete operation",
            description = "Creates an audit record for a delete operation")
    public ResponseEntity<AuditDTO> auditDelete(
            @Parameter(description = "Type of the entity")
            @RequestParam String entityType,
            @Parameter(description = "ID of the entity")
            @RequestParam String entityId,
            @Parameter(description = "Additional details")
            @RequestParam(required = false) String details,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.debug("REST request to create audit record for delete operation: {} {}", entityType, entityId);

        Audit audit = service.auditDelete(entityType, entityId, user, details);
        return ResponseEntity.ok(mapper.toDTO(audit));
    }
}