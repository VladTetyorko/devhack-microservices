package com.vladte.devhack.common.mapper;

import com.vladte.devhack.common.dto.VacancyResponseDTO;
import com.vladte.devhack.entities.Tag;
import com.vladte.devhack.entities.VacancyResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting between VacancyResponse entity and VacancyResponseDTO.
 */
@Component
public class VacancyResponseMapper implements EntityDTOMapper<VacancyResponse, VacancyResponseDTO> {

    @Override
    public VacancyResponseDTO toDTO(VacancyResponse entity) {
        if (entity == null) {
            return null;
        }

        VacancyResponseDTO dto = new VacancyResponseDTO();
        dto.setId(entity.getId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setPosition(entity.getPosition());
        dto.setTechnologies(entity.getTechnologies());
        dto.setPros(entity.getPros());
        dto.setCons(entity.getCons());
        dto.setNotes(entity.getNotes());
        dto.setSalary(entity.getSalary());
        dto.setLocation(entity.getLocation());
        dto.setInterviewStage(entity.getInterviewStage());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserName(entity.getUser().getName());
        }

        if (entity.getTags() != null) {
            dto.setTagIds(entity.getTags().stream()
                    .map(Tag::getId)
                    .collect(Collectors.toSet()));
            dto.setTagNames(entity.getTags().stream()
                    .map(Tag::getName)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    @Override
    public VacancyResponse toEntity(VacancyResponseDTO dto) {
        if (dto == null) {
            return null;
        }

        VacancyResponse entity = new VacancyResponse();
        entity.setId(dto.getId());
        entity.setCompanyName(dto.getCompanyName());
        entity.setPosition(dto.getPosition());
        entity.setTechnologies(dto.getTechnologies());
        entity.setPros(dto.getPros());
        entity.setCons(dto.getCons());
        entity.setNotes(dto.getNotes());
        entity.setSalary(dto.getSalary());
        entity.setLocation(dto.getLocation());
        entity.setInterviewStage(dto.getInterviewStage());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        // Note: User and tags need to be set by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }

    @Override
    public VacancyResponse updateEntityFromDTO(VacancyResponse entity, VacancyResponseDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setCompanyName(dto.getCompanyName());
        entity.setPosition(dto.getPosition());
        entity.setTechnologies(dto.getTechnologies());
        entity.setPros(dto.getPros());
        entity.setCons(dto.getCons());
        entity.setNotes(dto.getNotes());
        entity.setSalary(dto.getSalary());
        entity.setLocation(dto.getLocation());
        entity.setInterviewStage(dto.getInterviewStage());
        entity.setCreatedAt(dto.getCreatedAt());

        // Note: User and tags need to be updated by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }
}