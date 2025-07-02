package com.vladte.devhack.common.mapper;

import com.vladte.devhack.common.dto.AuditDTO;
import com.vladte.devhack.entities.Audit;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Audit entity and AuditDTO.
 */
@Component
public class AuditMapper implements EntityDTOMapper<Audit, AuditDTO> {

    @Override
    public AuditDTO toDTO(Audit entity) {
        if (entity == null) {
            return null;
        }

        AuditDTO dto = new AuditDTO();
        dto.setId(entity.getId());
        dto.setOperationType(entity.getOperationType());
        dto.setEntityType(entity.getEntityType());
        dto.setEntityId(entity.getEntityId());
        dto.setTimestamp(entity.getTimestamp());
        dto.setDetails(entity.getDetails());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserName(entity.getUser().getName());
        }

        return dto;
    }

    @Override
    public Audit toEntity(AuditDTO dto) {
        if (dto == null) {
            return null;
        }

        Audit entity = new Audit();
        entity.setId(dto.getId());
        entity.setOperationType(dto.getOperationType());
        entity.setEntityType(dto.getEntityType());
        entity.setEntityId(dto.getEntityId());
        entity.setTimestamp(dto.getTimestamp());
        entity.setDetails(dto.getDetails());

        // Note: User needs to be set by the service layer
        // as it requires fetching the related entity from the database

        return entity;
    }

    @Override
    public Audit updateEntityFromDTO(Audit entity, AuditDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setOperationType(dto.getOperationType());
        entity.setEntityType(dto.getEntityType());
        entity.setEntityId(dto.getEntityId());
        entity.setTimestamp(dto.getTimestamp());
        entity.setDetails(dto.getDetails());

        // Note: User needs to be updated by the service layer
        // as it requires fetching the related entity from the database

        return entity;
    }
}