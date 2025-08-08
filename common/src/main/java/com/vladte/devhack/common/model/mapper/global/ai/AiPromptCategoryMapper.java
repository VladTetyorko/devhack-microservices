package com.vladte.devhack.common.model.mapper.global.ai;

import com.vladte.devhack.common.model.dto.global.ai.AiPromptCategoryDTO;
import com.vladte.devhack.common.model.mapper.EntityDTOMapper;
import com.vladte.devhack.entities.global.ai.AiPromptCategory;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between AiPromptCategory entity and AiPromptCategoryDTO.
 */
@Component
public class AiPromptCategoryMapper implements EntityDTOMapper<AiPromptCategory, AiPromptCategoryDTO> {

    private final AiPromptMapper aiPromptMapper;

    public AiPromptCategoryMapper(AiPromptMapper aiPromptMapper) {
        this.aiPromptMapper = aiPromptMapper;
    }

    public AiPromptCategoryDTO toDTO(AiPromptCategory entity) {
        if (entity == null) {
            return null;
        }

        AiPromptCategoryDTO dto = new AiPromptCategoryDTO();
        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCode(entity.getCode());
        dto.setDescription(entity.getDescription());
        dto.setName(entity.getName());

        if (entity.getPrompts() != null) {
            dto.setPrompts(aiPromptMapper.toDTOList(entity.getPrompts()));
            dto.setPromptCount(entity.getPrompts().size());
        } else {
            dto.setPromptCount(0);
        }

        return dto;
    }

    public AiPromptCategory toEntity(AiPromptCategoryDTO dto) {
        if (dto == null) {
            return null;
        }

        AiPromptCategory entity = new AiPromptCategory();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setDescription(dto.getDescription());
        entity.setName(dto.getName());

        // Note: Prompts list is managed by the service layer
        // to avoid circular dependencies and ensure proper persistence

        return entity;
    }

    public AiPromptCategory updateEntityFromDTO(AiPromptCategory entity, AiPromptCategoryDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setCode(dto.getCode());
        entity.setDescription(dto.getDescription());
        entity.setName(dto.getName());

        // Note: Prompts list is managed by the service layer
        // to avoid circular dependencies and ensure proper persistence

        return entity;
    }

}
