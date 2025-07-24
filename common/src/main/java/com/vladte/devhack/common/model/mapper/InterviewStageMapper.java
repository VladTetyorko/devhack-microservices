package com.vladte.devhack.common.model.mapper;

import com.vladte.devhack.common.model.dto.InterviewStageDTO;
import com.vladte.devhack.entities.global.InterviewStage;
import com.vladte.devhack.entities.global.InterviewStageCategory;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between InterviewStage entity and InterviewStageDTO.
 */
@Component
public class InterviewStageMapper implements EntityDTOMapper<InterviewStage, InterviewStageDTO> {

    private final InterviewStageCategoryMapper categoryMapper;


    public InterviewStageMapper(InterviewStageCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public InterviewStageDTO toDTO(InterviewStage entity) {
        if (entity == null) {
            return null;
        }

        InterviewStageDTO dto = new InterviewStageDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setLabel(entity.getLabel());
        dto.setOrderIndex(entity.getOrderIndex());
        dto.setColorClass(entity.getColorClass());
        dto.setIconClass(entity.getIconClass());
        dto.setActive(entity.getActive());
        dto.setFinalStage(entity.getFinalStage());
        dto.setInternalOnly(entity.getInternalOnly());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getId());
            dto.setCategory(categoryMapper.toDTO(entity.getCategory()));
        }

        return dto;
    }

    @Override
    public InterviewStage toEntity(InterviewStageDTO dto) {
        if (dto == null) {
            return null;
        }

        InterviewStage entity = new InterviewStage();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setLabel(dto.getLabel());
        entity.setOrderIndex(dto.getOrderIndex());
        entity.setColorClass(dto.getColorClass());
        entity.setIconClass(dto.getIconClass());
        entity.setActive(dto.getActive());
        entity.setFinalStage(dto.getFinalStage());
        entity.setInternalOnly(dto.getInternalOnly());

        // Note: Category needs to be set by the service layer
        // as it requires fetching the related entity from the database
        if (dto.getCategoryId() != null) {
            InterviewStageCategory category = new InterviewStageCategory();
            category.setId(dto.getCategoryId());
            entity.setCategory(category);
        }

        return entity;
    }

    @Override
    public InterviewStage updateEntityFromDTO(InterviewStage entity, InterviewStageDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setCode(dto.getCode());
        entity.setLabel(dto.getLabel());
        entity.setOrderIndex(dto.getOrderIndex());
        entity.setColorClass(dto.getColorClass());
        entity.setIconClass(dto.getIconClass());
        entity.setActive(dto.getActive());
        entity.setFinalStage(dto.getFinalStage());
        entity.setInternalOnly(dto.getInternalOnly());

        // Note: Category needs to be updated by the service layer
        // as it requires fetching the related entity from the database

        return entity;
    }
}