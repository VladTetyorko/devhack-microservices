package com.vladte.devhack.domain.model.mapper.global;

import com.vladte.devhack.domain.entities.global.InterviewStageCategory;
import com.vladte.devhack.domain.model.dto.global.InterviewStageCategoryDTO;
import com.vladte.devhack.domain.model.mapper.EntityDTOMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between InterviewStageCategory entity and InterviewStageCategoryDTO.
 */
@Component
public class InterviewStageCategoryMapper implements EntityDTOMapper<InterviewStageCategory, InterviewStageCategoryDTO> {

    @Override
    public InterviewStageCategoryDTO toDTO(InterviewStageCategory entity) {
        if (entity == null) {
            return null;
        }

        InterviewStageCategoryDTO dto = new InterviewStageCategoryDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setLabel(entity.getLabel());
        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }

    @Override
    public InterviewStageCategory toEntity(InterviewStageCategoryDTO dto) {
        if (dto == null) {
            return null;
        }

        InterviewStageCategory entity = new InterviewStageCategory();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setLabel(dto.getLabel());

        return entity;
    }

    @Override
    public InterviewStageCategory updateEntityFromDTO(InterviewStageCategory entity, InterviewStageCategoryDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setCode(dto.getCode());
        entity.setLabel(dto.getLabel());

        return entity;
    }
}