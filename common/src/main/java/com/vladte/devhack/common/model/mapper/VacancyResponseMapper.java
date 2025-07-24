package com.vladte.devhack.common.model.mapper;

import com.vladte.devhack.common.model.dto.VacancyResponseDTO;
import com.vladte.devhack.entities.global.Tag;
import com.vladte.devhack.entities.personalized.VacancyResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting between VacancyResponse entity and VacancyResponseDTO.
 */
@Component
public class VacancyResponseMapper implements EntityDTOMapper<VacancyResponse, VacancyResponseDTO> {

    private final InterviewStageMapper interviewStageMapper;

    public VacancyResponseMapper(InterviewStageMapper interviewStageMapper) {
        this.interviewStageMapper = interviewStageMapper;
    }

    @Override
    public VacancyResponseDTO toDTO(VacancyResponse entity) {
        if (entity == null) {
            return null;
        }

        VacancyResponseDTO dto = new VacancyResponseDTO();
        dto.setId(entity.getId());

        // Get company-related properties from the Vacancy entity
        if (entity.getVacancy() != null) {
            dto.setCompanyName(entity.getVacancy().getCompanyName());
            dto.setPosition(entity.getVacancy().getPosition());
            dto.setTechnologies(entity.getVacancy().getTechnologies());
            dto.setVacancyId(entity.getVacancy().getId());
        }

        dto.setPros(entity.getPros());
        dto.setCons(entity.getCons());
        dto.setNotes(entity.getNotes());
        dto.setSalary(entity.getSalary());
        dto.setLocation(entity.getLocation());

        // Map interview stage
        if (entity.getInterviewStage() != null) {
            dto.setInterviewStageId(entity.getInterviewStage().getId().toString());
            dto.setInterviewStage(entity.getInterviewStage().getCode());
            dto.setInterviewStageDTO(interviewStageMapper.toDTO(entity.getInterviewStage()));
        }

        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserName(entity.getUser().getProfile().getName());
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
        // Vacancy will be set by the service layer
        entity.setPros(dto.getPros());
        entity.setCons(dto.getCons());
        entity.setNotes(dto.getNotes());
        entity.setSalary(dto.getSalary());
        entity.setLocation(dto.getLocation());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        // Note: User, vacancy, interview stage and tags need to be set by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }

    @Override
    public VacancyResponse updateEntityFromDTO(VacancyResponse entity, VacancyResponseDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        // Vacancy will be updated by the service layer
        entity.setPros(dto.getPros());
        entity.setCons(dto.getCons());
        entity.setNotes(dto.getNotes());
        entity.setSalary(dto.getSalary());
        entity.setLocation(dto.getLocation());
        entity.setCreatedAt(dto.getCreatedAt());

        // Note: User, vacancy, interview stage and tags need to be updated by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }
}
