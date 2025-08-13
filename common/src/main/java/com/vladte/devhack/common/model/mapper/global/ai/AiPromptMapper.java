package com.vladte.devhack.common.model.mapper.global.ai;

import com.vladte.devhack.common.model.dto.global.ai.AiPromptDTO;
import com.vladte.devhack.common.model.mapper.EntityDTOMapper;
import com.vladte.devhack.entities.global.ai.AiPrompt;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between AiPrompt entity and AiPromptDTO.
 */
@Component
public class AiPromptMapper implements EntityDTOMapper<AiPrompt, AiPromptDTO> {

    public AiPromptDTO toDTO(AiPrompt entity) {
        if (entity == null) {
            return null;
        }

        AiPromptDTO dto = new AiPromptDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setDescription(entity.getDescription());
        dto.setPrompt(entity.getPrompt());
        dto.setLanguage(entity.getLanguage());
        dto.setActive(entity.getActive());
        dto.setAmountOfArguments(entity.getAmountOfArguments());
        dto.setArgsDescription(entity.getArgsDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getId());
            dto.setCategoryName(entity.getCategory().getName());
        }

        return dto;
    }

    public AiPrompt toEntity(AiPromptDTO dto) {
        if (dto == null) {
            return null;
        }

        AiPrompt entity = new AiPrompt();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setDescription(dto.getDescription());
        entity.setPrompt(dto.getPrompt());
        entity.setLanguage(dto.getLanguage());
        entity.setActive(dto.getActive());
        entity.setAmountOfArguments(dto.getAmountOfArguments());
        entity.setArgsDescription(dto.getArgsDescription());

        // Note: Category needs to be set by the service layer
        // as it requires fetching the related entity from the database

        return entity;
    }

    public AiPrompt updateEntityFromDTO(AiPrompt entity, AiPromptDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setCode(dto.getCode());
        entity.setDescription(dto.getDescription());
        entity.setPrompt(dto.getPrompt());
        entity.setLanguage(dto.getLanguage());
        entity.setActive(dto.getActive());
        entity.setAmountOfArguments(dto.getAmountOfArguments());
        entity.setArgsDescription(dto.getArgsDescription());

        // Note: Category needs to be updated by the service layer
        // as it requires fetching the related entity from the database

        return entity;
    }

}
