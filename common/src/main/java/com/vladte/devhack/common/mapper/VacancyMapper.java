package com.vladte.devhack.common.mapper;

import com.vladte.devhack.common.dto.VacancyDTO;
import com.vladte.devhack.entities.Vacancy;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Vacancy entity and VacancyDTO.
 */
@Component
public class VacancyMapper implements EntityDTOMapper<Vacancy, VacancyDTO> {

    @Override
    public VacancyDTO toDTO(Vacancy entity) {
        if (entity == null) {
            return null;
        }

        VacancyDTO dto = new VacancyDTO();
        dto.setId(entity.getId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setPosition(entity.getPosition());
        dto.setTechnologies(entity.getTechnologies());
        dto.setSource(entity.getSource());
        dto.setUrl(entity.getUrl());
        dto.setAppliedAt(entity.getAppliedAt());
        dto.setStatus(entity.getStatus());
        dto.setContactPerson(entity.getContactPerson());
        dto.setContactEmail(entity.getContactEmail());
        dto.setDeadline(entity.getDeadline());
        dto.setRemoteAllowed(entity.getRemoteAllowed());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Set response count if responses are loaded
        if (entity.getResponses() != null) {
            dto.setResponseCount(entity.getResponses().size());
        }

        return dto;
    }

    @Override
    public Vacancy toEntity(VacancyDTO dto) {
        if (dto == null) {
            return null;
        }

        Vacancy entity = new Vacancy();
        entity.setId(dto.getId());
        entity.setCompanyName(dto.getCompanyName());
        entity.setPosition(dto.getPosition());
        entity.setTechnologies(dto.getTechnologies());
        entity.setSource(dto.getSource());
        entity.setUrl(dto.getUrl());
        entity.setAppliedAt(dto.getAppliedAt());
        entity.setStatus(dto.getStatus());
        entity.setContactPerson(dto.getContactPerson());
        entity.setContactEmail(dto.getContactEmail());
        entity.setDeadline(dto.getDeadline());
        entity.setRemoteAllowed(dto.getRemoteAllowed());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        // Note: Responses need to be set by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }

    @Override
    public Vacancy updateEntityFromDTO(Vacancy entity, VacancyDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setCompanyName(dto.getCompanyName());
        entity.setPosition(dto.getPosition());
        entity.setTechnologies(dto.getTechnologies());
        entity.setSource(dto.getSource());
        entity.setUrl(dto.getUrl());
        entity.setAppliedAt(dto.getAppliedAt());
        entity.setStatus(dto.getStatus());
        entity.setContactPerson(dto.getContactPerson());
        entity.setContactEmail(dto.getContactEmail());
        entity.setDeadline(dto.getDeadline());
        entity.setRemoteAllowed(dto.getRemoteAllowed());

        // Note: Responses need to be updated by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }
}